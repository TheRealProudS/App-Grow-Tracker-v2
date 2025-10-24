#!/usr/bin/env python3
"""Fetch latest model & manifest from filesystem registry into target directory.

Usage:
  python ml/fetch_latest_model.py --registry registry --out fetched_model

Result:
  fetched_model/manifest.json
  fetched_model/model.tflite
"""
from __future__ import annotations
import argparse, json, shutil
from pathlib import Path

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--registry', required=True)
    ap.add_argument('--out', required=True)
    ap.add_argument('--version', help='Optional explicit version (otherwise latest)')
    args = ap.parse_args()

    reg_root = Path(args.registry)
    index_path = reg_root / 'index.json'
    if not index_path.exists():
        raise SystemExit('Registry index.json not found')
    index = json.loads(index_path.read_text(encoding='utf-8'))

    version = args.version or index.get('latest')
    if not version:
        raise SystemExit('No version specified and no latest in index')

    version_dir = reg_root / version
    manifest_src = version_dir / 'manifest.json'
    model_src = version_dir / 'model.tflite'
    if not manifest_src.exists() or not model_src.exists():
        raise SystemExit(f'Missing files for version {version}')

    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)
    shutil.copy2(manifest_src, out_dir / 'manifest.json')
    shutil.copy2(model_src, out_dir / 'model.tflite')

    print(f"[FETCH] Retrieved version {version} -> {out_dir}")

if __name__ == '__main__':
    main()
