package org.telegram.messenger;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONObject;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.utils.settings.SharedSettings;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

/**
 * Bublik Pro: push notifications without Telegram's own FCM project.
 *
 * Telegram's server can't reach our FCM project, but it can send Web Push (token_type 10) to any
 * HTTPS endpoint. We register an endpoint on our relay (Tools/bublik/push-relay) that carries our
 * FCM token; the relay turns each Web Push into a plain FCM "wake up" message, and on that message
 * the app connects and shows notifications from the updates it receives. The payload is never
 * forwarded, so the relay only sees that something arrived.
 */
public final class BublikPush {

    public static final int TOKEN_TYPE_WEB_PUSH = 10;
    private static final String PREFS = "bublik_push";

    private BublikPush() {
    }

    /** The relay is configured in this build and the user hasn't switched it off. */
    public static boolean isActive() {
        return !TextUtils.isEmpty(BuildConfig.BUBLIK_PUSH_RELAY) && SharedSettings.bublikPushEnabled.get();
    }

    /** Web Push subscription JSON for {@code account.registerDevice}, built around our FCM token. */
    public static String webPushToken(String fcmToken) {
        try {
            final SharedPreferences prefs = ApplicationLoader.applicationContext.getSharedPreferences(PREFS, 0);
            String p256dh = prefs.getString("p256dh", null);
            String auth = prefs.getString("auth", null);
            if (p256dh == null || auth == null) {
                // Telegram encrypts the payload to this key. We only use the push as a wake-up signal,
                // so the private half is never needed and isn't stored.
                final KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
                generator.initialize(new ECGenParameterSpec("secp256r1"));
                final KeyPair pair = generator.generateKeyPair();
                p256dh = base64Url(uncompressedPoint((ECPublicKey) pair.getPublic()));
                final byte[] secret = new byte[16];
                Utilities.random.nextBytes(secret);
                auth = base64Url(secret);
                prefs.edit().putString("p256dh", p256dh).putString("auth", auth).apply();
            }
            final JSONObject keys = new JSONObject();
            keys.put("p256dh", p256dh);
            keys.put("auth", auth);
            final JSONObject token = new JSONObject();
            token.put("endpoint", relayUrl() + "/p/" + fcmToken);
            token.put("keys", keys);
            return token.toString();
        } catch (Exception e) {
            FileLog.e(e);
            return null;
        }
    }

    /** True for the wake-up messages our relay sends. */
    public static boolean isRelayMessage(java.util.Map<String, String> data) {
        return data != null && data.containsKey("bublik");
    }

    /** Wakes every logged-in account so it fetches updates and shows notifications. */
    public static void onWake() {
        AndroidUtilities.runOnUIThread(() -> {
            ApplicationLoader.postInitApplication();
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                if (UserConfig.getInstance(a).isClientActivated()) {
                    org.telegram.tgnet.ConnectionsManager.onInternalPushReceived(a);
                    org.telegram.tgnet.ConnectionsManager.getInstance(a).resumeNetworkMaybe();
                }
            }
        });
    }

    /** Turns relay pushes on or off for every logged-in account. */
    public static void setEnabled(boolean enabled) {
        // Compute the endpoint before the switch flips, so turning off can still name it.
        final String webPushToken = TextUtils.isEmpty(SharedConfig.pushString) ? null : webPushToken(SharedConfig.pushString);
        SharedSettings.bublikPushEnabled.set(enabled);
        if (webPushToken == null) {
            return;
        }
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            final UserConfig userConfig = UserConfig.getInstance(a);
            if (!userConfig.isClientActivated()) {
                continue;
            }
            userConfig.registeredForPush = false;
            userConfig.saveConfig(false);
            if (enabled) {
                MessagesController.getInstance(a).registerForPush(SharedConfig.pushType, SharedConfig.pushString);
            } else {
                final TL_account.unregisterDevice req = new TL_account.unregisterDevice();
                req.token_type = TOKEN_TYPE_WEB_PUSH;
                req.token = webPushToken;
                org.telegram.tgnet.ConnectionsManager.getInstance(a).sendRequest(req, null);
            }
        }
    }

    private static String relayUrl() {
        String url = BuildConfig.BUBLIK_PUSH_RELAY;
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private static byte[] uncompressedPoint(ECPublicKey key) {
        final byte[] out = new byte[65];
        out[0] = 0x04;
        copyUnsigned(key.getW().getAffineX(), out, 1);
        copyUnsigned(key.getW().getAffineY(), out, 33);
        return out;
    }

    private static void copyUnsigned(BigInteger value, byte[] out, int offset) {
        final byte[] bytes = value.toByteArray();
        final int start = bytes.length > 32 ? bytes.length - 32 : 0;
        final int length = bytes.length - start;
        System.arraycopy(bytes, start, out, offset + 32 - length, length);
    }

    private static String base64Url(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }
}
