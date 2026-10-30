# Bublik Graphite: компоненти

Токени: `tokens.json` (контраст: 17 пар, 0 помилок AA). Шрифт: Google Sans Flex із системи Pixel, інакше шрифт Telegram.
Іконки: Phosphor Duotone (MIT), згенеровані `Tools/bublik/make_phosphor_drawables.py`. Екрани S01–S16 — з `../recon.md`.

## Наявні (вже в застосунку)

```
Pill tab bar                     MainTabsActivity, GlassTabView
  variants  5 вкладок: Чати, Контакти, Виклики, Налаштування, Профіль (аватар)
  states    idle (duotone, text-faint), selected (fill, капсула accent 16%), badge, long-press меню
  tokens    bg #121214 @ 88%, hairline white 14%, radius pill, label caption
  motion    spring на іконці при виборі
  a11y      contentDescription = назва вкладки
  used on   S01–S05

Compose button                   DialogsActivity.floatingButton3
  tokens    accent, іконка compose fill on-accent, 56dp коло поруч з таббаром
  used on   S01

Folder pills                     FilterTabsView.setBublikPills
  states    idle (surface, text-muted), selected (accent, on-accent), counter
  tokens    radius 14, висота 28
  used on   S01

Section header                   BublikSectionHeaderCell
  variants  Закріплені (pin), Усі чати (chats)
  tokens    label 13/600 uppercase, text-muted
  used on   S01

Cookie avatar                    BublikShapes + ImageReceiver.bublikCookie + AvatarDrawable.bublikCookie
  variants  фото, ініціали, saved / replies / archive
  states    кільце непрочитаних (accent 2dp, відступ 3.5dp), кільце історій (Telegram)
  used on   S01, S02, S06, S12, S14 (+ S05 профіль і S03 контакти — у роботі)

Bubble                           MessageDrawable.generatePath
  variants  in (surface-raised), out (surface-out)
  states    згрупована (тісні кути до сусідів), з кнопками бота
  tokens    radius 22, без хвостика, «прочитано» accent
  used on   S06

Dense glass panel                BlurredBackgroundProviderImpl
  variants  таббар, поле вводу, шапка чату, меню
  tokens    surface @ 92–96%, hairline white 14%
  used on   S01, S06

Recording timer                  ChatActivityEnterView.TimerView
  states    звичайний (text), поза лімітом на фіксованому бітрейті (warning #FF9F0A)
  tokens    timer 15/500, tabular
  used on   S07
```

## Нові (макет: `design_refs/mockup_components.html`, чекають вибору)

```
Cloud progress tab               GlassTabView (GOOGLE_PHOTOS) + ChatAttachAlertPhotoLayout
  мета      показати копіювання з Google Фото (F03)
  states    idle; копіювання: силует хмарки обводиться accent 0→100 % (PathMeasure по силуету Phosphor),
            підпис «Фото» або «NN%» (варіант A/B); помилка: тост, контур гасне
  tokens    stroke accent 18/256 viewport, duotone фон text-faint
  a11y      contentDescription «Фото, завантаження NN%», оновлюється кожні 10 %
  used on   S09

Auto bitrate indicator           ChatActivityEnterView.TimerView
  мета      показати, що «Авто» знижує бітрейт (F01)
  варіанти  A чип «АВТО 1.4» (accent 16 %, caption 11/700)
            B іконка trend-down accent 18dp
            C кільце бюджету довкола камери 4dp, 0–11.8 МБ
  states    прихований, поки бітрейт = 2 Мбіт/с; видимий, коли нижче
  used on   S07

Settings card                    Settings / ProfileActivity
  мета      налаштування як картки списку чатів (S04)
  варіанти  A іконка accent без плашки; B біла іконка на кольоровій плашці 30dp r10; C світла на #2C2C2E
  tokens    surface, radius 24, рядок 48dp, роздільник hairline з відступом 56dp, label-заголовки груп
  states    pressed (surface-raised), disabled (text-faint)
  used on   S04, S05, S13

Screen transition                ActionBarLayout
  варіанти  A глибина: новий зсувається, старий scale 0.92 + затемнення
            B спільна вісь: обидва ±30dp + fade
            C fade-through: старий зникає, новий scale 0.92→1
            D як у Telegram (зсув з паралаксом)
  motion    base 220–380 ms, ease (.2,.8,.2,1); жест «назад» тягне ту саму анімацію
  used on   усі переходи
```
