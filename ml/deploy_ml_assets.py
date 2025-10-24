#!/usr/bin/env python3
"""Deploy ML + Knowledge assets into Android app assets folder.

Responsibilities:
 1. Select model (same priority as generate_manifest)
 2. Ensure labels/classes available
 3. Generate manifest (delegates to generate_manifest.py or inline minimal if flag set)
 4. Copy model + labels + manifest + knowledge files into app/src/main/assets/
 5. Validate manifest references (model_file, knowledge.merged_file) exist post-copy

Usage example:
  python ml/deploy_ml_assets.py \
    --models-dir ml/outputs/run_007/quant \
    --labels ml/outputs/run_007/labels.txt \
    --knowledge-merged knowledge/build/merged.jsonl \
    --out-assets app/src/main/assets

Add --no-manifest to skip regeneration if you already created it.
"""
from __future__ import annotations
import argparse, shutil, subprocess, sys, json, hashlib
from pathlib import Path
from typing import Optional

PRIORITY = ["model_int8_full.tflite","model_int8_dynamic.tflite","model_fp32.tflite","leafsense_model.tflite"]


def find_model(models_dir: Path) -> Optional[Path]:
    for name in PRIORITY:
        p = models_dir / name
        if p.exists():
            return p
    return None


def copy(src: Path, dst: Path):
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)


def run_generate_manifest(args, model_file: Path, out_manifest: Path, classes_source: Path | None):
    cmd = [sys.executable, 'ml/generate_manifest.py',
           '--models-dir', str(model_file.parent),
           '--out', str(out_manifest),
           '--input-size', str(args.input_size)]
    if classes_source and classes_source.exists():
        cmd += ['--labels', str(classes_source)]
    if args.version:
        cmd += ['--version', args.version]
    if args.knowledge_merged:
        cmd += ['--kb-merged', args.knowledge_merged]
    if args.knowledge_version:
        cmd += ['--kb-version', args.knowledge_version]
    if args.knowledge_index:
        cmd += ['--kb-index', args.knowledge_index]
    print('[CMD]', ' '.join(cmd))
    subprocess.check_call(cmd)


def sha256_file(p: Path) -> str | None:
    try:
        h = hashlib.sha256()
        with p.open('rb') as f:
            for chunk in iter(lambda: f.read(65536), b''):
                h.update(chunk)
        return h.hexdigest()
    except Exception:
        return None

def validate_manifest(manifest_path: Path, assets_dir: Path):
    try:
        data = json.loads(manifest_path.read_text(encoding='utf-8'))
    except Exception as e:
        raise SystemExit(f"Manifest JSON ungültig: {e}")
    missing = []
    model_rel = data.get('model_file')
    if model_rel:
        if not (assets_dir / model_rel).exists():
            missing.append(model_rel)
    kb = data.get('knowledge') or {}
    merged = kb.get('merged_file')
    if merged and not (assets_dir / merged).exists():
        missing.append(merged)
    # Hash check
    model_sha = data.get('model_sha256')
    if model_rel and model_sha:
        actual_hash = sha256_file(assets_dir / model_rel)
        if actual_hash and actual_hash.lower() != model_sha.lower():
            raise SystemExit(f"Hash mismatch: manifest {model_sha} != actual {actual_hash}")
        if not actual_hash:
            print('[WARN] Konnte Modellhash nicht berechnen (Übersprungen).')

    if missing:
        raise SystemExit(f"Fehlende referenzierte Dateien im Assets-Verzeichnis: {missing}")
    print('[OK] Manifest Referenzen validiert.')


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--models-dir', required=True)
    ap.add_argument('--labels', help='Pfad zur Labels-Datei (optional wenn Manifest Klassen generiert)')
    ap.add_argument('--out-assets', default='app/src/main/assets')
    ap.add_argument('--manifest-name', default='leafsense_model.json')
    ap.add_argument('--input-size', type=int, default=224)
    ap.add_argument('--version', help='Version Tag fürs Modell')
    # Knowledge
    ap.add_argument('--knowledge-merged', help='Knowledge JSONL (wird kopiert)')
    ap.add_argument('--knowledge-version', help='Knowledge Versions-Tag')
    ap.add_argument('--knowledge-index', help='Optionaler Index (faiss/tfidf)')
    ap.add_argument('--no-manifest', action='store_true', help='Manifest nicht regenerieren (nur kopieren)')
    args = ap.parse_args()

    models_dir = Path(args.models_dir)
    assets_dir = Path(args.out_assets)
    assets_dir.mkdir(parents=True, exist_ok=True)

    model_path = find_model(models_dir)
    if not model_path:
        raise SystemExit('Kein Modell in models-dir gefunden (erwartet TFLite Varianten).')

    # Copy model
    copy(model_path, assets_dir / model_path.name)
    print(f'[OK] Modell kopiert: {model_path.name}')

    labels_src = Path(args.labels) if args.labels else None
    if labels_src and labels_src.exists():
        copy(labels_src, assets_dir / labels_src.name)
        print(f'[OK] Labels kopiert: {labels_src.name}')

    # Knowledge file
    if args.knowledge_merged:
        km = Path(args.knowledge_merged)
        if not km.exists():
            raise SystemExit(f'Knowledge Datei nicht gefunden: {km}')
        target_km = assets_dir / km
        target_km.parent.mkdir(parents=True, exist_ok=True)
        copy(km, target_km)
        print(f'[OK] Knowledge kopiert: {km}')

    manifest_path = assets_dir / args.manifest_name
    if not args.no_manifest:
        run_generate_manifest(args, model_path, manifest_path, labels_src)
    else:
        if not manifest_path.exists():
            raise SystemExit('Manifest fehlt und --no-manifest gesetzt.')

    validate_manifest(manifest_path, assets_dir)
    print('[DONE] Deployment abgeschlossen ->', assets_dir)

if __name__ == '__main__':
    main()
