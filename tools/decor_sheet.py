#!/usr/bin/env python3
"""Превью декора v0.5: ларь подписчика и чердачные мелочи.

Ларь рисуется из своей модели (tools/render_models.py), мелочи — как текстуры,
потому что пыль — слой, а паутина — крестовая модель ванильного типа.

Запуск из корня репозитория: python3 tools/decor_sheet.py
Результат: docs/attic_decor.png
"""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from PIL import Image, ImageDraw, ImageFont  # noqa: E402

import render_models as rm  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "docs/attic_decor.png"

BG = (32, 28, 36)
INK = (238, 230, 214)
DIM = (170, 158, 144)
GOLD = (214, 176, 92)

DECOR = [
    ("Вековая пыль", "attic_dust", "слой 1/16, как ковёр"),
    ("Чердачная паутина", "attic_web", "крестовая модель"),
    ("Витраж чердака", "attic_glass", "стекло с гербом"),
]


def main() -> None:
    chest = rm.render_block("subscriber_chest")

    title_font = rm.font(20, bold=True)
    name_font = rm.font(14, bold=True)
    note_font = rm.font(11)

    chest_size = chest.width
    tile_size = 120
    padding = 26
    header = 62
    footer = 46

    decor_width = len(DECOR) * (tile_size + padding) - padding
    width = max(chest_size, decor_width, 560) + padding * 2
    chest_top = header
    decor_top = chest_top + chest_size + 56
    height = decor_top + tile_size + 44 + footer

    sheet = Image.new("RGB", (width, height), BG)
    draw = ImageDraw.Draw(sheet)

    draw.text((padding, 18), "Декор чердака и ларь подписчика", font=title_font, fill=GOLD)
    draw.text((padding, 44), "v0.5 — ларь с биркой, пыль, паутина и витраж",
              font=note_font, fill=DIM)

    # ларь сверху
    sheet.paste(chest, (padding, chest_top), chest)
    draw.rectangle((padding, chest_top, padding + chest_size - 1, chest_top + chest_size - 1), outline=(72, 62, 56))
    draw.text((padding, chest_top + chest_size + 6), "Сундук подписчика", font=name_font, fill=INK)
    draw.text((padding, chest_top + chest_size + 24), "8 досок + пустая печать; светится, пока не открыт",
              font=note_font, fill=DIM)

    # мелочи снизу
    decor_x = padding
    decor_y = decor_top

    for title, texture_name, note in DECOR:
        texture = Image.open(rm.ASSETS / f"textures/block/{texture_name}.png").convert("RGBA")
        tile = texture.resize((tile_size, tile_size), Image.NEAREST)

        # шахматка, чтобы было видно прозрачность
        for y in range(0, tile_size, 12):
            for x in range(0, tile_size, 12):
                color = (58, 52, 58) if (x // 12 + y // 12) % 2 == 0 else (46, 42, 48)
                draw.rectangle((decor_x + x, decor_y + y, decor_x + x + 11, decor_y + y + 11), fill=color)

        sheet.paste(tile, (decor_x, decor_y), tile)
        draw.rectangle((decor_x, decor_y, decor_x + tile_size - 1, decor_y + tile_size - 1), outline=(72, 62, 56))
        draw.text((decor_x, decor_y + tile_size + 6), title, font=name_font, fill=INK)
        draw.text((decor_x, decor_y + tile_size + 24), note, font=note_font, fill=DIM)
        decor_x += tile_size + padding

    draw.text((padding, height - 32),
              "Ларь нарисован из своей модели (blockstates/models), мелочи — как текстуры 16×16.",
              font=note_font, fill=DIM)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(OUT)
    print(f"готово: {OUT.relative_to(ROOT)} ({sheet.width}×{sheet.height})")


if __name__ == "__main__":
    main()
