#!/usr/bin/env python3
"""Simplify ONNX model using onnx-simplifier (onnxsim).

Usage:
  python ml/simplify_onnx.py --input ml/outputs/run_001/model.onnx --output ml/outputs/run_001/model_simplified.onnx

If --overwrite is set, output replaces input.

Will report original vs simplified file size and operator count difference.
"""
from __future__ import annotations
import argparse, os, sys, json
from pathlib import Path

try:
    import onnx
    from onnxsim import simplify
except ImportError:
    print('[ERROR] onnx / onnxsim nicht installiert. Bitte: pip install onnx onnxsim')
    sys.exit(1)

def count_nodes(model):
    return len(model.graph.node)

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--input', required=True, help='Pfad zur ONNX Datei')
    ap.add_argument('--output', required=False, help='Zielpfad (Standard: *_simplified.onnx)')
    ap.add_argument('--overwrite', action='store_true')
    ap.add_argument('--no_check', action='store_true', help='Validierungscheck deaktivieren')
    args = ap.parse_args()

    in_path = Path(args.input)
    if not in_path.exists():
        print(f'[ERROR] Datei nicht gefunden: {in_path}')
        sys.exit(1)

    out_path = Path(args.output) if args.output else in_path.with_name(in_path.stem + '_simplified.onnx')
    if args.overwrite:
        out_path = in_path

    print(f'[INFO] Lade Modell: {in_path}')
    model = onnx.load(in_path.as_posix())
    orig_nodes = count_nodes(model)
    orig_size = in_path.stat().st_size

    print('[INFO] Starte Vereinfachung...')
    simplified_model, success = simplify(model, check_n=0 if args.no_check else 3)
    if not success:
        print('[WARN] Vereinfachung meldet erfolglos (success=False), speichere dennoch Ergebnis.')

    onnx.save(simplified_model, out_path.as_posix())
    new_size = out_path.stat().st_size
    new_nodes = count_nodes(simplified_model)

    report = {
        'input': str(in_path),
        'output': str(out_path),
        'overwrite': args.overwrite,
        'orig_nodes': orig_nodes,
        'new_nodes': new_nodes,
        'orig_size_bytes': orig_size,
        'new_size_bytes': new_size,
        'size_reduction_pct': round(100*(1 - new_size / orig_size),2)
    }
    print('[DONE] Vereinfachung abgeschlossen:')
    print(json.dumps(report, indent=2))

if __name__ == '__main__':
    main()
