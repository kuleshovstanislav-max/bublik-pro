#!/usr/bin/env python3
"""Turns Telegram's Night theme into Bublik Graphite.

Usage: make_graphite_theme.py <upstream night.attheme> <output night.attheme>

Neutral greys are moved onto the graphite ramp (#0E0E0E page, #1C1C1E cards),
blue accents are re-hued to #2E9BF0, every other color is kept. Run it again on
the upstream file after each Telegram update.
"""
import colorsys
import sys

ACCENT_HUE = colorsys.rgb_to_hsv(0x2E / 255, 0x9B / 255, 0xF0 / 255)[0]
GRAPHITE_HUE = 240 / 360

# Surfaces with a fixed meaning get exact graphite values.
EXACT = {
    "windowBackgroundGray": 0xFF0E0E0E,
    "windowBackgroundWhite": 0xFF1C1C1E,
    "actionBarDefault": 0xFF0E0E0E,
    "actionBarDefaultArchived": 0xFF0E0E0E,
    "chats_menuBackground": 0xFF1C1C1E,
    "actionBarDefaultSubmenuBackground": 0xFF1C1C1E,
    "chat_topPanelBackground": 0xFF1C1C1E,
    "dialogBackground": 0xFF1C1C1E,
    "chat_messagePanelBackground": 0xFF1C1C1E,
    "chat_inBubble": 0xFF242426,
    "chat_wallpaper": 0xFF0E0E0E,
    "divider": 0xFF0E0E0E,
    # Accent surfaces use the exact Tide Guide blue.
    "chats_actionBackground": 0xFF2E9BF0,
    "chats_unreadCounter": 0xFF2E9BF0,
    "switchTrackChecked": 0xFF2E9BF0,
    "chat_outBubble": 0xFF1E2A38,
    "chat_outBubbleSelected": 0xFF26364A,
    "chat_outSentCheck": 0xFF2E9BF0,
    "chat_outSentCheckRead": 0xFF2E9BF0,
    # Bottom tab bar (keys missing upstream, so Night used to fall back to light-theme defaults).
    "glass_tabSelected": 0xFF2E9BF0,
    "glass_tabSelectedText": 0xFFF2F7FC,
    "glass_tabUnselected": 0xFF8E8E93,
    "glass_targetMainTabs": 0xFF1C1C1E,
    "glass_targetMainTopPanel": 0xFF1C1C1E,
    # Round compose button.
    "featuredStickers_addButton": 0xFF2E9BF0,
    "featuredStickers_addButtonPressed": 0xFF2683CC,
    # Pinned chats already sit in their own card.
    "chats_pinnedOverlay": 0x00000000,
}


def parse(value):
    value = value.strip()
    if value.startswith("#"):
        return int(value[1:], 16) & 0xFFFFFFFF
    return int(value) & 0xFFFFFFFF


def neutral_value(v):
    # Lift the darkest greys a touch (pure black becomes #0E0E0E) and fade the lift out by mid-grey.
    lift = 0.055 * max(0.0, 1.0 - v / 0.5)
    return min(1.0, v + lift)


def remap(argb):
    a = argb >> 24 & 0xFF
    r, g, b = (argb >> 16 & 0xFF) / 255, (argb >> 8 & 0xFF) / 255, (argb & 0xFF) / 255
    h, s, v = colorsys.rgb_to_hsv(r, g, b)
    if s < 0.15:
        h, s, v = GRAPHITE_HUE, min(s, 0.06), neutral_value(v)
    elif 180 / 360 <= h <= 250 / 360:
        h = ACCENT_HUE
    else:
        return argb
    r, g, b = colorsys.hsv_to_rgb(h, s, v)
    return a << 24 | round(r * 255) << 16 | round(g * 255) << 8 | round(b * 255)


def main(src, dst):
    out = []
    changed = 0
    for line in open(src, encoding="utf-8"):
        key, sep, value = line.rstrip("\n").partition("=")
        if not sep:
            out.append(line.rstrip("\n"))
            continue
        old = parse(value)
        new = EXACT.get(key, remap(old))
        changed += new != old
        signed = new - (1 << 32) if new & 0x80000000 else new
        out.append(f"{key}={signed}")
    for key, color in EXACT.items():
        if not any(l.startswith(key + "=") for l in out):
            out.append(f"{key}={color - (1 << 32)}")
            changed += 1
    open(dst, "w", encoding="utf-8").write("\n".join(out) + "\n")
    print(f"{changed} of {len(out)} colors changed")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
