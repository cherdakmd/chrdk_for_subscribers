#!/usr/bin/env python3
"""Схема Ауры Пантеона: ступени по числу фигурок рядом.

Запуск из корня репозитория: python3 tools/aura_sheet.py
Результат: docs/aura_ladder.png
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "docs/aura_ladder.png"

BG = (32, 28, 36)
PANEL = (46, 40, 46)
INK = (238, 230, 214)
GOLD = (214, 176, 92)
DIM = (170, 158, 144)
WOOD = (122, 86, 52)

TIERS = [
    ("1+", "Чердачный уют", "Speed", "первая фигурка на полке"),
    ("10+", "Рабочий гул", "Haste", "полка мастеров"),
    ("50+", "Крепкие стены", "Resistance", "чердак держит удар"),
    ("100+", "Дыхание хранителей", "Regeneration", "хранители рядом"),
    ("500+", "Мощь Пантеона", "Strength", "легенда канала"),
]


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    name = "DejaVuSans-Bold.ttf" if bold else "DejaVuSans.ttf"
    return ImageFont.truetype(f"/usr/share/fonts/truetype/dejavu/{name}", size)


def main() -> None:
    width = 1180
    row_height = 74
    header = 96
    footer = 52
    height = header + row_height * len(TIERS) + footer

    image = Image.new("RGB", (width, height), BG)
    draw = ImageDraw.Draw(image)

    title_font = font(26, bold=True)
    subtitle_font = font(14)
    count_font = font(22, bold=True)
    tier_font = font(18, bold=True)
    effect_font = font(15)
    note_font = font(13)

    draw.text((36, 26), "Аура Пантеона", font=title_font, fill=GOLD)
    draw.text((36, 62), "фигурки и портреты активных подписчиков в радиусе 16 блоков",
              font=subtitle_font, fill=DIM)

    for index, (threshold, tier, effect, note) in enumerate(TIERS):
        top = header + index * row_height
        draw.rounded_rectangle((28, top, width - 28, top + row_height - 14), radius=10, fill=PANEL)

        # «свеча» ступени: чем выше тир, тем ярче
        glow = 90 + index * 34
        draw.ellipse((46, top + 12, 78, top + 44), fill=(min(255, glow + 60), min(230, glow + 30), 90))
        draw.rectangle((58, top + 44, 66, top + 56), fill=WOOD)
        draw.text((52, top + 46), "", font=note_font, fill=INK)

        draw.text((96, top + 12), threshold, font=count_font, fill=GOLD)
        draw.text((220, top + 16), tier, font=tier_font, fill=INK)
        draw.text((220, top + 40), note, font=note_font, fill=DIM)
        draw.text((width - 300, top + 24), effect, font=effect_font, fill=GOLD)

    draw.text((36, height - 38),
              "Пересчёт раз в 5 секунд, эффект держится ~12 секунд. Выключить: /attic aura off",
              font=note_font, fill=DIM)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    image.save(OUT)
    print(f"готово: {OUT.relative_to(ROOT)} ({image.width}×{image.height})")


if __name__ == "__main__":
    main()
