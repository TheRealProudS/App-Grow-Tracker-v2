#!/usr/bin/env python3
"""Generate leafsense_model.json manifest.

Combines information from:
- Selected model variant (priority: model_int8_full.tflite > model_int8_dynamic.tflite > model_fp32.tflite > leafsense_model.tflite)
- Optional comparison report (latest artifacts/quant_eval/*_tflite_eval.json)
- Optional class list (labels file or provided via --classes)

Usage:
  python ml/generate_manifest.py \
    --models-dir ml/outputs/run_001/quant \
    --out app/src/main/assets/leafsense_model.json \
    --labels app/src/main/assets/leafsense_labels.txt

Optional:
  --classes healthy,chlorosis,... (overrides labels file)
  --input-size 224
  --comparison artifacts/quant_eval/20250929_120000_tflite_eval.json
  --version 2025-09-29_mnetv3s_int8f

If comparison not provided, tries newest *_tflite_eval.json.
"""
from __future__ import annotations
import argparse, json, time, hashlib
from pathlib import Path
from typing import List, Dict, Any, Optional

PRIORITY = ["model_int8_full.tflite","model_int8_dynamic.tflite","model_fp32.tflite","leafsense_model.tflite"]

def load_labels(path: Path) -> List[str]:
    return [l.strip() for l in path.read_text(encoding='utf-8').splitlines() if l.strip()]

def find_model(models_dir: Path) -> str | None:
    for name in PRIORITY:
        if (models_dir / name).exists():
            return name
    return None

def latest_comparison(report_glob: str) -> Optional[Path]:
    paths = sorted(Path('.').glob(report_glob))
    return paths[-1] if paths else None

def extract_metrics(report_path: Path, model_file: str) -> Dict[str, Any]:
    try:
        data = json.loads(report_path.read_text(encoding='utf-8'))
    except Exception:
        return {}
    reports = data.get('reports', {})
    variant_map = {
        'model_int8_full.tflite': 'int8_full',
        'model_int8_dynamic.tflite': 'int8_dynamic',
        'model_fp32.tflite': 'fp32',
        'leafsense_model.tflite': 'fp32'  # treat as baseline
    }
    sel_key = variant_map.get(model_file)
    if not sel_key:
        return {}
    chosen = reports.get(sel_key, {})
    base = reports.get('fp32', {})
    result = {}
    for k in ('macro_f1','accuracy','delta_macro_f1_pct','delta_accuracy_pct'):
        if k in chosen:
            result[k] = chosen[k]
    # Fill baseline for reference
    if 'macro_f1' in base:
        result['baseline_fp32_macro_f1'] = base['macro_f1']
    return result

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--models-dir', required=True)
    ap.add_argument('--out', required=True)
    ap.add_argument('--labels')
    ap.add_argument('--classes')
    ap.add_argument('--input-size', type=int, default=224)
    ap.add_argument('--comparison', help='Pfad zu *_tflite_eval.json')
    ap.add_argument('--comparison-glob', default='artifacts/quant_eval/*_tflite_eval.json')
    ap.add_argument('--version', help='Modellversion Tag; default: timestamp + filename')
    # Knowledge base (optional)
    ap.add_argument('--kb-version', help='Knowledge base version tag (optional)')
    ap.add_argument('--kb-merged', help='Path to merged knowledge JSONL (e.g. knowledge/build/merged.jsonl)')
    ap.add_argument('--kb-index', help='Path to vector index (faiss or tfidf)')
    ap.add_argument('--kb-entry-count', type=int, help='Override knowledge entry count (if not auto-detected)')
    args = ap.parse_args()

    models_dir = Path(args.models_dir)
    model_file = find_model(models_dir)
    if not model_file:
        raise SystemExit('Kein Modell gefunden (erwartet TFLite Varianten).')

    if args.classes:
        classes = [c.strip() for c in args.classes.split(',') if c.strip()]
    elif args.labels:
        classes = load_labels(Path(args.labels))
    else:
        raise SystemExit('Klassen nicht gefunden: --classes oder --labels angeben.')

    comparison_path = Path(args.comparison) if args.comparison else latest_comparison(args.comparison_glob)
    metrics = extract_metrics(comparison_path, model_file) if comparison_path else {}

    version = args.version or time.strftime('%Y%m%d_%H%M%S') + '_' + model_file.replace('.tflite','')

    # Compute model file SHA256 for integrity
    model_path = models_dir / model_file
    sha256 = None
    try:
        h = hashlib.sha256()
        with model_path.open('rb') as f:
            for chunk in iter(lambda: f.read(65536), b''):
                h.update(chunk)
        sha256 = h.hexdigest()
    except Exception:
        sha256 = None

    manifest = {
        'model_version': version,
        'model_file': model_file,
        'model_sha256': sha256,
        'input_size': args.input_size,
        'format': 'tflite',
        'quantization': ('int8_full' if 'int8_full' in model_file else 'int8_dynamic' if 'int8_dynamic' in model_file else 'fp32'),
        'classes': classes,
        'normalization': {
            'mean': [0.485,0.456,0.406],
            'std': [0.229,0.224,0.225]
        }
    }
    manifest.update(metrics)

    # Optional knowledge block
    if args.kb_version or args.kb_merged or args.kb_index:
        kb_block = {}
        if args.kb_version:
            kb_block['kb_version'] = args.kb_version
        if args.kb_merged:
            kb_block['merged_file'] = args.kb_merged
            if 'kb_version' not in kb_block:
                # derive a quick version tag from filename timestamp or fallback
                kb_block['kb_version'] = 'kb_' + Path(args.kb_merged).stem
            # auto count lines if file exists and no manual override
            if args.kb_entry_count is None:
                try:
                    p = Path(args.kb_merged)
                    if p.exists():
                        with p.open('r', encoding='utf-8') as f:
                            kb_block['entry_count'] = sum(1 for _ in f if _.strip())
                except Exception:
                    pass
        if args.kb_entry_count is not None:
            kb_block['entry_count'] = args.kb_entry_count
        if args.kb_index:
            kb_block['index_file'] = args.kb_index
        if kb_block:
            manifest['knowledge'] = kb_block

    out_path = Path(args.out)
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text(json.dumps(manifest, indent=2), encoding='utf-8')
    print('[OK] Manifest geschrieben:', out_path)
    if metrics:
        print('[INFO] Enthaltene Metriken:', {k: metrics[k] for k in metrics.keys() if 'macro' in k or 'accuracy' in k})

if __name__ == '__main__':
    main()
