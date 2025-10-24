#!/usr/bin/env python3
"""Post Training Quantization (PTQ) Pipeline (Experimental)

Pipeline (configurable):
1. Input ONNX model (optionally simplified)
2. Convert ONNX -> TensorFlow (onnx2tf) OR abort with message if missing
3. Convert TF SavedModel -> TFLite FP32
4. Dynamic Range Quantization (int8 weights, float activations)
5. (Optional) Full Int8 PTQ with representative dataset

Usage Example:
  python ml/quantize_ptq.py \
    --onnx ml/outputs/run_001/model_simplified.onnx \
    --repr-dir datasets/plant_v1/images/healthy \
    --count 128 \
    --out ml/outputs/run_001/quant

Notes:
- Requires: onnx, onnx2tf, tensorflow (>=2.10), Pillow
- Representative dataset: small subset of real preprocessed images (unaltered original resolution ok; resized inside script)
- For performance-critical mobile deployment, verify accuracy drop < ~3% macro F1.
"""
from __future__ import annotations
import argparse, os, sys, json, random
from pathlib import Path
from typing import List

try:
    import numpy as np
    from PIL import Image
except ImportError:
    print('[ERROR] Benötigt numpy & Pillow')
    sys.exit(1)

# Optional deps; we guard imports
try:
    import onnx
except ImportError:
    onnx = None  # type: ignore


def collect_images(root: Path, limit: int) -> List[Path]:
    img_ext = {'.jpg','.jpeg','.png','.bmp','.webp'}
    all_imgs = []
    for p in root.rglob('*'):
        if p.suffix.lower() in img_ext:
            all_imgs.append(p)
    random.shuffle(all_imgs)
    return all_imgs[:limit]


