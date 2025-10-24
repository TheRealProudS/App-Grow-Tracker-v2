#!/usr/bin/env python3
"""Build unified CI report aggregating manifest, diff, evaluation metrics and history.

Inputs (auto-discovered if not provided):
  --manifest app/src/main/assets/leafsense_model.json
  --diff-json artifacts/manifest_diff.json (optional)
  --eval-glob artifacts/quant_eval/*_tflite_eval.json (pick latest)
  --history artifacts/metrics_history.csv (optional)

Outputs:
  artifacts/ci_report.json
  artifacts/ci_report.md
"""
from __future__ import annotations
import argparse, json, csv, time
from pathlib import Path
from typing import Any, Dict, Optional

def load_json(path: Path) -> Dict[str, Any]:
    return json.loads(path.read_text(encoding='utf-8'))

def newest(glob: str) -> Optional[Path]:
    files = sorted(Path('.').glob(glob))
    return files[-1] if files else None

def load_history_last(history: Path) -> Optional[Dict[str,str]]:
    try:
        with history.open('r', encoding='utf-8', newline='') as f:
            rows = list(csv.DictReader(f))
        return rows[-1] if rows else None
    except Exception:
        return None

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--manifest', default='app/src/main/assets/leafsense_model.json')
    ap.add_argument('--diff-json', default='artifacts/manifest_diff.json')
    ap.add_argument('--eval-glob', default='artifacts/quant_eval/*_tflite_eval.json')
    ap.add_argument('--history', default='artifacts/metrics_history.csv')
    ap.add_argument('--out-json', default='artifacts/ci_report.json')
    ap.add_argument('--out-md', default='artifacts/ci_report.md')
    args = ap.parse_args()

    manifest_path = Path(args.manifest)
    if not manifest_path.exists():
        raise SystemExit(f"Manifest not found: {manifest_path}")
    manifest = load_json(manifest_path)

    diff_path = Path(args.diff_json)
    diff = load_json(diff_path) if diff_path.exists() else None

    eval_path = newest(args.eval_glob)
    eval_data = load_json(eval_path) if eval_path else None

    history_path = Path(args.history)
    last_history = load_history_last(history_path) if history_path.exists() else None

    reports = (eval_data or {}).get('reports', {})
    # Choose variant metrics in manifest based on model_file
    model_file = manifest.get('model_file')
    variant_key = None
    if model_file:
        if 'int8_full' in model_file:
            variant_key = 'int8_full'
        elif 'int8_dynamic' in model_file:
            variant_key = 'int8_dynamic'
        else:
            variant_key = 'fp32'
    variant_metrics = reports.get(variant_key, {}) if variant_key else {}

    summary = {
        'timestamp': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime()),
        'model_version': manifest.get('model_version'),
        'model_file': model_file,
        'model_sha256': manifest.get('model_sha256'),
        'input_size': manifest.get('input_size') or manifest.get('inputSize'),
        'quantization': manifest.get('quantization'),
        'classes_count': len(manifest.get('classes', [])),
        'knowledge_entry_count': (manifest.get('knowledge') or {}).get('entry_count'),
        'metrics': {
            'macro_f1': variant_metrics.get('macro_f1'),
            'accuracy': variant_metrics.get('accuracy'),
            'delta_macro_f1_pct': variant_metrics.get('delta_macro_f1_pct'),
            'delta_accuracy_pct': variant_metrics.get('delta_accuracy_pct'),
            'baseline_fp32_macro_f1': variant_metrics.get('baseline_fp32_macro_f1') or manifest.get('baseline_fp32_macro_f1')
        },
        'diff': diff,
        'latest_eval_file': str(eval_path) if eval_path else None,
        'history_last': last_history,
    }

    # Determine gate_violation if diff JSON recorded one
    gate_violated = diff.get('gate_violated') if diff else False
    summary['gate_violated'] = gate_violated

    out_json_path = Path(args.out_json)
    out_json_path.parent.mkdir(parents=True, exist_ok=True)
    out_json_path.write_text(json.dumps(summary, indent=2), encoding='utf-8')

    # Markdown report
    md_lines = []
    md_lines.append(f"# CI Model Report\n")
    md_lines.append(f"**Version:** {summary['model_version']}  ")
    md_lines.append(f"**Model File:** {summary['model_file']}  ")
    if summary.get('model_sha256'):
        md_lines.append(f"**Model SHA256:** `{summary['model_sha256']}`  ")
    md_lines.append(f"**Quantization:** {summary['quantization']}  ")
    md_lines.append(f"**Classes:** {summary['classes_count']}  ")
    if summary['knowledge_entry_count'] is not None:
        md_lines.append(f"**Knowledge Entries:** {summary['knowledge_entry_count']}  ")
    m = summary['metrics']
    md_lines.append("\n## Metrics\n")
    for k in ('macro_f1','accuracy','delta_macro_f1_pct','delta_accuracy_pct','baseline_fp32_macro_f1'):
        if m.get(k) is not None:
            md_lines.append(f"- {k}: {m[k]}")
    if diff:
        md_lines.append("\n## Manifest Diff\n")
        for section in ('added','removed','changed'):
            part = diff.get(section)
            if part:
                md_lines.append(f"### {section.capitalize()}\n")
                if section == 'changed':
                    for ck, cv in part.items():
                        if isinstance(cv, dict) and 'old' in cv and 'new' in cv:
                            md_lines.append(f"- {ck}: {cv['old']} -> {cv['new']}")
                        else:
                            md_lines.append(f"- {ck}: {cv}")
                else:
                    for ak,av in part.items():
                        md_lines.append(f"- {ak}: {av}")
    if last_history:
        md_lines.append("\n## Last History Entry\n")
        md_lines.append("| Field | Value |")
        md_lines.append("|-------|-------|")
        for k,v in last_history.items():
            md_lines.append(f"| {k} | {v} |")
    md_lines.append("\nGate Violated: **{}**\n".format('YES' if gate_violated else 'NO'))

    Path(args.out_md).write_text('\n'.join(md_lines), encoding='utf-8')
    print(f"[OK] CI report written: {out_json_path} and {args.out_md}")

if __name__ == '__main__':
    main()
