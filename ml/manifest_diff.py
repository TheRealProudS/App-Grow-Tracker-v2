#!/usr/bin/env python3
"""Compare two manifest JSON files (leafsense_model.json) and report differences.

Outputs:
 - Human-readable table to stdout
 - Optional JSON diff (--json-out)
 - Optional gate: fail if critical fields change beyond thresholds

Critical numeric fields checked (if present in both):
  macro_f1, accuracy, delta_macro_f1_pct, delta_accuracy_pct

Example:
  python ml/manifest_diff.py --old old/leafsense_model.json --new app/src/main/assets/leafsense_model.json \
    --json-out artifacts/manifest_diff.json --fail-on-loss --max-macro-drop 0.02

Exit codes:
 0 = ok / differences within bounds
 1 = gate violation / parse error
 2 = old manifest missing (treated as first run if --allow-missing-old)
"""
from __future__ import annotations
import argparse, json, math, sys
from pathlib import Path
from typing import Any, Dict

def load(path: Path) -> Dict[str, Any]:
    return json.loads(path.read_text(encoding='utf-8'))

def fmt(v: Any) -> str:
    if isinstance(v, float):
        return f"{v:.6g}"
    return str(v)

def diff_dict(old: Dict[str, Any], new: Dict[str, Any]):
    added = {k: new[k] for k in new.keys() - old.keys()}
    removed = {k: old[k] for k in old.keys() - new.keys()}
    changed = {k: (old[k], new[k]) for k in old.keys() & new.keys() if old[k] != new[k]}
    return added, removed, changed

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--old', required=True)
    ap.add_argument('--new', required=True)
    ap.add_argument('--json-out', help='Write structured diff JSON here')
    ap.add_argument('--fail-on-loss', action='store_true', help='Fail if macro_f1 or accuracy decreased beyond tolerance')
    ap.add_argument('--max-macro-drop', type=float, default=0.01, help='Allowed absolute drop in macro_f1 (e.g. 0.01 = 1pp)')
    ap.add_argument('--max-acc-drop', type=float, default=0.01, help='Allowed absolute drop in accuracy')
    ap.add_argument('--allow-missing-old', action='store_true', help='Do not fail if old manifest missing (first run)')
    args = ap.parse_args()

    old_path = Path(args.old)
    new_path = Path(args.new)

    if not new_path.exists():
        print(f"[ERROR] New manifest missing: {new_path}", file=sys.stderr)
        sys.exit(1)
    if not old_path.exists():
        msg = f"[WARN] Old manifest not found: {old_path}"
        if args.allow_missing_old:
            print(msg + " (allowed; treating as first run)")
            sys.exit(2)
        else:
            print(msg, file=sys.stderr)
            sys.exit(1)

    try:
        old = load(old_path)
        new = load(new_path)
    except Exception as e:
        print(f"[ERROR] Failed to parse manifests: {e}", file=sys.stderr)
        sys.exit(1)

    added, removed, changed = diff_dict(old, new)

    print("=== Manifest Diff ===")
    if added:
        print("Added:")
        for k,v in added.items():
            print(f"  + {k}: {fmt(v)}")
    if removed:
        print("Removed:")
        for k,v in removed.items():
            print(f"  - {k}: {fmt(v)}")
    if changed:
        print("Changed:")
        for k,(ov,nv) in changed.items():
            print(f"  ~ {k}: {fmt(ov)} -> {fmt(nv)}")
    if not (added or removed or changed):
        print("No differences.")

    # Gating
    violated = False
    if args.fail_on_loss:
        for metric, limit in (("macro_f1", args.max_macro_drop),("accuracy", args.max_acc_drop)):
            if metric in old and metric in new:
                delta = new[metric] - old[metric]
                if delta < -limit:  # drop exceeds allowed
                    print(f"[GATE] {metric} drop {delta:.4f} exceeds allowed {-limit:.4f}", file=sys.stderr)
                    violated = True

    # Optionally write JSON diff
    if args.json_out:
        out = {
            'added': added,
            'removed': removed,
            'changed': {k: {'old': ov, 'new': nv} for k,(ov,nv) in changed.items()},
            'gate_violated': violated
        }
        Path(args.json_out).parent.mkdir(parents=True, exist_ok=True)
        Path(args.json_out).write_text(json.dumps(out, indent=2), encoding='utf-8')
        print(f"[OK] Diff JSON written: {args.json_out}")

    if violated:
        sys.exit(1)
    sys.exit(0)

if __name__ == '__main__':
    main()
