#!/usr/bin/env python3
"""Генератор пиксель-арт ресурсов для мода «Чердак Бессмертных».

Рисует:
  * текстуры блоков и предметов (assets/chrdk_pantheon/textures/...);
  * иконку мода;
  * набор процедурных скинов подписчиков (assets/chrdk_pantheon/textures/figurine/skin_XX.png)
    и превью-лист docs/figurine_preview.png.

Процедурные скины — обычные скины Minecraft 64×64 в современной разметке, поэтому
подписчики без аккаунта получают свой собственный, но «майнкрафтовый» вид.

Запуск:  python3 tools/make_textures.py
"""
import os
import random

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "chrdk_pantheon")
BLOCK_DIR = os.path.join(ASSETS, "textures", "block")
ITEM_DIR = os.path.join(ASSETS, "textures", "item")
FIGURINE_DIR = os.path.join(ASSETS, "textures", "figurine")
DOCS_DIR = os.path.join(ROOT, "docs")

SKIN_COUNT = 48

SKIN_TONES = [
    (247, 205, 176), (236, 188, 152), (222, 170, 130), (198, 140, 100),
    (168, 116, 78), (134, 88, 56), (104, 68, 44), (78, 50, 34),
]
HAIR_COLORS = [
    (38, 30, 26), (62, 42, 30), (96, 62, 34), (132, 88, 44), (176, 132, 62),
    (206, 178, 106), (150, 148, 146), (208, 206, 202), (120, 60, 44), (58, 56, 74),
]
SHIRTS = [
    (186, 62, 62), (62, 108, 186), (74, 148, 92), (198, 160, 62), (140, 84, 178),
    (58, 58, 70), (222, 222, 222), (44, 138, 148), (206, 112, 48), (110, 112, 122),
    (94, 132, 168), (168, 78, 120),
]
PANTS = [(62, 70, 108), (72, 72, 72), (110, 92, 62), (44, 82, 62), (92, 62, 92), (52, 60, 76)]
SHOES = [(52, 42, 36), (30, 30, 32), (86, 62, 42), (24, 24, 28)]
EYES = [(70, 92, 150), (78, 108, 62), (108, 74, 44), (56, 56, 64), (86, 120, 140)]
CAP_COLORS = [(168, 60, 52), (58, 76, 132), (52, 96, 62), (222, 214, 196), (58, 58, 64)]
CAPS = [color * 1 for color in CAP_COLORS]


def jitter(color, rng, amount=6):
    return (
        max(0, min(255, color[0] + rng.randint(-amount, amount))),
        max(0, min(255, color[1] + rng.randint(-amount, amount))),
        max(0, min(255, color[2] + rng.randint(-amount, amount))),
        255,
    )


def shade(color, factor):
    return (
        max(0, min(255, int(color[0] * factor))),
        max(0, min(255, int(color[1] * factor))),
        max(0, min(255, int(color[2] * factor))),
        255,
    )


# --------------------------------------------------------------------------
# базовые текстуры блоков и предметов
# --------------------------------------------------------------------------

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
    for y in (3, 7, 11, 15):
        for x in range(16):
            px(img, x, y, (46, 32, 22, 255))
    for _ in range(40):
        x, y = random.randrange(16), random.randrange(16)
        shade_color = random.choice([(86, 62, 42, 255), (60, 42, 28, 255)])
        px(img, x, y, shade_color)
    for x, y in ((4, 5), (12, 9), (8, 13)):
        px(img, x, y, (40, 27, 18, 255))
        for dx, dy in ((1, 0), (0, 1), (-1, 0), (0, -1)):
            px(img, (x + dx) % 16, (y + dy) % 16, (52, 36, 24, 255))
    return img


def brass():
    img = new()
    for y in range(16):
        t = y / 15
        base = (int(196 - 60 * t), int(146 - 46 * t), int(58 - 22 * t))
        for x in range(16):
            d = random.randint(-5, 5)
            px(img, x, y, (base[0] + d, base[1] + d, base[2] + d, 255))
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        px(img, x, y, (120, 84, 30, 255))
        px(img, x, y + 1, (232, 190, 96, 255))
    for _ in range(12):
        x, y = random.randrange(16), random.randrange(16)
        px(img, x, y, (150, 112, 44, 255))
    for x in range(16):
        px(img, x, 15, (94, 66, 24, 255))
    return img


