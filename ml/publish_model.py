#!/usr/bin/env python3
"""Publish a trained model + manifest to a simple filesystem-based registry.

Usage:
  python ml/publish_model.py \
    --model ml/outputs/run_010/model_int8_full.tflite \
    --manifest ml/outputs/run_010/leafsense_model.json \
    --registry registry/ \
    --version 20250930_run010_int8

Creates:
  registry/<version>/model.tflite
  registry/<version>/manifest.json
Updates (or creates) registry/index.json with latest pointer and version list.
"""
from __future__ import annotations
import argparse, json, shutil, hashlib
from pathlib import Path

def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as f:
        for chunk in iter(lambda: f.read(1<<20), b''):
            h.update(chunk)
    return h.hexdigest()

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--model', required=True)
    ap.add_argument('--manifest', required=True)
    ap.add_argument('--registry', required=True)
    ap.add_argument('--version', required=True)
    ap.add_argument('--force', action='store_true', help='Overwrite existing version directory if present')
    args = ap.parse_args()

    model_path = Path(args.model)
    manifest_path = Path(args.manifest)
    reg_root = Path(args.registry)
    version_dir = reg_root / args.version

    if version_dir.exists():
        if not args.force:
            raise SystemExit(f"Version directory exists: {version_dir}. Use --force to overwrite.")
        shutil.rmtree(version_dir)
    version_dir.mkdir(parents=True, exist_ok=True)

    # Copy model
    target_model = version_dir / 'model.tflite'
    shutil.copy2(model_path, target_model)
    model_hash = sha256(target_model)

    # Load & augment manifest
    manifest = json.loads(manifest_path.read_text(encoding='utf-8'))
    manifest['model_file'] = 'model.tflite'
    manifest['model_version'] = args.version
    manifest['model_sha256'] = model_hash

    target_manifest = version_dir / 'manifest.json'
    target_manifest.write_text(json.dumps(manifest, indent=2), encoding='utf-8')

    # Update index
    index_path = reg_root / 'index.json'
    if index_path.exists():
        index = json.loads(index_path.read_text(encoding='utf-8'))
    else:
        index = {'versions': []}
    # Remove existing identical version if present
    index['versions'] = [v for v in index.get('versions', []) if v.get('version') != args.version]
    index['versions'].append({'version': args.version, 'model_sha256': model_hash})
    index['versions'] = sorted(index['versions'], key=lambda v: v['version'])
    index['latest'] = args.version
    index_path.write_text(json.dumps(index, indent=2), encoding='utf-8')

    print(f"[PUBLISH] Version {args.version} published. Latest -> {index['latest']}")

if __name__ == '__main__':
    main()
