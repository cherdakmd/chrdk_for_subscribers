#!/usr/bin/env python3
"""Отправитель событий для моста «Чердака Бессмертных» (проверка v0.6 и после неё).

Умеет двумя способами:
  * положить файл в папку входящих мода (канал А);
  * отправить POST на локальный порт (канал Б).

Примеры:
  # подписка на 12 месяцев
  python3 tools/bridge_send.py Kolya sub --months 12

  # донат на 500 с сообщением
  python3 tools/bridge_send.py Kolya donate --amount 500 --message "привет с чердака"

  # рейд и гифт-саб
  python3 tools/bridge_send.py Kolya raid --viewers 42
  python3 tools/bridge_send.py Kolya gift --months 3 --gifted 5

  # положить файл, а не дёргать порт
  python3 tools/bridge_send.py Kolya follow --file

Пути по умолчанию — для стандартного лаунчера: папка входящих лежит в <игра>/config/chrdk_pantheon/inbox.
Если игра стоит в другом месте, укажи --inbox /path/to/inbox.
"""
from __future__ import annotations

import argparse
import json
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

DEFAULT_INBOX = Path("config/chrdk_pantheon/inbox")
DEFAULT_URL = "http://127.0.0.1:8765/event"

TYPES = ("follow", "sub", "resub", "gift", "donation", "raid", "message", "remove")


def build_event(args: argparse.Namespace) -> dict:
    event = {
        "type": args.type,
        "nick": args.nick,
        "platform": args.platform,
    }

    if args.months:
        event["months"] = args.months

    if args.amount:
        event["amount"] = args.amount

    if args.tier:
        event["tier"] = args.tier

    if args.message:
        event["message"] = args.message

    if args.id:
        event["id"] = args.id
    else:
        event["id"] = f"{args.platform}-{args.type}-{args.nick}-{int(time.time() * 1000)}"

    return event


def send_http(event: dict, url: str, token: str | None) -> int:
    body = json.dumps(event, ensure_ascii=False).encode("utf-8")
    request = urllib.request.Request(url, data=body, method="POST")
    request.add_header("Content-Type", "application/json")

    if token:
        request.add_header("X-Attic-Token", token)

    try:
        with urllib.request.urlopen(request, timeout=5) as response:
            print(f"{response.status} {response.read().decode('utf-8', 'replace')}")
            return 0
    except urllib.error.HTTPError as error:
        print(f"{error.code} {error.read().decode('utf-8', 'replace')}", file=sys.stderr)
        return 1
    except urllib.error.URLError as error:
        print(f"не дозвонился до {url}: {error.reason}", file=sys.stderr)
        print("мост включён? проверь /attic bridge в игре", file=sys.stderr)
        return 2


def send_file(event: dict, inbox: Path) -> int:
    inbox.mkdir(parents=True, exist_ok=True)
    stamp = time.strftime("%Y-%m-%dT%H-%M-%S", time.localtime())
    path = inbox / f"event-{stamp}-{int(time.time() * 1000) % 1000:03d}.json"
    path.write_text(json.dumps(event, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"положил: {path}")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="Отправить событие на чердак")
    parser.add_argument("nick", help="ник зрителя")
    parser.add_argument("type", choices=TYPES, help="тип события")
    parser.add_argument("--months", type=int, help="месяцев подписки (sub/resub/gift)")
    parser.add_argument("--amount", type=int, help="сумма доната")
    parser.add_argument("--tier", type=int, choices=range(1, 6), help="тир вручную (1..5)")
    parser.add_argument("--message", help="сообщение зрителя")
    parser.add_argument("--platform", default="проверка", help="откуда пришло (twitch/youtube/…)")
    parser.add_argument("--id", help="идентификатор события (иначе сгенерируется)")
    parser.add_argument("--file", action="store_true", help="положить файл в папку входящих")
    parser.add_argument("--inbox", type=Path, default=DEFAULT_INBOX, help="папка входящих мода")
    parser.add_argument("--url", default=DEFAULT_URL, help="адрес локального порта моста")
    parser.add_argument("--token", help="общий секрет, если включён в bridge.json")
    args = parser.parse_args()

    event = build_event(args)
    print(json.dumps(event, ensure_ascii=False))

    if args.file:
        return send_file(event, args.inbox)

    return send_http(event, args.url, args.token)


if __name__ == "__main__":
    raise SystemExit(main())