def build_representative_gen(image_paths: List[Path], image_size: int):
    def gen():
        import tensorflow as tf  # delayed import
        for p in image_paths:
            try:
                img = Image.open(p).convert('RGB')
                img = img.resize((image_size, image_size))
                arr = np.array(img).astype(np.float32) / 255.0
                # Use same normalization as training (ImageNet stats)
                mean = np.array([0.485,0.456,0.406])
                std = np.array([0.229,0.224,0.225])
                arr = (arr/1.0 - mean) / std
                arr = np.expand_dims(np.transpose(arr, (2,0,1)), 0)  # NCHW -> convert to expected? Need NHWC for TF
                # TFLite expects NHWC typically
                arr = np.transpose(arr, (0,2,3,1))
                yield [arr.astype(np.float32)]
            except Exception:
                continue
    return gen


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--onnx', required=True, help='Pfad zum (vereinfachten) ONNX Modell')
    ap.add_argument('--repr-dir', help='Verzeichnis mit Repräsentativbildern (optional für Full Int8)')
    ap.add_argument('--count', type=int, default=128, help='Max Anzahl Repräsentativbilder')
    ap.add_argument('--image-size', type=int, default=224)
    ap.add_argument('--out', required=True, help='Ausgabeverzeichnis')
    ap.add_argument('--no-dynamic', action='store_true', help='Dynamic Range Quantization überspringen')
    # Optional: direkt nach PTQ Genauigkeitsvergleich ausführen
    ap.add_argument('--compare-meta', help='meta.csv für Test/Val zur automatischen Genauigkeitsprüfung (optional)')
    ap.add_argument('--compare-data-root', help='Wurzelverzeichnis der Bilder für Vergleich')
    ap.add_argument('--compare-classes', help='Komma-separierte Klassenliste in Trainingsreihenfolge')
    ap.add_argument('--compare-max-drop', type=float, default=3.0, help='Max Macro-F1 Drop (Prozent) für Auto-Vergleich')
    ap.add_argument('--compare-script', default='ml/compare_tflite_accuracy.py', help='Pfad zum Vergleichsskript')
    ap.add_argument('--compare-strict', action='store_true', help='Fehlercode wenn Schwelle überschritten')
    args = ap.parse_args()

    onnx_path = Path(args.onnx)
    if not onnx_path.exists():
        print('[ERROR] ONNX Modell nicht gefunden:', onnx_path)
        sys.exit(1)

    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)

    # STEP 1: Convert ONNX -> TF (onnx2tf)
    try:
        import onnx2tf  # type: ignore
    except ImportError:
        print('[ERROR] onnx2tf nicht installiert. Install: pip install onnx2tf tensorflow')
        sys.exit(1)

    print('[INFO] Konvertiere ONNX -> TF SavedModel ...')
    tf_model_dir = out_dir / 'tf_model'
    if tf_model_dir.exists():
        print('[WARN] Überschreibe bestehendes tf_model Verzeichnis')
    # onnx2tf CLI style invocation programmatically:
    try:
        from onnx2tf import convert
        convert(
            input_onnx_file_path=onnx_path.as_posix(),
            output_folder_path=tf_model_dir.as_posix(),
            copy_onnx_input_output_names_to_tflite=True,
            non_verbose=True
        )
    except Exception as e:
        print('[ERROR] ONNX->TF Konvertierung fehlgeschlagen:', e)
        sys.exit(1)

    # STEP 2: TF -> TFLite (FP32)
    try:
        import tensorflow as tf
    except ImportError:
        print('[ERROR] tensorflow nicht installiert.')
        sys.exit(1)
    print('[INFO] Konvertiere TF -> TFLite (FP32) ...')
    converter = tf.lite.TFLiteConverter.from_saved_model(tf_model_dir.as_posix())
    tflite_fp32 = converter.convert()
    fp32_path = out_dir / 'model_fp32.tflite'
    fp32_path.write_bytes(tflite_fp32)
    print(f'[OK] FP32 TFLite gespeichert: {fp32_path.name} ({fp32_path.stat().st_size/1024:.1f} KB)')

    results = {
        'fp32_size_kb': round(fp32_path.stat().st_size/1024,2),
        'int8_dynamic_size_kb': None,
        'int8_full_size_kb': None
    }

    # STEP 3: Dynamic Range Quantization (optional skip)
    if not args.no_dynamic:
        print('[INFO] Dynamic Range Quantization ...')
        converter = tf.lite.TFLiteConverter.from_saved_model(tf_model_dir.as_posix())
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        try:
            tflite_dr = converter.convert()
            dr_path = out_dir / 'model_int8_dynamic.tflite'
            dr_path.write_bytes(tflite_dr)
            results['int8_dynamic_size_kb'] = round(dr_path.stat().st_size/1024,2)
            print(f'[OK] Dynamic Int8 gespeichert: {dr_path.name} ({results['int8_dynamic_size_kb']} KB)')
        except Exception as e:
            print('[WARN] Dynamic Quant fehlgeschlagen:', e)

    # STEP 4: Full Int8 (Representative) wenn Verzeichnis vorhanden
    if args.repr_dir:
        rep_dir = Path(args.repr_dir)
        if rep_dir.exists():
            imgs = collect_images(rep_dir, args.count)
            if imgs:
                print(f'[INFO] Full Int8 PTQ mit {len(imgs)} repr. Bildern ...')
                converter = tf.lite.TFLiteConverter.from_saved_model(tf_model_dir.as_posix())
                converter.optimizations = [tf.lite.Optimize.DEFAULT]
                converter.representative_dataset = build_representative_gen(imgs, args.image_size)
                converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
                converter.inference_input_type = tf.uint8
                converter.inference_output_type = tf.uint8
                try:
                    tflite_full = converter.convert()
                    full_path = out_dir / 'model_int8_full.tflite'
                    full_path.write_bytes(tflite_full)
                    results['int8_full_size_kb'] = round(full_path.stat().st_size/1024,2)
                    print(f'[OK] Full Int8 gespeichert: {full_path.name} ({results['int8_full_size_kb']} KB)')
                except Exception as e:
                    print('[WARN] Full Int8 Quant fehlgeschlagen:', e)
            else:
                print('[WARN] Keine repräsentativen Bilder gefunden für Full Int8.')
        else:
            print('[WARN] repr-dir nicht gefunden, überspringe Full Int8.')

    with open(out_dir / 'ptq_report.json', 'w', encoding='utf-8') as f:
        json.dump(results, f, indent=2)
    print('[DONE] PTQ abgeschlossen. Bericht: ptq_report.json')

    # Optionaler Auto-Vergleich
    if args.compare_meta and args.compare_data_root and args.compare_classes:
        compare_script = Path(args.compare_script)
        if not compare_script.exists():
            print(f'[WARN] Vergleichsskript nicht gefunden: {compare_script} (überspringe)')
        else:
            print('[INFO] Starte automatischen Genauigkeitsvergleich ...')
            import subprocess, shlex
            cmd = [
                sys.executable, compare_script.as_posix(),
                '--models-dir', out_dir.as_posix(),
                '--data-root', args.compare_data_root,
                '--meta', args.compare_meta,
                '--classes', args.compare_classes,
                '--image-size', str(args.image_size),
                '--max-macro-f1-drop', str(args.compare_max_drop),
                '--max-accuracy-drop', str(args.compare_max_drop)
            ]
            if args.compare_strict:
                cmd.append('--strict')
            try:
                print('[DEBUG] CMD:', ' '.join(shlex.quote(c) for c in cmd))
                res = subprocess.run(cmd, capture_output=True, text=True)
                print(res.stdout)
                if res.returncode != 0:
                    print('[WARN] Vergleich meldete Returncode', res.returncode)
                    if res.stderr:
                        print(res.stderr)
            except Exception as e:
                print('[WARN] Auto-Vergleich fehlgeschlagen:', e)
    else:
        print('[INFO] Auto-Vergleich übersprungen (Parameter unvollständig).')

if __name__ == '__main__':
    main()
