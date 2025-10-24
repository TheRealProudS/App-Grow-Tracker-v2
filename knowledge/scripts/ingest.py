#!/usr/bin/env python3
"""Ingest knowledge base entries into a merged JSONL file.

Features:
- Validates against schema.json
- Accepts JSON entries (future: Markdown with front-matter)
- Outputs build/merged.jsonl + summary report

Usage:
  python knowledge/scripts/ingest.py --entries-dir knowledge/entries --out knowledge/build/merged.jsonl
"""
from __future__ import annotations
import argparse, json, sys, hashlib
from pathlib import Path
from datetime import datetime

try:
    import jsonschema  # type: ignore
except ImportError:
    jsonschema = None

SCHEMA_PATH = Path('knowledge/schema.json')

def load_schema():
    if not SCHEMA_PATH.exists():
        print('[ERROR] schema.json nicht gefunden:', SCHEMA_PATH)
        sys.exit(1)
    return json.loads(SCHEMA_PATH.read_text(encoding='utf-8'))

def validate(entry, schema):
    if jsonschema is None:
        return True, []  # Skip validation if dependency missing
    try:
        jsonschema.validate(entry, schema)
        return True, []
    except Exception as e:
        return False, [str(e)]

def compute_hash(entry):
    h = hashlib.sha256(json.dumps(entry, sort_keys=True).encode('utf-8')).hexdigest()[:12]
    return h

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--entries-dir', required=True)
    ap.add_argument('--out', required=True)
    args = ap.parse_args()

    entries_dir = Path(args.entries_dir)
    if not entries_dir.exists():
        print('[ERROR] entries-dir fehlt')
        sys.exit(1)

    schema = load_schema()
    out_path = Path(args.out)
    out_path.parent.mkdir(parents=True, exist_ok=True)

    merged = []
    errors = 0
    for p in sorted(entries_dir.glob('*.json')):
        try:
            entry = json.loads(p.read_text(encoding='utf-8'))
            ok, errs = validate(entry, schema)
            if not ok:
                print(f'[INVALID] {p.name}: {errs}')
                errors += 1
                continue
            entry['_content_hash'] = compute_hash(entry)
            merged.append(entry)
        except Exception as e:
            print('[ERROR] Datei Problem', p, e)
            errors += 1

    with out_path.open('w', encoding='utf-8') as f:
        for e in merged:
            f.write(json.dumps(e, ensure_ascii=False) + '\n')

    summary = {
        'count': len(merged),
        'invalid': errors,
        'generated_utc': datetime.utcnow().isoformat() + 'Z',
        'out_file': str(out_path)
    }
    print('[DONE] Ingest:', summary)
    # Write summary sidecar
    (out_path.parent / 'summary.json').write_text(json.dumps(summary, indent=2), encoding='utf-8')

if __name__ == '__main__':
    main()
