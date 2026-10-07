#!/usr/bin/env python3
"""Генератор пиксель-арт текстур для мода «Чердак Бессмертных».

Все текстуры рисуются процедурно (Pillow), чтобы их можно было пересобрать одной командой:
    python3 tools/make_textures.py
"""
import os
import random

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "chrdk_pantheon")
BLOCK_DIR = os.path.join(ASSETS, "textures", "block")
ITEM_DIR = os.path.join(ASSETS, "textures", "item")

random.seed(7)


def new(w=16, h=16):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))


def px(img, x, y, color):
    img.putpixel((x, y), color)


def fill_noise(img, base, spread=6, alpha=255):
    for y in range(img.height):
        for x in range(img.width):
            d = random.randint(-spread, spread)
            img.putpixel((x, y), (
                max(0, min(255, base[0] + d)),
                max(0, min(255, base[1] + d)),
                max(0, min(255, base[2] + d)),
                alpha,
            ))


def attic_wood():
    img = new()
    fill_noise(img, (74, 53, 36), 5)
    # горизонтальные доски с тёмными швами
    for y in (3, 7, 11, 15):
        for x in range(16):
            px(img, x, y, (46, 32, 22, 255))
    # продольные волокна
    for _ in range(40):
        x, y = random.randrange(16), random.randrange(16)
        shade = random.choice([(86, 62, 42, 255), (60, 42, 28, 255)])
        px(img, x, y, shade)
    # сучки
    for x, y in ((4, 5), (12, 9), (8, 13)):
        px(img, x, y, (40, 27, 18, 255))
        for dx, dy in ((1, 0), (0, 1), (-1, 0), (0, -1)):
            px(img, (x + dx) % 16, (y + dy) % 16, (52, 36, 24, 255))
    return img


def brass():
    img = new()
    for y in range(16):
        t = y / 15
        base = (
            int(196 - 60 * t),
            int(146 - 46 * t),
            int(58 - 22 * t),
        )
        for x in range(16):
            d = random.randint(-5, 5)
            px(img, x, y, (base[0] + d, base[1] + d, base[2] + d, 255))
    # заклёпки
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        px(img, x, y, (120, 84, 30, 255))
        px(img, x, y + 1, (232, 190, 96, 255))
    # потёртости
    for _ in range(12):
        x, y = random.randrange(16), random.randrange(16)
        px(img, x, y, (150, 112, 44, 255))
    for x in range(16):
        px(img, x, 15, (94, 66, 24, 255))
    return img


def marble():
    img = new()
    fill_noise(img, (206, 200, 188), 4)
    # прожилки
    for _ in range(3):
        x, y = random.randrange(16), random.randrange(16)
        for _ in range(random.randint(10, 22)):
            px(img, x % 16, y % 16, (166, 158, 146, 255))
            x += random.choice([-1, 0, 1])
            y += random.choice([0, 1])
    for _ in range(18):
        x, y = random.randrange(16), random.randrange(16)
        px(img, x, y, (226, 222, 213, 255))
    return img


def candle():
    img = new()
    fill_noise(img, (232, 221, 192), 3)
    for x in range(16):
        px(img, x, 0, (246, 238, 214, 255))
    for _ in range(5):
        x = random.randrange(16)
        for y in range(random.randint(2, 5), random.randint(7, 12)):
            px(img, x, y, (214, 200, 166, 255))
    return img


def flame():
    img = new()
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 12.0
            dist = (dx * dx + dy * dy * 0.55) ** 0.5
            t = max(0.0, min(1.0, dist / 9.0))
            r = int(255 - 30 * t)
            g = int(226 - 130 * t)
            b = int(120 - 90 * t)
            px(img, x, y, (max(0, r), max(0, g), max(0, b), 255))
    return img


def tome():
    img = new()
    fill_noise(img, (108, 44, 44), 4)
    for y in range(16):
        px(img, 0, y, (78, 30, 30, 255))
        px(img, 1, y, (146, 108, 54, 255))
        px(img, 2, y, (184, 134, 58, 255))
        px(img, 15, y, (222, 212, 190, 255))
    for x in range(4, 14):
        for y in (4, 8, 12):
            px(img, x, y, (196, 176, 130, 255))
    return img


def parchment():
    img = new()
    fill_noise(img, (217, 201, 163), 5)
    for _ in range(20):
        x = random.randrange(1, 15)
        y = random.randrange(16)
        px(img, x, y, (190, 172, 132, 255))
    for y in (5, 7, 9, 11):
        for x in range(2, 14):
            if random.random() < 0.75:
                px(img, x, y, (142, 122, 92, 255))
    return img


def figure_head():
    """Голова статуэтки: лицо рисуем в левом верхнем углу (эта область и маппится на лицо)."""
    img = attic_wood()
    # 8x5 зона лица (uv [0,0,8,5])
    for x in range(8):
        for y in range(5):
            d = random.randint(-4, 4)
            px(img, x, y, (108 + d, 78 + d, 52 + d, 255))
    # глаза
    px(img, 1, 1, (40, 27, 18, 255))
    px(img, 2, 1, (40, 27, 18, 255))
    px(img, 5, 1, (40, 27, 18, 255))
    px(img, 6, 1, (40, 27, 18, 255))
    # нос и рот
    px(img, 3, 2, (86, 60, 40, 255))
    for x in range(2, 6):
        px(img, x, 3, (86, 60, 40, 255))
    return img


