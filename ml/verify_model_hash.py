#!/usr/bin/env python3
"""Verify model integrity by recomputing SHA256 and comparing with manifest.

Usage:
  python ml/verify_model_hash.py --manifest app/src/main/assets/leafsense_model.json \
      --search-dirs app/src/main/assets ml/outputs/run_007/quant \
      --require

Behavior:
  - Reads manifest JSON and extracts model_file + model_sha256.
  - Searches provided directories (default: current directory) for the model file name.
  - Computes SHA256; compares with manifest.model_sha256.
  - Exit 0 on success / match (or if hash absent and --require not set).
  - Exit 1 if mismatch or required hash missing.

Intended CI Integration:
  Add step after manifest generation & before deployment packaging to ensure artifacts are consistent.
"""
from __future__ import annotations
import argparse, json, sys, hashlib
from pathlib import Path
from typing import Iterable, Optional


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def find_file(filename: str, dirs: Iterable[Path]) -> Optional[Path]:
    for d in dirs:
        if not d.exists():
            continue
        candidate = d / filename
        if candidate.exists():
            return candidate
    # fallback: recursive search if not found directly
    for d in dirs:
        if not d.exists():
            continue
        for p in d.rglob(filename):
            if p.is_file():
                return p
    return None


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--manifest', default='app/src/main/assets/leafsense_model.json')
    ap.add_argument('--search-dirs', nargs='*', default=['app/src/main/assets', 'ml/outputs'])
    ap.add_argument('--require', action='store_true', help='Fail if manifest hash missing or model not found')
    ap.add_argument('--quiet', action='store_true')
    args = ap.parse_args()

    manifest_path = Path(args.manifest)
    if not manifest_path.exists():
        print(f"[verify] Manifest not found: {manifest_path}", file=sys.stderr)
        return 1
    try:
        manifest = json.loads(manifest_path.read_text(encoding='utf-8'))
    except Exception as e:
        print(f"[verify] Failed to parse manifest: {e}", file=sys.stderr)
        return 1

    model_file = manifest.get('model_file') or manifest.get('modelFile')
    manifest_hash = manifest.get('model_sha256')

    if not model_file:
        print('[verify] model_file missing in manifest', file=sys.stderr)
        return 1
    if not manifest_hash:
        msg = '[verify] model_sha256 missing in manifest'
        if args.require:
            print(msg, file=sys.stderr)
            return 1
        else:
            if not args.quiet:
                print(msg + ' (warning only)')
            return 0

    search_dirs = [Path(d) for d in args.search_dirs]
    located = find_file(model_file, search_dirs)
    if not located:
        msg = f"[verify] model file '{model_file}' not found in search dirs: {', '.join(str(d) for d in search_dirs)}"
        if args.require:
            print(msg, file=sys.stderr)
            return 1
        else:
            if not args.quiet:
                print(msg + ' (warning only)')
            return 0

    try:
        runtime_hash = sha256_file(located)
    except Exception as e:
        print(f"[verify] Failed to hash model file: {e}", file=sys.stderr)
        return 1

    if runtime_hash.lower() != manifest_hash.lower():
        print(f"[verify] HASH MISMATCH: manifest={manifest_hash} actual={runtime_hash} file={located}", file=sys.stderr)
        return 1

    if not args.quiet:
        print(f"[verify] OK hash match for {model_file} ({runtime_hash})")
    return 0

if __name__ == '__main__':
    sys.exit(main())
