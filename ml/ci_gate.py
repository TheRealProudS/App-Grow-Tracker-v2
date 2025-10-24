#!/usr/bin/env python3
"""CI Gate script for LeafSense model quality.

Steps:
1. (Optional) Run quantization if --onnx provided and output dir missing.
2. Run TFLite accuracy comparison (strict thresholds).
3. Generate manifest selecting best model.
4. Exit non-zero if thresholds violated or required artifacts missing.

Usage Example:
  python ml/ci_gate.py \
    --onnx ml/outputs/run_007/model_simplified.onnx \
    --quant-dir ml/outputs/run_007/quant \
    --data-root datasets/plant_v1/images \
    --meta datasets/plant_v1/meta_test.csv \
    --classes healthy,chlorosis,fungus,necrosis \
    --max-drop 3.0 \
    --manifest-out app/src/main/assets/leafsense_model.json
"""
from __future__ import annotations
import argparse, subprocess, sys, json
from pathlib import Path

def run(cmd: list[str], desc: str):
    print(f"[RUN] {desc}: {' '.join(cmd)}")
    res = subprocess.run(cmd, text=True, capture_output=True)
    print(res.stdout)
    if res.returncode != 0:
        print(res.stderr)
    return res.returncode

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--onnx')
    ap.add_argument('--quant-dir', required=True)
    ap.add_argument('--data-root', required=True)
    ap.add_argument('--meta', required=True)
    ap.add_argument('--classes', required=True)
    ap.add_argument('--max-drop', type=float, default=3.0)
    ap.add_argument('--min-macro-f1', type=float, help='Absolute Mindest-Macro-F1 für FP32 Baseline')
    ap.add_argument('--image-size', type=int, default=224)
    ap.add_argument('--manifest-out', required=True)
    ap.add_argument('--skip-quant', action='store_true')
    args = ap.parse_args()

    quant_dir = Path(args.quant_dir)
    quant_dir.mkdir(parents=True, exist_ok=True)

    # Step 1 Quantization
    if not args.skip_quant and args.onnx and not any((quant_dir / n).exists() for n in ('model_fp32.tflite','model_int8_full.tflite','model_int8_dynamic.tflite')):
        qcmd = [sys.executable, 'ml/quantize_ptq.py', '--onnx', args.onnx, '--out', str(quant_dir)]
        rc = run(qcmd, 'Quantization')
        if rc != 0:
            sys.exit(rc)
    else:
        print('[INFO] Quantisierung übersprungen (Artefakte vorhanden oder --skip-quant).')

    # Step 2 Comparison
    ccmd = [
        sys.executable, 'ml/compare_tflite_accuracy.py',
        '--models-dir', str(quant_dir),
        '--data-root', args.data_root,
        '--meta', args.meta,
        '--classes', args.classes,
        '--image-size', str(args.image_size),
        '--max-macro-f1-drop', str(args.max_drop),
        '--max-accuracy-drop', str(args.max_drop),
        '--strict'
    ]
    if args.min_macro_f1 is not None:
        ccmd.extend(['--min-macro-f1', str(args.min_macro_f1)])
    rc = run(ccmd, 'TFLite Accuracy Comparison')
    if rc != 0:
        print('[FAIL] Accuracy Gate nicht bestanden.')
        sys.exit(rc)

    # Step 3 Manifest
    mcmd = [
        sys.executable, 'ml/generate_manifest.py',
        '--models-dir', str(quant_dir),
        '--out', args.manifest_out,
        '--classes', args.classes,
        '--input-size', str(args.image_size)
    ]
    rc = run(mcmd, 'Generate Manifest')
    if rc != 0:
        print('[FAIL] Manifest Generierung fehlgeschlagen.')
        sys.exit(rc)

    print('[OK] CI Gate erfolgreich. Modell bereit.')

if __name__ == '__main__':
    main()