def marble():
    img = new()
    fill_noise(img, (206, 200, 188), 4)
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
            px(img, x, y, (max(0, int(255 - 30 * t)), max(0, int(226 - 130 * t)), max(0, int(120 - 90 * t)), 255))
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
        x, y = random.randrange(1, 15), random.randrange(16)
        px(img, x, y, (190, 172, 132, 255))
    for y in (5, 7, 9, 11):
        for x in range(2, 14):
            if random.random() < 0.75:
                px(img, x, y, (142, 122, 92, 255))
    return img


def attic_chest_texture():
    """Ларь подписчика: тёмные доски, латунные полосы и замок.
    Разметка: y 0..4 — крышка, y 5..15 — корпус."""
    rng = random.Random(0xC4E5)
    img = new()
    draw = ImageDraw.Draw(img)
    wood = (86, 58, 36)
    dark = shade(wood, 0.7)
    fill_noise(img, wood, 9)

    # вертикальные стыки досок (по всему корпусу)
    for x in (5, 10):
        draw.line((x, 0, x, 15), fill=dark)

    # латунные полосы: на крышке и по низу корпуса
    band = (178, 138, 62)
    for y in (0, 1, 13, 14):
        for x in range(16):
            px(img, x, y, jitter(band, rng, 12))

    # тень под крышкой
    for x in range(16):
        px(img, x, 4, shade(wood, 0.55))

    # замок
    plate = (196, 156, 74)
    draw.rectangle((6, 7, 9, 12), fill=jitter(plate, rng, 10))
    draw.rectangle((7, 9, 8, 11), fill=shade(plate, 0.45))
    px(img, 7, 9, (250, 226, 150))
    px(img, 8, 9, (250, 226, 150))

    return img


def attic_dust():
    """Слой вековой пыли: серо-бежевые хлопья."""
    rng = random.Random(0xD057)
    img = new()
    base = (168, 158, 138)
    fill_noise(img, base, 10)

    for _ in range(90):
        x, y = rng.randrange(16), rng.randrange(16)
        tone = rng.choice([(198, 190, 172), (140, 130, 112), (214, 208, 192)])
        px(img, x, y, tone)

    return img


def attic_web_texture():
    """Чердачная паутина: бледные нити по прозрачному фону (вырезной слой)."""
    rng = random.Random(0xE8)
    img = new()
    thread = (222, 222, 214, 255)
    shadow = (168, 170, 166, 255)
    draw = ImageDraw.Draw(img)

    # радиальные нити из угла и пара дуг
    for target in ((15, 2), (15, 8), (15, 14), (9, 14), (2, 15), (0, 9), (0, 3), (5, 0)):
        draw.line((0, 0, target[0], target[1]), fill=thread)

    for radius in (5, 9, 13):
        draw.arc((-radius, -radius, radius, radius), start=0, end=90, fill=shadow)
        draw.arc((-radius + 1, -radius + 1, radius + 1, radius + 1), start=0, end=90, fill=thread)

    # редкие подвески
    for _ in range(12):
        x, y = rng.randrange(16), rng.randrange(16)
        px(img, x, y, thread)

    return img


def attic_glass_texture():
    """Витраж с гербом канала: цветные стёкла в свинцовой оплётке."""
    rng = random.Random(0x61A5)
    img = new()
    draw = ImageDraw.Draw(img)
    palettes = [(86, 62, 132), (52, 96, 150), (168, 62, 62), (196, 154, 62), (62, 120, 88)]

    cell = 5

    for cy in range(0, 16, cell):
        for cx in range(0, 16, cell):
            color = palettes[rng.randrange(len(palettes))]
            for y in range(cy, min(16, cy + cell - 1)):
                for x in range(cx, min(16, cx + cell - 1)):
                    px(img, x, y, (color[0], color[1], color[2], 190))

    # свинцовая оплётка
    for i in range(0, 16, cell):
        draw.line((i, 0, i, 15), fill=(48, 46, 52, 255))
        draw.line((0, i, 15, i), fill=(48, 46, 52, 255))

    draw.rectangle((0, 0, 15, 15), outline=(48, 46, 52, 255))

    # герб: латинская «C» под короной
    emblem_dark = (40, 34, 30, 255)
    for y in range(4, 13):
        px(img, 5, y, emblem_dark)
    for x in range(5, 12):
        px(img, x, 4, emblem_dark)
        px(img, x, 12, emblem_dark)
    for x in range(6, 11):
        px(img, x, 2, (250, 230, 160, 255))
    px(img, 5, 3, (250, 230, 160, 255))
    px(img, 10, 3, (250, 230, 160, 255))

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
    for y in range(3, 13):
        for x in range(3, 13):
            d = random.randint(-5, 5)
            px(img, x, y, (222 + d, 205 + d, 166 + d, 255))
    for y in (2, 13):
        for x in range(3, 13):
            d = random.randint(-4, 4)
            px(img, x, y, (188 + d, 168 + d, 128 + d, 255))
        px(img, 3, y, (150, 130, 96, 255))
        px(img, 12, y, (150, 130, 96, 255))
    for y in (5, 7, 9, 11):
        for x in range(5, 11):
            if random.random() < 0.8:
                px(img, x, y, (128, 106, 76, 255))
    for x in range(2, 14):
        px(img, x, 7, (168, 60, 52, 255))
    for y in range(6, 10):
        px(img, 13, y, (140, 46, 40, 255))
    return img


