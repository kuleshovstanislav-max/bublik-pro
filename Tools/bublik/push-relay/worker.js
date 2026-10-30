// Bublik Pro push relay (Cloudflare Worker).
//
// Telegram's server sends a Web Push (token_type 10) to https://<relay>/p/<fcm token>.
// We don't read or forward the encrypted payload: each push becomes a high-priority FCM
// data message {"bublik": "1"} that wakes the app, which then fetches its updates itself.
//
// Secrets (wrangler secret put ...): FCM_SERVICE_ACCOUNT = the Firebase service account JSON.

const FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
const TOKEN_URL = "https://oauth2.googleapis.com/token";
const MAX_BODY = 8192;
const RATE_PER_MINUTE = 60;

let cachedAccess = null; // { token, expires }
const recent = new Map(); // fcm token -> [window start, count], per isolate

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const match = url.pathname.match(/^\/p\/([A-Za-z0-9_:\-]{20,400})$/);
    if (request.method !== "POST" || !match) {
      return new Response("Not found", { status: 404 });
    }
    const length = Number(request.headers.get("content-length") || "0");
    if (length > MAX_BODY) {
      return new Response("Payload too large", { status: 413 });
    }
    const fcmToken = match[1];
    if (rateLimited(fcmToken)) {
      return new Response("Too many requests", { status: 429 });
    }

    const account = JSON.parse(env.FCM_SERVICE_ACCOUNT);
    const access = await accessToken(account);
    const response = await fetch(
      `https://fcm.googleapis.com/v1/projects/${account.project_id}/messages:send`,
      {
        method: "POST",
        headers: { authorization: `Bearer ${access}`, "content-type": "application/json" },
        body: JSON.stringify({
          message: {
            token: fcmToken,
            data: { bublik: "1" },
            android: { priority: "high", ttl: `${ttlSeconds(request)}s` },
          },
        }),
      },
    );
    if (response.ok) {
      return new Response(null, { status: 201 });
    }
    const error = await response.text();
    // An app that was uninstalled or reset its token: tell Telegram to forget this endpoint.
    if (response.status === 404 || error.includes("UNREGISTERED")) {
      return new Response("Gone", { status: 410 });
    }
    console.log("FCM error", response.status, error.slice(0, 300));
    return new Response("Upstream error", { status: 502 });
  },
};

function ttlSeconds(request) {
  const ttl = Number(request.headers.get("ttl") || "60");
  return Math.max(0, Math.min(Number.isFinite(ttl) ? ttl : 60, 2419200));
}

function rateLimited(key) {
  const now = Date.now();
  const entry = recent.get(key);
  if (!entry || now - entry[0] > 60_000) {
    recent.set(key, [now, 1]);
    if (recent.size > 10_000) recent.clear();
    return false;
  }
  entry[1]++;
  return entry[1] > RATE_PER_MINUTE;
}

async function accessToken(account) {
  const now = Math.floor(Date.now() / 1000);
  if (cachedAccess && cachedAccess.expires - 60 > now) {
    return cachedAccess.token;
  }
  const header = b64url(JSON.stringify({ alg: "RS256", typ: "JWT" }));
  const claims = b64url(JSON.stringify({
    iss: account.client_email,
    scope: FCM_SCOPE,
    aud: TOKEN_URL,
    iat: now,
    exp: now + 3600,
  }));
  const key = await crypto.subtle.importKey(
    "pkcs8",
    pemToDer(account.private_key),
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"],
  );
  const signature = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", key, new TextEncoder().encode(`${header}.${claims}`));
  const response = await fetch(TOKEN_URL, {
    method: "POST",
    headers: { "content-type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: `${header}.${claims}.${b64url(signature)}`,
    }),
  });
  const json = await response.json();
  if (!json.access_token) {
    throw new Error("OAuth failed: " + JSON.stringify(json).slice(0, 200));
  }
  cachedAccess = { token: json.access_token, expires: now + (json.expires_in || 3600) };
  return cachedAccess.token;
}

function pemToDer(pem) {
  const base64 = pem.replace(/-----[^-]+-----/g, "").replace(/\s+/g, "");
  return Uint8Array.from(atob(base64), (c) => c.charCodeAt(0)).buffer;
}

function b64url(input) {
  const bytes = typeof input === "string" ? new TextEncoder().encode(input) : new Uint8Array(input);
  let binary = "";
  for (const b of bytes) binary += String.fromCharCode(b);
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}