def blank_seal():
    img = new()
    cx, cy, r = 7.5, 7.5, 6.2
    for y in range(16):
        for x in range(16):
            dist = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if dist <= r:
                d = random.randint(-6, 6)
                edge = dist > r - 1.4
                base = (128, 118, 110) if edge else (176, 168, 160)
                px(img, x, y, (base[0] + d, base[1] + d, base[2] + d, 255))
    # ромб-оттиск
    for y in range(16):
        for x in range(16):
            if abs(x - cx) + abs(y - cy) < 2.4:
                px(img, x, y, (206, 200, 194, 255))
    for y in range(16):
        for x in range(16):
            if abs(abs(x - cx) + abs(y - cy) - 3.4) < 0.6:
                px(img, x, y, (110, 100, 92, 255))
    return img


def name_scroll():
    img = new()
    # полотно свитка
    for y in range(3, 13):
        for x in range(3, 13):
            d = random.randint(-5, 5)
            px(img, x, y, (222 + d, 205 + d, 166 + d, 255))
    # валики сверху и снизу
    for y in (2, 13):
        for x in range(3, 13):
            d = random.randint(-4, 4)
            px(img, x, y, (188 + d, 168 + d, 128 + d, 255))
        px(img, 3, y, (150, 130, 96, 255))
        px(img, 12, y, (150, 130, 96, 255))
    # строки текста
    for y in (5, 7, 9, 11):
        for x in range(5, 11):
            if random.random() < 0.8:
                px(img, x, y, (128, 106, 76, 255))
    # лента
    for x in range(3, 13):
        px(img, x, 8 - 0, (0, 0, 0, 0))
    for x in range(2, 14):
        px(img, x, 7, (168, 60, 52, 255))
    for y in range(6, 10):
        px(img, 13, y, (140, 46, 40, 255))
    return img


def icon(size=128):
    img = new(size, size)
    bg = ImageDraw.Draw(img)
    for y in range(size):
        t = y / (size - 1)
        base = (int(38 + 18 * t), int(30 + 14 * t), int(26 + 12 * t))
        for x in range(size):
            d = random.randint(-5, 5)
            img.putpixel((x, y), (base[0] + d, base[1] + d, base[2] + d, 255))
    # доски на стене
    for y in range(0, size, 24):
        bg.line([(0, y), (size, y)], fill=(28, 22, 19, 255), width=2)
    # рама с портретом
    frame = (24, 18, 104, 96)
    bg.rectangle(frame, fill=(184, 134, 58, 255))
    bg.rectangle((frame[0] + 5, frame[1] + 5, frame[2] - 5, frame[3] - 5), fill=(52, 44, 38, 255))
    bg.rectangle((frame[0] + 9, frame[1] + 9, frame[2] - 9, frame[3] - 9), fill=(30, 26, 23, 255))
    # силуэт подписчика
    cx = (frame[0] + frame[2]) // 2
    bg.ellipse((cx - 15, frame[1] + 16, cx + 15, frame[1] + 46), fill=(96, 72, 50, 255))
    bg.rectangle((cx - 24, frame[1] + 44, cx + 24, frame[3] - 10), fill=(96, 72, 50, 255))
    # заклёпки на раме
    for px_, py_ in ((26, 20), (100, 20), (26, 92), (100, 92)):
        bg.ellipse((px_, py_, px_ + 4, py_ + 4), fill=(126, 90, 34, 255))
    # свеча
    bg.rectangle((92, 100, 100, 118), fill=(232, 221, 192, 255))
    bg.ellipse((93, 92, 99, 102), fill=(255, 210, 110, 255))
    # пыль
    for _ in range(70):
        x, y = random.randrange(size), random.randrange(size)
        a = random.randint(40, 130)
        img.putpixel((x, y), (240, 226, 190, a))
    return img


def main():
    os.makedirs(BLOCK_DIR, exist_ok=True)
    os.makedirs(ITEM_DIR, exist_ok=True)

    block_textures = {
        "attic_wood.png": attic_wood(),
        "brass.png": brass(),
        "marble.png": marble(),
        "attic_candle.png": candle(),
        "flame.png": flame(),
        "tome.png": tome(),
        "parchment.png": parchment(),
        "figure_head.png": figure_head(),
    }
    item_textures = {
        "blank_seal.png": blank_seal(),
        "name_scroll.png": name_scroll(),
    }

    for name, image in block_textures.items():
        image.save(os.path.join(BLOCK_DIR, name))
        print("block/" + name)

    for name, image in item_textures.items():
        image.save(os.path.join(ITEM_DIR, name))
        print("item/" + name)

    icon_img = icon()
    icon_img.save(os.path.join(ASSETS, "icon.png"))
    print("icon.png")


if __name__ == "__main__":
    main()