def attic_chisel():
    """Чердачный резец: рукоять из тёмного дерева, латунная обойма, стальное лезвие."""
    img = new()
    # рукоять — диагональ в нижний левый угол
    for y in range(16):
        for x in range(16):
            if x <= 6 and 14 <= x + y <= 16:
                d = random.randint(-6, 6)
                px(img, x, y, (98 + d, 68 + d, 42 + d, 255))
    # латунная обойма между рукоятью и лезвием
    for y in range(16):
        for x in range(16):
            if 7 <= x <= 8 and 14 <= x + y <= 16:
                d = random.randint(-6, 6)
                px(img, x, y, (190 + d, 158 + d, 86 + d, 255))
    # стальное лезвие — диагональ в верхний правый угол
    for y in range(16):
        for x in range(16):
            if 9 <= x <= 15 and 14 <= x + y <= 16:
                d = random.randint(-8, 8)
                px(img, x, y, (170 + d, 174 + d, 180 + d, 255))
    # острая кромка на кончике
    for y in range(0, 3):
        px(img, 15, y, (216, 220, 226, 255))
    px(img, 14, 1, (200, 204, 210, 255))
    # торец рукояти
    px(img, 0, 15, (74, 50, 30, 255))
    px(img, 1, 15, (74, 50, 30, 255))
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
    for y in range(0, size, 24):
        bg.line([(0, y), (size, y)], fill=(28, 22, 19, 255), width=2)
    frame = (24, 18, 104, 96)
    bg.rectangle(frame, fill=(184, 134, 58, 255))
    bg.rectangle((frame[0] + 5, frame[1] + 5, frame[2] - 5, frame[3] - 5), fill=(52, 44, 38, 255))
    bg.rectangle((frame[0] + 9, frame[1] + 9, frame[2] - 9, frame[3] - 9), fill=(30, 26, 23, 255))
    cx = (frame[0] + frame[2]) // 2
    bg.ellipse((cx - 15, frame[1] + 16, cx + 15, frame[1] + 46), fill=(96, 72, 50, 255))
    bg.rectangle((cx - 24, frame[1] + 44, cx + 24, frame[3] - 10), fill=(96, 72, 50, 255))
    for px_, py_ in ((26, 20), (100, 20), (26, 92), (100, 92)):
        bg.ellipse((px_, py_, px_ + 4, py_ + 4), fill=(126, 90, 34, 255))
    bg.rectangle((92, 100, 100, 118), fill=(232, 221, 192, 255))
    bg.ellipse((93, 92, 99, 102), fill=(255, 210, 110, 255))
    for _ in range(70):
        x, y = random.randrange(size), random.randrange(size)
        img.putpixel((x, y), (240, 226, 190, random.randint(40, 130)))
    return img


# --------------------------------------------------------------------------
# процедурные скины подписчиков (64×64, современная разметка скинов Minecraft)
# --------------------------------------------------------------------------

def fill_rect(img, x, y, w, h, color_fn):
    for j in range(h):
        for i in range(w):
            img.putpixel((x + i, y + j), color_fn(i, j))


def skin_box(img, u, v, w, h, d, face_fn):
    """Разворачивает коробку w×h×d в текстуру так же, как это делает Minecraft."""
    fill_rect(img, u + d, v, w, d, lambda i, j: face_fn("top", i, j))
    fill_rect(img, u + d + w, v, w, d, lambda i, j: face_fn("bottom", i, j))
    fill_rect(img, u, v + d, d, h, lambda i, j: face_fn("right", i, j))
    fill_rect(img, u + d, v + d, w, h, lambda i, j: face_fn("front", i, j))
    fill_rect(img, u + d + w, v + d, d, h, lambda i, j: face_fn("left", i, j))
    fill_rect(img, u + d + w + d, v + d, w, h, lambda i, j: face_fn("back", i, j))


