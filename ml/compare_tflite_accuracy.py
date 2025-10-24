#!/usr/bin/env python3
"""Compare FP32 vs Quantized TFLite model accuracy & metrics.

Features:
- Loads one or more TFLite models (fp32, dynamic int8, full int8) if files exist in a directory.
- Reconstructs dataset from meta.csv (same format used in training) without torch dependency.
- Applies identical preprocessing (resize -> center crop -> normalization) to match training/eval.
- Computes: overall accuracy, macro F1, per-class precision/recall/F1, confusion matrix.
- Produces delta metrics vs fp32 baseline (macro F1 drop %, accuracy drop %).
- Writes JSON report to artifacts/quant_eval/<timestamp>_tflite_eval.json.

Usage Example:
 python ml/compare_tflite_accuracy.py \
   --models-dir ml/outputs/run_001/quant \
   --data-root datasets/plant_v1/images \
   --meta datasets/plant_v1/meta_test.csv \
   --classes healthy,chlorosis,fungus,necrosis \
   --image-size 224

If you have a single combined meta.csv for test with columns filename,label use --meta accordingly.

Assumptions:
- Images are either under data_root/<label>/<filename> or directly under data_root/<filename> (same logic as training dataset).
- class list order provided matches training order.

"""
from __future__ import annotations
import argparse, os, json, time
from pathlib import Path
from typing import List, Dict, Tuple
import csv
import numpy as np
from PIL import Image

# ---------- Dataset Loading (Torch-free) ----------

