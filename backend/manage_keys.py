#!/usr/bin/env python3
"""Manage API keys for GrowTracker ingest backend.

Usage:
  python backend/manage_keys.py --file data/keys.json list
  python backend/manage_keys.py --file data/keys.json add --key NEWKEY123 --name staging --ttl-days 30
  python backend/manage_keys.py --file data/keys.json deactivate --key OLDKEY

TTL (days) converts to epoch millis (expires). 0 = no expiry.
"""
from __future__ import annotations
import argparse, time
from pathlib import Path
from security import KeyManager


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--file', required=True)
    sub = ap.add_subparsers(dest='cmd', required=True)

    add_p = sub.add_parser('add')
    add_p.add_argument('--key', required=True)
    add_p.add_argument('--name', default='')
    add_p.add_argument('--ttl-days', type=int, default=0)

    de_p = sub.add_parser('deactivate')
    de_p.add_argument('--key', required=True)

    sub.add_parser('list')

    args = ap.parse_args()
    km = KeyManager(Path(args.file))

    if args.cmd == 'add':
        exp = 0
        if args.ttl_days:
            exp = int((time.time() + args.ttl_days * 86400) * 1000)
        km.add_key(args.key, name=args.name, expires=exp)
        print(f"[KEYS] Added {args.key} (expires={exp or 'never'})")
    elif args.cmd == 'deactivate':
        km.deactivate(args.key)
        print(f"[KEYS] Deactivated {args.key}")
    elif args.cmd == 'list':
        for k in km.list_keys():
            print(k)

if __name__ == '__main__':
    main()
