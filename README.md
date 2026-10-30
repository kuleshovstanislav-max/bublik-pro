# Bublik Pro 🥯

**HQ round videos for Telegram, tuned for the Pixel 11 Pro series.**

Bublik Pro is an *unofficial* Telegram client: a small fork of
[Telegram for Android](https://github.com/DrKLO/Telegram) (GPL v2) that records round video
messages ("circles") at up to **640×640, H.264 High, 6 Mbps**. The stock app records 384–480p
at about 1 Mbps.

> Not affiliated with Telegram FZ-LLC or Google. "Telegram" and "Pixel" are trademarks of their owners.

## What's different

| | Telegram (Camera2 recorder) | Bublik Pro |
|---|---|---|
| Output resolution | 360p / 480p | 360p / 480p / **640p** (Telegram's maximum for circles) |
| Video bitrate | 0.75 – 1.2 Mbps | **Auto** (default: up to 2 Mbps, fits any length) or fixed 1.2 / 2 / 4 / **6 Mbps** |
| H.264 profile | encoder default | **High** when the encoder supports it, automatic fallback otherwise |
| Rate control | encoder default | VBR when supported, bitrate clamped to the encoder's range |
| Camera source for 640p | — | 1080p crop, downscaled |

Settings → **Experimental → Round Video** lets you choose resolution, camera source, 30/60 fps and bitrate.

> **The 12 MB limit.** Telegram's server turns a round video bigger than about 12 MiB into an ordinary
> (square) video. We measured it: a 15 s circle at 6 Mbps (≈11.9 MB) arrives round, 16 s (12.6 MB) arrives square.
> So a fixed bitrate only fits this long: 6 Mbps ≈ 15 s, 4 Mbps ≈ 23 s, 2 Mbps ≈ 45 s, 1.2 Mbps the full minute.
>
> **Auto** (the default since 13.0.2-3) starts at 2 Mbps and lowers the bitrate while recording so the
> file always stays under 11.8 MB: short circles keep 2 Mbps, a full minute slides smoothly to about 1.1 Mbps
> (≈1.6 Mbps on average, still above the official app). With a fixed bitrate the recording timer turns
> orange once the circle no longer fits.

Recipients get the file **byte-for-byte as recorded**: the client does not re-encode round videos,
and we verified that the copy downloaded from Telegram's servers by the official app has the same SHA-256.

Measured on a Pixel 11 Pro XL (Android 17), with hardware encoder `c2.google.avc.encoder`:
640×640, High profile, level 3.1, 30 fps, about 6.2 Mbps at the 6 Mbps setting, a keyframe every second.

> **Why not 720p?** Telegram accepts video messages up to 640×640. Bigger squares lose the
> "round" flag on the server and arrive as ordinary videos (TDLib documents this limit too:
> `length … not greater than 640`). Release 12.10.6-1 offered 720p by mistake. Update to 12.10.6-2 or newer.

### Side by side on the same Pixel 11 Pro XL

Measured on circles recorded in the same chat, read from the copies downloaded from Telegram's servers:

| | Telegram (official) | Bublik Pro, 6 Mbps |
|---|---|---|
| Resolution | 480×480 | **640×640** |
| Pixels per frame | 230 k | 410 k (×1.78) |
| Codec | H.264 High, level 3.0 | H.264 High, level 3.1 |
| Frame rate, keyframes | 30 fps, every 1 s | 30 fps, every 1 s |
| Bitrate (incl. audio) | 1.32 Mbps | 6.2 Mbps (×4.7) |
| Bits per pixel per frame | 0.19 | 0.50 |
| Size of 10 s | ~1.7 MB | ~7.8 MB |

The Pixel encoder already picks High profile for the official app. The gain comes from 1.78× more pixels
and about 2.6× more bits per pixel, which means more detail and fewer compression artifacts.
The 4 Mbps setting gives about 0.33 bits per pixel (not yet re-measured at 640p).

## Bublik Graphite design

Since 13.0.2-2 the whole app uses its own dark look, inspired by Android 17 on the Pixel and top 2026 apps:

- **Graphite theme** (`#0E0E0E` / `#1C1C1E`) with one light-blue accent `#2E9BF0`, set as the default (still changeable in Telegram's theme settings)
- **Google Sans Flex** from the phone's system fonts (not bundled; other phones keep Telegram's font)
- **Phosphor Duotone icons** ([phosphoricons.com](https://phosphoricons.com), MIT) with a spring animation in a floating bottom bar, plus a separate round "New message" button
- **Calls tab** in the bottom bar, like on iPhone
- Chat list as rounded cards with "Pinned" / "All chats" labels, folder pills, a big "Chats" title, stories behind one button
- 9-wave "cookie" avatars with a blue ring on chats with unread messages
- Chats: plain graphite background, tail-less bubbles, Phosphor icons in the header, input bar, message menu, call messages and attach menu
- **Photos** tab in the attach menu: opens the system photo picker, which includes your **Google Photos** library (Telegram's own gallery only sees photos stored on the phone); picked photos open in the editor before sending
- New app icon

## Limitations, please read

- **Push notifications don't work.** Telegram's FCM project only serves official builds.
  Messages arrive while the app is open or connected in the background.
- Google sign-in and passkeys are disabled (they only work with official app IDs).
- arm64 only. Tested on the Pixel 11 Pro XL; the Pixel 11 Pro has the same SoC and should behave the same.
  Other phones may work too, since everything falls back to safe defaults, but nobody has tested them.
- Fixed 4 and 6 Mbps circles longer than about 23 s / 15 s arrive as square videos (Telegram's size limit); use Auto for long ones.
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

- `utils/recorder/round/RoundVideoSession.java`: 640p output size
- `utils/recorder/round/RoundVideoCameraController.java`: source crop for HQ sizes
- `utils/recorder/round/RoundVideoCodecRecorder.java`: High profile and level, VBR, bitrate clamping, fallback
- `ui/RoundVideoSettingsActivity.java`: new choices incl. Auto (and a fix for a `null` bitrate label in release builds)
- `RoundVideoCodecRecorder.java` / `RoundVideoMp4Writer.java`: Auto bitrate that keeps the file under Telegram's round size limit
- `utils/settings/SharedSettings.java`: HQ defaults, Round Video settings visible in release
- `messenger/BuildVars.java`, Gradle files: API credentials from local config, own package and signing, no update checks
- Design: `ActionBar/Theme.java`, `assets/night.attheme` (generated by `Tools/bublik/make_graphite_theme.py`), Phosphor drawables (generated by `Tools/bublik/make_phosphor_drawables.py`, license in `Tools/bublik/licenses/`), `Components/BublikShapes.java`, `Cells/BublikSectionHeaderCell.java`, chat list, tab bar, bubbles and attach menu classes

## Українською

**Bublik Pro** — неофіційний клієнт Telegram, який записує відеокружки у якості до 640×640 (максимум, який Telegram приймає для кружків),
H.264 High, 6 Мбіт/с. Налаштований під Pixel 11 Pro та Pro XL. Ставиться поруч з офіційним Telegram.
Push-сповіщення не працюють: це обмеження всіх неофіційних збірок без власного Firebase.

З версії 13.0.2-2 — власний дизайн «Bublik Graphite»: графітова тема з блакитним акцентом, шрифт Google Sans Flex, іконки Phosphor Duotone,
вкладка «Виклики» внизу, чати картками, аватарки-«печиво», бульбашки без хвостика та вкладка «Фото» в меню скріпки з доступом до Google Фото.

З версії 13.0.2-3 бітрейт за замовчуванням — «Авто»: сервер Telegram перетворює кружок більший за ~12 МБ на звичайне квадратне відео,
тому Bublik починає з 2 Мбіт/с і плавно знижує бітрейт під час запису, щоб навіть хвилинний кружок лишався кружком.
На фіксованому бітрейті таймер запису стає помаранчевим, коли кружок уже не влазить у ліміт.

## License

GPL v2, same as upstream. See [LICENSE](LICENSE). Upstream README: [README_UPSTREAM.md](README_UPSTREAM.md).