HEAD_UV = (0, 0, 8, 8, 8)
HAT_UV = (32, 0, 8, 8, 8)
BODY_UV = (16, 16, 8, 12, 4)
JACKET_UV = (16, 32, 8, 12, 4)
RIGHT_ARM_UV = (40, 16, 4, 12, 4)
RIGHT_SLEEVE_UV = (40, 32, 4, 12, 4)
LEFT_ARM_UV = (32, 48, 4, 12, 4)
LEFT_SLEEVE_UV = (48, 48, 4, 12, 4)
RIGHT_LEG_UV = (0, 16, 4, 12, 4)
RIGHT_PANTS_UV = (0, 32, 4, 12, 4)
LEFT_LEG_UV = (16, 48, 4, 12, 4)
LEFT_PANTS_UV = (0, 48, 4, 12, 4)


def make_skin(index):
    """Один процедурный скин: всегда одинаковый для одного и того же индекса."""
    rng = random.Random(90210 + index * 7919)

    tone = rng.choice(SKIN_TONES)
    hair = rng.choice(HAIR_COLORS)
    eye = rng.choice(EYES)
    shirt = rng.choice(SHIRTS)
    sleeve = shade(shirt, rng.uniform(0.85, 1.05))
    pants = rng.choice(PANTS)
    shoes = rng.choice(SHOES)

    hair_style = rng.randrange(5)              # 0 — лысый, 1 — короткая стрижка, 2 — с чёлкой,
    beard = rng.random() < 0.25                # 3 — ирокез, 4 — длинные волосы
    hat_kind = 0 if rng.random() > 0.18 else (1 if rng.random() < 0.5 else 2)  # кепка или шапка
    cap = rng.choice(CAPS)
    stripes = rng.random() < 0.3
    emblem = rng.random() < 0.25
    emblem_color = rng.choice(CAPS)

    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

    # --- голова ---
    def head_face(face, x, y):
        color = tone

        if face in ("top", "back"):
            if hair_style >= 1:
                color = hair
        elif face in ("right", "left"):
            if hair_style == 2 and y <= 3:
                color = hair
            elif hair_style == 3 and x in (1, 2):
                color = hair
            elif hair_style == 4 and y <= 6:
                color = hair
        elif face == "front":
            if hair_style == 1 and y == 0:
                color = hair
            elif hair_style >= 2 and y <= 1:
                color = hair
            if hair_style == 3 and 2 <= x <= 5 and y == 0:
                color = hair

        if face == "top" and hair_style == 3:
            color = hair if 2 <= x <= 5 else tone

        return jitter(color, rng)

    skin_box(img, *HEAD_UV, head_face)

    # лицо: глаза, нос, рот (координаты внутри лицевой грани 8×8)
    fx, fy = HEAD_UV[0] + HEAD_UV[3], HEAD_UV[1] + HEAD_UV[3]
    if hair_style == 0:
        for x in range(8):
            img.putpixel((fx + x, fy), jitter(shade(tone, 1.05), rng, 3))  # лысина чуть светлее

    for ex in (1, 5):
        img.putpixel((fx + ex, fy + 3), (246, 246, 246, 255))
        img.putpixel((fx + ex + 1, fy + 3), eye)

    img.putpixel((fx + 3, fy + 4), shade(tone, 0.88))
    img.putpixel((fx + 4, fy + 4), shade(tone, 0.88))
    img.putpixel((fx + 3, fy + 5), shade(tone, 0.7))
    img.putpixel((fx + 4, fy + 5), shade(tone, 0.7))

    if hair_style >= 1:
        for x in range(8):
            img.putpixel((fx + x, fy + 2), jitter(hair, rng, 4))

    if beard:
        for x in range(1, 7):
            img.putpixel((fx + x, fy + 7), jitter(hair, rng, 4))
        for x in range(2, 6):
            img.putpixel((fx + x, fy + 6), jitter(hair, rng, 4))
        img.putpixel((fx + 2, fy + 5), jitter(hair, rng, 4))
        img.putpixel((fx + 5, fy + 5), jitter(hair, rng, 4))

    # --- шапка (слой шляпы) ---
    if hat_kind == 1:
        def cap_face(face, x, y):
            if face == "bottom":
                return (0, 0, 0, 0)
            if face == "front" and y > 1:
                return (0, 0, 0, 0)          # открытое лицо
            if face in ("right", "left") and y > 2:
                return (0, 0, 0, 0)
            if face == "top":
                return jitter(shade(cap, 0.85), rng, 4)
            return jitter(cap, rng, 4)

        skin_box(img, *HAT_UV, cap_face)
        # козырёк
        for x in range(8):
            img.putpixel((HAT_UV[0] + HAT_UV[3] + x, HAT_UV[1] + HAT_UV[3] + 1), shade(cap, 0.7))
    elif hat_kind == 2:
        def beanie_face(face, x, y):
            if face == "bottom":
                return (0, 0, 0, 0)
            if face == "front" and y > 1:
                return (0, 0, 0, 0)
            if face in ("right", "left") and y > 3:
                return (0, 0, 0, 0)
            if face == "back" and y > 5:
                return (0, 0, 0, 0)
            if face == "front" and y == 1:
                return jitter(shade(cap, 0.75), rng, 4)
            return jitter(cap, rng, 4)

        skin_box(img, *HAT_UV, beanie_face)
    elif hair_style in (2, 3, 4):
        # волосы «потолще»: тот же рисунок на слое шляпы
        def hair_overlay(face, x, y):
            if face in ("top", "back"):
                return jitter(hair, rng, 5)
            if face in ("right", "left"):
                if hair_style == 2 and y <= 3:
                    return jitter(hair, rng, 5)
                if hair_style == 3 and x in (1, 2):
                    return jitter(hair, rng, 5)
                if hair_style == 4 and y <= 6:
                    return jitter(hair, rng, 5)
            if face == "front":
                if hair_style == 4 and y <= 1:
                    return jitter(hair, rng, 5)
                if hair_style >= 2 and y == 0:
                    return jitter(hair, rng, 5)
                if hair_style == 3 and 2 <= x <= 5 and y == 0:
                    return jitter(hair, rng, 5)
            return (0, 0, 0, 0)

        skin_box(img, *HAT_UV, hair_overlay)

    # --- туловище ---
    def body_face(face, x, y):
        color = shirt
        if face == "front":
            if y == 0:
                color = shade(shirt, 0.75)            # воротник
            if stripes and y % 3 == 1:
                color = shade(shirt, 0.85)
        if face == "back" and stripes and y % 3 == 1:
            color = shade(shirt, 0.85)
        if face == "top":
            color = shade(shirt, 0.9)                 # плечи
        return jitter(color, rng, 5)

    skin_box(img, *BODY_UV, body_face)

    if emblem:
        bx, by = BODY_UV[0] + BODY_UV[3], BODY_UV[1] + BODY_UV[3]
        for x in range(3, 5):
            for y in range(4, 7):
                img.putpixel((bx + x, by + y), shade(emblem_color, 1.1 if (x + y) % 2 else 0.85))

    if rng.random() < 0.35:
        # жилет поверх рубашки
        def jacket_face(face, x, y):
            if face == "front" and 2 <= x <= 5 and 2 <= y <= 10:
                return (0, 0, 0, 0)                   # рубашка видна спереди
            if face == "bottom" or y == 0:
                return (0, 0, 0, 0)
            return jitter(shade(shirt, 0.62), rng, 4)

        skin_box(img, *JACKET_UV, jacket_face)

    # --- руки: рукав сверху, открытая кисть снизу ---
    def make_arm_fn(sleeve_len):
        def arm_face(face, x, y):
            if y < sleeve_len:
                return jitter(sleeve, rng, 5)
            if y >= 9:
                return jitter(tone, rng, 5)
            return jitter(sleeve, rng, 5)

        return arm_face

    skin_box(img, *RIGHT_ARM_UV, make_arm_fn(7))
    skin_box(img, *LEFT_ARM_UV, make_arm_fn(7))

    # --- ноги: штаны и обувь ---
    def leg_face(face, x, y):
        if y >= 10:
            return jitter(shoes, rng, 4)
        return jitter(pants, rng, 5)

    skin_box(img, *RIGHT_LEG_UV, leg_face)
    skin_box(img, *LEFT_LEG_UV, leg_face)

    # штанины-оверлеи: чуть темнее, чтобы ноги читались
    def pants_overlay(face, x, y):
        if y >= 10:
            return jitter(shade(shoes, 0.9), rng, 3)
        return jitter(shade(pants, 0.92), rng, 4)

    skin_box(img, *RIGHT_PANTS_UV, pants_overlay)
    skin_box(img, *LEFT_PANTS_UV, pants_overlay)

    return img


