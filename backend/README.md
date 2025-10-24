# GrowTracker Backend Skeleton

Simple FastAPI ingest service for plant analysis image + metadata collection.

## Features
- POST /v1/ingest/image (multipart: image + meta JSON form field)
- SHA256 hashing & collision-safe filename
- Append-only JSONL metadata log
- CORS enabled (dev)
- API Key header auth (X-API-Key)

## Quick Start
```bash
python -m venv .venv
source .venv/bin/activate  # Windows PowerShell: .venv\\Scripts\\Activate.ps1
pip install -r backend/requirements.txt
export INGEST_API_KEY=dev-key
uvicorn backend.main:app --reload
```

Test Upload (bash example):
```bash
curl -X POST http://localhost:8000/v1/ingest/image \
  -H "X-API-Key: dev-key" \
  -F "image=@sample.jpg" \
  -F 'meta={"predictionLabel":"healthy","top1Score":0.95}'
```

Outputs:
- Stored file: data/uploads/<timestamp_hash>_sample.jpg
- Metadata line: data/meta_log.jsonl

## Security: API Key Rotation & Rate Limiting

### Key Storage
Datei: `data/keys.json`
Format:
```jsonc
{
  "keys": [
    {"key": "DEVKEY", "active": true, "name": "dev", "expires": 0},
    {"key": "OLDKEY", "active": false, "name": "rotated", "expires": 0}
  ]
}
```
`expires` = Epoch Millis (0 = kein Ablauf). Nur aktive & nicht abgelaufene Keys werden akzeptiert.

### Verwaltung
Script: `backend/manage_keys.py`

Hinzufügen (30 Tage gültig):
```bash
python backend/manage_keys.py --file data/keys.json add --key NEWKEY123 --name staging --ttl-days 30
```
Deaktivieren:
```bash
python backend/manage_keys.py --file data/keys.json deactivate --key OLDKEY
```
Auflisten:
```bash
python backend/manage_keys.py --file data/keys.json list
```

Rotation Ablauf:
1. Neuen Key hinzufügen & an Clients verteilen
2. Monitoring (Requests mit neuem Key sichtbar?)
3. Alten Key deaktivieren nach Grace-Periode

### Rate Limiting
In-Memory pro Key (Reset nach Fenster). Env Variablen:
- `RATE_LIMIT` (Standard 300)
- `RATE_WINDOW` Sekunden (Standard 3600)

Antwort Header:
- `X-RateLimit-Limit`
- `X-RateLimit-Remaining`
- Bei 429 zusätzlich: `Retry-After`

### Hinweise / Erweiterung
| Thema | Empfehlung |
|-------|------------|
| Persistenz | Redis / Upstash für horizontalen Scale |
| Burst Schutz | Token Bucket zusätzlich zum festen Fenster |
| Missbrauch | IP + Key Kombination tracken |
| Rotation Audit | Historie der Key-Änderungen versionieren |
| Attestation | SafetyNet / Play Integrity für Geräte-Vertrauen |

## Next Steps
- Add validation (MIME type, size limit)
- Move to persistent DB (Postgres) & object storage (S3/MinIO)
- Add /v1/ingest/batch for future multi-upload
- Authentication hardening (rotating keys, device attestation)
- Rate limiting & abuse detection
