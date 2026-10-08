# Bublik Pro 🥯

**HQ round videos for Telegram, tuned for the Pixel 11 Pro series.**

Bublik Pro is an *unofficial* Telegram client: a small fork of
[Telegram for Android](https://github.com/DrKLO/Telegram) (GPL v2) that records round video
messages ("circles") at up to **720×720, H.264 High, 6 Mbps**. The stock app records 384–480p
at about 1 Mbps.

> Not affiliated with Telegram FZ-LLC or Google. "Telegram" and "Pixel" are trademarks of their owners.

## What's different

| | Telegram (Camera2 recorder) | Bublik Pro |
|---|---|---|
| Output resolution | 360p / 480p | 360p / 480p / **640p / 720p** |
| Video bitrate | 0.75 – 1.2 Mbps | 0.75 – **6 Mbps** (default 4) |
| H.264 profile | encoder default | **High** when the encoder supports it, automatic fallback otherwise |
| Rate control | encoder default | VBR when supported, bitrate clamped to the encoder's range |
| Camera source for 640/720p | — | 1080p crop, downscaled |

Settings → **Experimental → Round Video** lets you choose resolution, camera source, 30/60 fps and bitrate.

Recipients get the file **byte-for-byte as recorded**: the client does not re-encode round videos,
and we verified that the copy downloaded from Telegram's servers by the official app has the same SHA-256.

Measured on a Pixel 11 Pro XL (Android 17), with hardware encoder `c2.google.avc.encoder`:
720×720, High profile, level 3.1, 30 fps, about 6.1–6.3 Mbps at the 6 Mbps setting, a keyframe every second.

## Limitations, please read

- **Push notifications don't work.** Telegram's FCM project only serves official builds.
  Messages arrive while the app is open or connected in the background.
- Google sign-in and passkeys are disabled (they only work with official app IDs).
- arm64 only. Tested on the Pixel 11 Pro XL; the Pixel 11 Pro has the same SoC and should behave the same.
  Other phones may work too, since everything falls back to safe defaults, but nobody has tested them.
- Files are larger: a 60 s circle at 6 Mbps is about 48 MB, at 4 Mbps about 30 MB.
- Installs alongside the official Telegram (`app.bublik.pro`); log in as a separate session.

## Install

Download the APK from [Releases](../../releases), allow installs from your browser or file manager, and open it.
Verify the signing certificate fingerprint published in the release notes.

## Build it yourself

1. Get your own `api_id` / `api_hash` at <https://my.telegram.org>.
2. `git clone --recurse-submodules --shallow-submodules <this repo>`
3. Put them in `local.properties` (never committed):
   ```
   sdk.dir=/path/to/Android/sdk
   TG_API_ID=12345
   TG_API_HASH=0123456789abcdef0123456789abcdef
   ```
   or export `TG_API_ID` / `TG_API_HASH`.
4. Requirements: JDK 21, Android SDK 36, NDK 27.2.12479018, CMake 3.22.1.
5. Debug build: `./gradlew :TMessagesProj_App:assembleAfatDebug`
6. Release build: create your own key and a `keystore.properties` (also never committed):
   ```
   storeFile=/absolute/path/bublik-release.jks
   storePassword=...
   keyAlias=bublik
   keyPassword=...
   ```
   then run `./gradlew :TMessagesProj_App:assembleAfatRelease`.
   Without `keystore.properties` the release APK is left unsigned. The public key bundled in upstream is never used.

## Changed files

- `utils/camera/roundvideo/RoundVideoSession.java`: 640p / 720p output sizes
- `utils/camera/roundvideo/RoundVideoCameraController.java`: source crop for HQ sizes
- `utils/camera/roundvideo/RoundVideoCodecRecorder.java`: High profile and level, VBR, bitrate clamping, fallback
- `ui/RoundVideoSettingsActivity.java`: new choices (and a fix for a `null` bitrate label in release builds)
- `utils/settings/SharedSettings.java`: HQ defaults, Round Video settings visible in release
- `messenger/BuildVars.java`, Gradle files: API credentials from local config, own package and signing, no update checks

## Українською

**Bublik Pro** — неофіційний клієнт Telegram, який записує відеокружки у якості до 720×720,
H.264 High, 6 Мбіт/с. Налаштований під Pixel 11 Pro та Pro XL. Ставиться поруч з офіційним Telegram.
Push-сповіщення не працюють: це обмеження всіх неофіційних збірок без власного Firebase.

## License

GPL v2, same as upstream. See [LICENSE](LICENSE). Upstream README: [README_UPSTREAM.md](README_UPSTREAM.md).