def make_preview(skins, path, scale=2):
    """Превью-лист со всеми процедурными скинами (для проверки глазами)."""
    columns = 8
    rows = (len(skins) + columns - 1) // columns
    cell_w, cell_h = 64 * scale, 64 * scale
    sheet = Image.new("RGBA", (columns * cell_w, rows * cell_h), (28, 24, 22, 255))
    draw = ImageDraw.Draw(sheet)

    for index, skin in enumerate(skins):
        col, row = index % columns, index // columns
        x, y = col * cell_w, row * cell_h
        big = skin.resize((cell_w, cell_h), Image.NEAREST)
        sheet.alpha_composite(big, (x, y))
        draw.rectangle((x, y, x + cell_w - 1, y + cell_h - 1), outline=(70, 60, 52, 255))

    sheet.save(path)
    return sheet


def make_figure_sheet(skins, path, scale=3):
    """Собирает «бумажных» человечков (вид спереди) из развёрток скинов — для проверки глазами.

    Слои-оверлеи (волосы, шапки, рукава, штанины) накладываются поверх базовых,
    ровно как это делает сама игра.
    """
    base_parts = [
        (8, 8, 8, 8, 4, 0),      # голова
        (20, 20, 8, 12, 4, 8),   # туловище
        (44, 20, 4, 12, 0, 8),   # правая рука
        (36, 52, 4, 12, 12, 8),  # левая рука
        (4, 20, 4, 12, 4, 20),   # правая нога
        (20, 52, 4, 12, 8, 20),  # левая нога
    ]
    overlay_parts = [
        (40, 8, 8, 8, 4, 0),     # волосы/шапка
        (20, 36, 8, 12, 4, 8),   # куртка
        (28, 20, 4, 12, 0, 8),   # правый рукав
        (52, 52, 4, 12, 12, 8),  # левый рукав
        (4, 36, 4, 12, 4, 20),   # правая штанина
        (4, 52, 4, 12, 8, 20),   # левая штанина
    ]

    columns = 8
    rows = (len(skins) + columns - 1) // columns
    cell_w, cell_h = 16 * scale, 32 * scale
    sheet = Image.new("RGBA", (columns * cell_w, rows * cell_h), (26, 22, 20, 255))
    draw = ImageDraw.Draw(sheet)

    for index, skin in enumerate(skins):
        col, row = index % columns, index // columns
        ox, oy = col * cell_w, row * cell_h

        for u, v, w, h, dx, dy in base_parts:
            part = skin.crop((u, v, u + w, v + h)).resize((w * scale, h * scale), Image.NEAREST)
            sheet.alpha_composite(part, (ox + dx * scale, oy + dy * scale))

        for u, v, w, h, dx, dy in overlay_parts:
            part = skin.crop((u, v, u + w, v + h)).resize((w * scale, h * scale), Image.NEAREST)
            sheet.alpha_composite(part, (ox + dx * scale, oy + dy * scale))

        draw.rectangle((ox, oy, ox + cell_w - 1, oy + cell_h - 1), outline=(64, 54, 46, 255))

    sheet.save(path)
    return sheet


