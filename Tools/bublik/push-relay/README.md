# Bublik push relay

Telegram's server can only send FCM pushes to its own Firebase project, but it can send
**Web Push** (`account.registerDevice`, `token_type = 10`) to any HTTPS endpoint. Bublik registers
`https://<relay>/p/<FCM token>` there. This Worker turns each Web Push into a high-priority FCM data
message `{"bublik": "1"}` for our own Firebase project. The app wakes up, connects to Telegram and
shows notifications from the updates it fetches.

The encrypted Web Push payload is dropped, never forwarded or logged. The relay only learns that a
push arrived for some FCM token and when.

## Setup

1. Firebase Console: create a project, add an Android app with package `app.bublik.pro`,
   download `google-services.json` into `TMessagesProj_App/` (replaces the upstream one).
2. Project settings → Service accounts → Generate new private key (JSON). Keep it out of git.
3. Cloudflare:
   ```
   npm i -g wrangler
   wrangler login
   cd Tools/bublik/push-relay
   wrangler secret put FCM_SERVICE_ACCOUNT < /path/to/service-account.json
   wrangler deploy
   ```
4. Put the Worker URL in `local.properties` (or the `BUBLIK_PUSH_RELAY` env var) and rebuild:
   ```
   BUBLIK_PUSH_RELAY=https://bublik-push.<your-subdomain>.workers.dev
   ```

Without `BUBLIK_PUSH_RELAY` the app behaves like before (no push) and the settings switch is hidden.

## Limits

- Cloudflare Workers free plan: 100,000 requests a day. Each Telegram push is one request.
- FCM: free, no message quota for this use.
- 60 pushes per minute per device token (per Worker isolate); more get HTTP 429.
- An FCM `UNREGISTERED` answer returns 410, so Telegram stops using that endpoint.
