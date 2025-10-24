#!/usr/bin/env python3
"""Append latest quant eval summary to CSV history.

Searches for newest artifacts/quant_eval/*_tflite_eval.json and extracts:
 timestamp, model_variant, macro_f1, accuracy, delta_macro_f1_pct, delta_accuracy_pct
Baseline (fp32) always written; quantized variants if present.

Usage:
  python ml/append_metrics_history.py --csv artifacts/metrics_history.csv
"""
from __future__ import annotations
import argparse, json
from pathlib import Path

FIELDS = ["timestamp","variant","macro_f1","accuracy","delta_macro_f1_pct","delta_accuracy_pct"]

def latest_report(glob_pattern: str) -> Path | None:
    paths = sorted(Path('artifacts/quant_eval').glob(glob_pattern))
    return paths[-1] if paths else None

def parse_report(path: Path):
    data = json.loads(path.read_text(encoding='utf-8'))
    reports = data.get('reports', {})
    ts = path.name.split('_tflite_eval.json')[0]
    rows = []
    for variant, metrics in reports.items():
        rows.append({
            'timestamp': ts,
            'variant': variant,
            'macro_f1': metrics.get('macro_f1'),
            'accuracy': metrics.get('accuracy'),
            'delta_macro_f1_pct': metrics.get('delta_macro_f1_pct'),
            'delta_accuracy_pct': metrics.get('delta_accuracy_pct')
        })
    return rows

def write_rows(csv_path: Path, rows):
    header_needed = not csv_path.exists()
    with csv_path.open('a', encoding='utf-8') as f:
        if header_needed:
            f.write(','.join(FIELDS) + '\n')
        for r in rows:
            f.write(','.join(str(r.get(k,'')) for k in FIELDS) + '\n')


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--csv', default='artifacts/metrics_history.csv')
    args = ap.parse_args()

    report = latest_report('*_tflite_eval.json')
    if not report:
        print('[INFO] Kein Eval Report gefunden.')
        return
    rows = parse_report(report)
    if not rows:
        print('[INFO] Keine Metriken in Report.')
        return
    csv_path = Path(args.csv)
    csv_path.parent.mkdir(parents=True, exist_ok=True)
    write_rows(csv_path, rows)
    print(f'[OK] {len(rows)} Zeilen angehängt an {csv_path}')

if __name__ == '__main__':
    main()