def main():
    for directory in (BLOCK_DIR, ITEM_DIR, FIGURINE_DIR, DOCS_DIR):
        os.makedirs(directory, exist_ok=True)

    block_textures = {
        "attic_wood.png": attic_wood(),
        "brass.png": brass(),
        "marble.png": marble(),
        "attic_candle.png": candle(),
        "flame.png": flame(),
        "tome.png": tome(),
        "parchment.png": parchment(),
        "attic_chest.png": attic_chest_texture(),
        "attic_dust.png": attic_dust(),
        "attic_web.png": attic_web_texture(),
        "attic_glass.png": attic_glass_texture(),
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

    icon().save(os.path.join(ASSETS, "icon.png"))
    print("icon.png")

    skins = []
    for index in range(SKIN_COUNT):
        skin = make_skin(index)
        skin.save(os.path.join(FIGURINE_DIR, "skin_%02d.png" % index))
        skins.append(skin)

    print("figurine skins:", SKIN_COUNT)
    make_preview(skins, os.path.join(DOCS_DIR, "figurine_skins.png"))
    make_figure_sheet(skins, os.path.join(DOCS_DIR, "figurine_figures.png"))
    print("docs/figurine_skins.png, docs/figurine_figures.png")


if __name__ == "__main__":
    main()
