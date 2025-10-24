#!/usr/bin/env python3
"""Aggregate and compare multiple training runs.

Scans given run directories (or defaults to ml/outputs/*) for metrics_final.json,
pr_curve.json, roc_curve.json, lr_find.json and produces:
- comparison.csv : Tabular data of key metrics
- comparison.md  : Markdown table
- summary.json   : Structured summary
- best_by_metric_<metric>.txt listing best run per selected metrics

Usage:
  python ml/compare_runs.py --runs ml/outputs/run_* --out-dir ml/outputs/summary
  python ml/compare_runs.py --metric macro_f1 --metric test_macro_f1

Columns included:
  run, epochs, macro_f1, val_loss, test_macro_f1, test_loss, pr_auc, roc_auc,
  stopped_early, median_early_stop, lr_initial, lr_optimal_guess
"""
from __future__ import annotations
import argparse, json, glob, statistics, csv
from pathlib import Path

KEY_METRICS_DEFAULT = ["macro_f1", "test_macro_f1", "pr_auc", "roc_auc"]


def safe_load(path: Path):
    try:
        with path.open('r', encoding='utf-8') as f:
            return json.load(f)
    except Exception:
        return None


def extract_row(run_dir: Path):
    metrics = safe_load(run_dir / 'metrics_final.json') or {}
    pr = safe_load(run_dir / 'pr_curve.json') or {}
    roc = safe_load(run_dir / 'roc_curve.json') or {}
    lr_find = safe_load(run_dir / 'lr_find.json') or {}

    pr_auc = pr.get('macro_pr_auc') or pr.get('pr_auc')
    roc_auc = roc.get('macro_roc_auc') or roc.get('roc_auc')

    early_info = metrics.get('early_stopping', {})
    median_es = early_info.get('type') == 'median'
    stopped_early = early_info.get('stopped_early', False)

    row = {
        'run': run_dir.name,
        'epochs': metrics.get('epochs_completed'),
        'macro_f1': metrics.get('val', {}).get('macro_f1') or metrics.get('macro_f1'),
        'val_loss': metrics.get('val', {}).get('loss') or metrics.get('val_loss'),
        'test_macro_f1': metrics.get('test', {}).get('macro_f1'),
        'test_loss': metrics.get('test', {}).get('loss'),
        'pr_auc': pr_auc,
        'roc_auc': roc_auc,
        'stopped_early': stopped_early,
        'median_early_stop': median_es,
        'lr_initial': (lr_find.get('history')[0]['lr'] if lr_find and lr_find.get('history') else None),
        'lr_optimal_guess': lr_find.get('suggested_lr') if lr_find else None,
    }
    return row


def format_markdown(rows, headers):
    lines = []
    lines.append('| ' + ' | '.join(headers) + ' |')
    lines.append('| ' + ' | '.join(['---'] * len(headers)) + ' |')
    for r in rows:
        vals = []
        for h in headers:
            v = r.get(h)
            if isinstance(v, float):
                vals.append(f"{v:.5f}")
            else:
                vals.append(str(v))
        lines.append('| ' + ' | '.join(vals) + ' |')
    return '\n'.join(lines) + '\n'


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--runs', nargs='*', help='Glob patterns of run directories (default ml/outputs/*)')
    ap.add_argument('--out-dir', default='ml/outputs/summary')
    ap.add_argument('--metric', action='append', help='Metric(s) to highlight best run')
    args = ap.parse_args()

    run_dirs = []
    patterns = args.runs or ['ml/outputs/*']
    for pat in patterns:
        for p in glob.glob(pat):
            if Path(p).is_dir():
                run_dirs.append(Path(p))
    run_dirs = sorted(set(run_dirs))
    if not run_dirs:
        raise SystemExit('Keine Runs gefunden.')

    rows = [extract_row(rd) for rd in run_dirs]

    headers = ['run','epochs','macro_f1','val_loss','test_macro_f1','test_loss','pr_auc','roc_auc','stopped_early','median_early_stop','lr_initial','lr_optimal_guess']

    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    # CSV
    with (out_dir / 'comparison.csv').open('w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=headers)
        writer.writeheader()
        for r in rows:
            writer.writerow(r)

    # Markdown
    md = format_markdown(rows, headers)
    (out_dir / 'comparison.md').write_text(md, encoding='utf-8')

    # Summary JSON
    summary = { 'rows': rows, 'count': len(rows) }
    (out_dir / 'summary.json').write_text(json.dumps(summary, indent=2), encoding='utf-8')

    metrics_for_best = args.metric or KEY_METRICS_DEFAULT
    for metric in metrics_for_best:
        valid = [r for r in rows if r.get(metric) is not None]
        if not valid:
            continue
        reverse = True  # Higher is better by default
        if 'loss' in metric.lower():
            reverse = False
        best = sorted(valid, key=lambda r: r.get(metric), reverse=reverse)[0]
        (out_dir / f'best_by_metric_{metric}.txt').write_text(best['run'], encoding='utf-8')

    print(f"[OK] {len(rows)} Runs verglichen -> {out_dir}")

if __name__ == '__main__':
    main()