def load_samples(data_root: Path, meta_csv: Path, class_names: List[str]) -> List[Tuple[Path,int]]:
    class_to_idx = {c: i for i, c in enumerate(class_names)}
    samples: List[Tuple[Path,int]] = []
    if not meta_csv.exists():
        raise FileNotFoundError(f"meta csv nicht gefunden: {meta_csv}")
    with meta_csv.open('r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        if 'filename' not in reader.fieldnames or 'label' not in reader.fieldnames:
            raise ValueError('meta.csv benötigt Spalten filename,label')
        for row in reader:
            label = row['label'].strip()
            fname = row['filename'].strip()
            if label not in class_to_idx:
                continue
            p = data_root / label / fname
            if not p.exists():
                alt = data_root / fname
                if alt.exists():
                    p = alt
            if p.exists():
                samples.append((p, class_to_idx[label]))
    if not samples:
        raise RuntimeError('Keine Samples geladen; prüfe Pfade.')
    return samples

# ---------- Preprocessing (match eval) ----------

def preprocess_image(path: Path, image_size: int) -> np.ndarray:
    # Resize 1.15x then center crop like eval transforms
    img = Image.open(path).convert('RGB')
    target = int(image_size * 1.15)
    img = img.resize((target, target))
    # Center crop
    left = (target - image_size)//2
    top = (target - image_size)//2
    img = img.crop((left, top, left+image_size, top+image_size))
    arr = np.array(img).astype(np.float32) / 255.0
    mean = np.array([0.485,0.456,0.406], dtype=np.float32)
    std = np.array([0.229,0.224,0.225], dtype=np.float32)
    arr = (arr - mean) / std
    # Model originally trained in NCHW; exported ONNX -> TF -> TFLite is NHWC
    arr = np.expand_dims(arr, 0)  # (1,H,W,C)
    return arr.astype(np.float32)

# For full-int8 model expecting uint8, we will quantize after normalization mapping to expected scale if metadata exists.
# Simpler approach: let TFLite runtime handle dequant since converter set inference_input_type.

# ---------- Metrics ----------

def init_confusion(n: int) -> np.ndarray:
    return np.zeros((n,n), dtype=np.int64)

def update_confusion(cm: np.ndarray, y_true: int, y_pred: int):
    if 0 <= y_true < cm.shape[0] and 0 <= y_pred < cm.shape[0]:
        cm[y_true, y_pred] += 1

def compute_metrics(cm: np.ndarray) -> Dict:
    tp = np.diag(cm).astype(float)
    fp = cm.sum(axis=0) - tp
    fn = cm.sum(axis=1) - tp
    precision = tp / (tp + fp + 1e-9)
    recall = tp / (tp + fn + 1e-9)
    f1 = 2*precision*recall / (precision + recall + 1e-9)
    per_class = {
        int(i): {
            'precision': float(precision[i]),
            'recall': float(recall[i]),
            'f1': float(f1[i]),
            'support': int(cm[i].sum())
        } for i in range(cm.shape[0])
    }
    macro_f1 = float(np.nanmean(f1))
    accuracy = float(tp.sum() / (cm.sum() + 1e-9))
    return {
        'macro_f1': macro_f1,
        'accuracy': accuracy,
        'per_class': per_class,
        'confusion': cm.tolist()
    }

# ---------- TFLite Inference ----------

def load_tflite_interpreter(model_path: Path):
    import tensorflow as tf
    return tf.lite.Interpreter(model_path=str(model_path))

def run_inference(interpreter, input_data: np.ndarray) -> int:
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()
    # Assume single input/output
    inp = input_details[0]
    tensor = input_data
    # For int8 full-quant model expecting uint8 input
    if inp['dtype'] == np.uint8:
        # De-normalize then scale to [0,255]
        # Reverse normalization: x_norm = (x - mean)/std => x = x_norm*std + mean
        mean = np.array([0.485,0.456,0.406], dtype=np.float32)
        std = np.array([0.229,0.224,0.225], dtype=np.float32)
        x = input_data.copy()
        x = x * std + mean
        x = np.clip(x, 0, 1) * 255.0
        tensor = x.astype(np.uint8)
    interpreter.resize_tensor_input(inp['index'], tensor.shape)
    interpreter.allocate_tensors()
    interpreter.set_tensor(inp['index'], tensor)
    interpreter.invoke()
    out = interpreter.get_tensor(output_details[0]['index'])
    # If quantized output, need dequantization parameters
    if output_details[0]['dtype'] != np.float32:
        scale, zero = output_details[0]['quantization']
        out = (out.astype(np.float32) - zero) * scale
    # Assume logits; take argmax
    pred = int(np.argmax(out, axis=1)[0])
    return pred

# ---------- Main ----------

def evaluate_model(model_path: Path, samples: List[Tuple[Path,int]], image_size: int) -> Dict:
    interpreter = load_tflite_interpreter(model_path)
    cm = init_confusion(len(set([s[1] for s in samples])))
    for p,label in samples:
        arr = preprocess_image(p, image_size)
        pred = run_inference(interpreter, arr)
        update_confusion(cm, label, pred)
    return compute_metrics(cm)

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--models-dir', required=True, help='Directory containing TFLite models (model_fp32.tflite etc.)')
    ap.add_argument('--data-root', required=True)
    ap.add_argument('--meta', required=True, help='meta.csv für Test/Valid set')
    ap.add_argument('--classes', required=True, help='Komma-separierte Klassen in Trainingsreihenfolge')
    ap.add_argument('--image-size', type=int, default=224)
    ap.add_argument('--out-dir', default='artifacts/quant_eval')
    ap.add_argument('--max-macro-f1-drop', type=float, default=3.0, help='Maximal erlaubter Macro-F1 Drop in Prozent (negativer Wert)')
    ap.add_argument('--max-accuracy-drop', type=float, default=3.0, help='Maximal erlaubter Accuracy Drop in Prozent (negativer Wert)')
    ap.add_argument('--min-macro-f1', type=float, default=None, help='Absolute Mindest-Macro-F1 (Baseline muss >= Wert, sonst Fail)')
    ap.add_argument('--strict', action='store_true', help='Falls gesetzt: Bei Verstoß sofort Exit Code 2 (sonst nur Warnung)')
    args = ap.parse_args()

    models_dir = Path(args.models_dir)
    if not models_dir.exists():
        raise SystemExit(f'Model dir nicht gefunden: {models_dir}')
    class_names = [c.strip() for c in args.classes.split(',') if c.strip()]

    samples = load_samples(Path(args.data_root), Path(args.meta), class_names)

    candidates = {
        'fp32': models_dir / 'model_fp32.tflite',
        'int8_dynamic': models_dir / 'model_int8_dynamic.tflite',
        'int8_full': models_dir / 'model_int8_full.tflite'
    }
    existing = {k: v for k,v in candidates.items() if v.exists()}
    if 'fp32' not in existing:
        raise SystemExit('Baseline model_fp32.tflite fehlt – zuerst quantize_ptq.py ausführen.')

    reports: Dict[str, Dict] = {}
    baseline_metrics = None
    for name, path in existing.items():
        print(f'[INFO] Evaluiere {name}: {path.name}')
        start = time.time()
        metrics = evaluate_model(path, samples, args.image_size)
        metrics['eval_seconds'] = round(time.time()-start,2)
        reports[name] = metrics
        if name == 'fp32':
            baseline_metrics = metrics

    # Compute deltas vs baseline
    if baseline_metrics:
        for name, m in reports.items():
            if name == 'fp32':
                continue
            m['delta_macro_f1_pct'] = round((m['macro_f1'] - baseline_metrics['macro_f1']) * 100.0, 2)
            m['delta_accuracy_pct'] = round((m['accuracy'] - baseline_metrics['accuracy']) * 100.0, 2)

    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    ts = time.strftime('%Y%m%d_%H%M%S')
    out_file = out_dir / f'{ts}_tflite_eval.json'
    status = {
        'macro_f1_drop_ok': True,
        'accuracy_drop_ok': True,
        'min_macro_f1_ok': True,
        'violations': []
    }
    if baseline_metrics:
        base_f1 = baseline_metrics['macro_f1']
        base_acc = baseline_metrics['accuracy']
        if args.min_macro_f1 is not None and base_f1 < args.min_macro_f1:
            status['min_macro_f1_ok'] = False
            status['violations'].append(f'baseline: macro_f1 {base_f1:.4f} < min_macro_f1 {args.min_macro_f1}')
        for variant in ('int8_dynamic','int8_full'):
            if variant in reports and 'delta_macro_f1_pct' in reports[variant]:
                d_f1 = reports[variant]['delta_macro_f1_pct']  # negative if drop
                if d_f1 < -abs(args.max_macro_f1_drop):
                    status['macro_f1_drop_ok'] = False
                    status['violations'].append(f'{variant}: macro_f1_drop {d_f1}% < -{abs(args.max_macro_f1_drop)}%')
                d_acc = reports[variant]['delta_accuracy_pct']
                if d_acc < -abs(args.max_accuracy_drop):
                    status['accuracy_drop_ok'] = False
                    status['violations'].append(f'{variant}: accuracy_drop {d_acc}% < -{abs(args.max_accuracy_drop)}%')

    overall_ok = status['macro_f1_drop_ok'] and status['accuracy_drop_ok'] and status['min_macro_f1_ok']
    status['overall_ok'] = overall_ok

    with out_file.open('w', encoding='utf-8') as f:
        json.dump({'classes': class_names, 'reports': reports, 'thresholds': {
            'max_macro_f1_drop_pct': args.max_macro_f1_drop,
            'max_accuracy_drop_pct': args.max_accuracy_drop
        }, 'status': status}, f, indent=2)
    print(f'[DONE] Bericht gespeichert: {out_file}')
    if not overall_ok:
        msg = ' | '.join(status['violations']) if status['violations'] else 'Unbekannter Schwellwert-Verstoß'
        if args.strict:
            print(f'[FAIL] Schwellenwerte verletzt: {msg}')
            raise SystemExit(2)
        else:
            print(f'[WARN] Schwellenwerte verletzt (kein Strict Mode): {msg}')

if __name__ == '__main__':
    main()
