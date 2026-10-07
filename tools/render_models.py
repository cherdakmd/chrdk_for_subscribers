#!/usr/bin/env python3
"""Черновые превью блоков «Чердака Бессмертных».

Рисует развёртку (вид спереди) прямо из моделей и текстур мода — так можно
посмотреть на блоки, не запуская игру. Внутри рамы дополнительно подставляется
лицо из скина, как это будет выглядеть с портретом.

Запуск из корня репозитория:  python3 tools/render_models.py
Результат: docs/attic_blocks.png
"""
from __future__ import annotations

import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/chrdk_pantheon"
OUT = ROOT / "docs/attic_blocks.png"

PX_PER_UNIT = 15  # 16 юнитов блока -> 240 px
PAD = 12
LABEL_HEIGHT = 46
BG = (32, 28, 36)
INK = (232, 224, 210)
DIM = (168, 156, 142)

# Панели: (подпись, id блока, подпись снизу)
PANELS = [
    ("Чердачный алтарь", "attic_altar", "ритуал и сводка"),
    ("Постамент подписчика", "pedestal", "фигурка подписчика"),
    ("Портретная рама", "portrait", "голова в проёме рамы"),
    ("Обелиск Имён", "obelisk", "печатает Книгу Имён"),
    ("Чердачный канделябр", "candelabra", "свет 14, три свечи"),
]

FACE_ORDER = ("south", "east", "west", "north", "up", "down")


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    name = "DejaVuSans-Bold.ttf" if bold else "DejaVuSans.ttf"
    return ImageFont.truetype(f"/usr/share/fonts/truetype/dejavu/{name}", size)


def load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def texture_image(reference: str, cache: dict) -> Image.Image:
    """Достаёт PNG по ссылке вида chrdk_pantheon:block/brass или minecraft:block/stone."""
    if reference in cache:
        return cache[reference]

    namespace, _, path = reference.partition(":")
    if namespace == "chrdk_pantheon":
        file = ASSETS / "textures" / f"{path}.png"
    else:
        # Ванильные текстуры в песочнице недоступны — рисуем заглушку в тон моде.
        file = None

    if file is not None and file.exists():
        image = Image.open(file).convert("RGBA")
    else:
        image = Image.new("RGBA", (16, 16), (120, 112, 108, 255))

    cache[reference] = image
    return image


def face_tile(element: dict, face: str, textures: dict, cache: dict) -> Image.Image | None:
    """Кусок текстуры для конкретной грани (учитывает uv, если он задан)."""
    face_data = element.get("faces", {}).get(face)
    if face_data is None:
        return None

    reference = face_data.get("texture", "")
    if reference.startswith("#"):
        reference = textures.get(reference[1:], "")

    if not reference:
        return None

    image = texture_image(reference, cache)
    width, height = image.size

    uv = face_data.get("uv")
    if uv and len(uv) == 4:
        scale_x = width / 16.0
        scale_y = height / 16.0
        box = (round(uv[0] * scale_x), round(uv[1] * scale_y),
               round(uv[2] * scale_x), round(uv[3] * scale_y))
        image = image.crop(box)

    rotation = face_data.get("rotation", 0)
    if rotation:
        image = image.rotate(-rotation, expand=True)

    return image


def render_block(block_id: str) -> Image.Image:
    """Развёртка блока: вид спереди (с юга), дальние элементы рисуются первыми."""
    model = load_json(ASSETS / "models/block" / f"{block_id}.json")
    textures = model.get("textures", {})
    cache: dict = {}

    size = 16 * PX_PER_UNIT
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(canvas)

    elements = sorted(model.get("elements", []), key=lambda el: el["from"][2])

    for element in elements:
        x0, y0, z0 = element["from"]
        x1, y1, z1 = element["to"]

        left = round(x0 * PX_PER_UNIT)
        right = round(x1 * PX_PER_UNIT)
        top = round((16 - y1) * PX_PER_UNIT)
        bottom = round((16 - y0) * PX_PER_UNIT)

        rect = (left, top, right, bottom)

        tile = None
        for face in FACE_ORDER:
            tile = face_tile(element, face, textures, cache)
            if tile is not None:
                break

        if tile is None:
            continue

        width = max(1, right - left)
        height = max(1, bottom - top)
        canvas.paste(tile.resize((width, height), Image.NEAREST), (left, top), tile.resize((width, height), Image.NEAREST))
        draw.rectangle((left, top, right - 1, bottom - 1), outline=(20, 16, 14, 190))

    return canvas


def add_portrait_face(frame: Image.Image, skin_path: Path) -> Image.Image:
    """Вклеивает лицо из скина в проём рамы — как это будет в игре."""
    skin = Image.open(skin_path).convert("RGBA")
    face = skin.crop((8, 8, 16, 16))
    hat = skin.crop((40, 8, 48, 16))

    result = frame.copy()
    # Голова в игре стоит по центру блока: 8 юнитов шириной, низ на y = 4.
    size = 8 * PX_PER_UNIT
    left = round(4.0 * PX_PER_UNIT)
    top = round((16 - 12.0) * PX_PER_UNIT)

    face_big = face.resize((size, size), Image.NEAREST)
    hat_big = hat.resize((size, size), Image.NEAREST)

    result.paste(face_big, (left, top), face_big)
    result.paste(hat_big, (left, top), hat_big)
    ImageDraw.Draw(result).rectangle(
        (left, top, left + size - 1, top + size - 1), outline=(20, 16, 14, 210))
    return result


def main() -> None:
    panel_width = 16 * PX_PER_UNIT
    panel_height = panel_width + LABEL_HEIGHT
    total_width = PAD + len(PANELS) * (panel_width + PAD)
    total_height = PAD + panel_height + PAD + 34

    sheet = Image.new("RGB", (total_width, total_height), BG)
    draw = ImageDraw.Draw(sheet)

    title_font = font(16, bold=True)
    body_font = font(12)
    small_font = font(11)

    for index, (title, block_id, caption) in enumerate(PANELS):
        x = PAD + index * (panel_width + PAD)
        y = PAD

        block = render_block(block_id)
        if block_id == "portrait":
            block = add_portrait_face(block, ASSETS / "textures/figurine/skin_07.png")

        sheet.paste(block, (x, y), block)
        draw.rectangle((x, y, x + panel_width - 1, y + panel_width - 1), outline=(72, 62, 56))

        draw.text((x, y + panel_width + 8), title, font=title_font, fill=INK)
        draw.text((x, y + panel_width + 28), caption, font=small_font, fill=DIM)

    draw.text((PAD, total_height - 28),
              "Развёртки моделей мода (источник: blockstates/models + текстуры). Портрет показан с лицом из скина.",
              font=body_font, fill=DIM)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(OUT)
    print(f"готово: {OUT.relative_to(ROOT)} ({sheet.width}×{sheet.height})")


if __name__ == "__main__":
    main()
