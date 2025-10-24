# ML / Training Scaffold (LeafSense)

Dieses Verzeichnis enthält den Startpunkt für das Training des On-Device Pflanzen-Diagnosemodells.

## Ziele
- Reproduzierbares Training (Konfigurationen versioniert)
- Export nach TFLite (mit optionaler Quantisierung)
- Kennzahlen: Macro F1, Per-Class Recall, Inference Zeit
- Skalierbare Erweiterung (Distillation, Multi-Label später)
 - Effiziente Nutzung (AMP Mixed Precision + Warmup/Cosine Scheduler)

## Struktur
```
ml/
  README.md
  train.py              # Einstiegsskript (Stub)
  configs/
    baseline.yaml       # Beispiel-Konfig
  outputs/              # Modellartefakte & Logs (nicht im Repo committen -> .gitignore empfohlen)
```
```

## Baseline Workflow
1. Virtuelle Umgebung & Abhängigkeiten installieren:
  ```bash
  python -m venv .venv
3. Config prüfen/anpassen: `ml/configs/baseline.yaml` (Klassenanzahl, Pfade, Augmentierungen).
4. Training starten:
  ```bash
  python ml/train.py --config ml/configs/baseline.yaml --out ml/outputs/run_001
  ```
5. (Optional vorher) Splits erzeugen (falls noch nicht vorhanden) mit stratifizierter Aufteilung:
  ```powershell
  python ml/build_splits.py --meta ml/data/metadata/metadata.jsonl --train-ratio 0.7 --val-ratio 0.15 --test-ratio 0.15
  ```
  Output:
  - `ml/data/splits/train.txt`, `val.txt`, `test.txt` (Zeilen: `rel/path.jpg<TAB>Label`)
  - `ml/data/splits/stats.json` (Klassenverteilung & effektive Ratios)

6. Ergebnisse:
  - `best.pt` (Gewichte mit bestem Macro F1)
  - `metrics_final.json` (Confusion Matrix + per-class Kennzahlen)
  - Optional `model.onnx` (bei erfolgreichem Export)
7. TFLite aktuell nur Placeholder → echte Konvertierung folgt.

Quellenoptionen:
1. Kopie aus App Assets:
  - Pfad: `app/src/main/assets/models/labels_v1.json`
  - Kopieren nach: `ml/labels_v1.json`
2. Generieren aus eigener Klassenliste (nur falls Reihenfolge 100% übereinstimmt mit App-Version!).

Prüfung vor Training (PowerShell):
```powershell
Compare-Object -ReferenceObject (Get-Content app/src/main/assets/models/labels_v1.json | ConvertFrom-Json) -DifferenceObject (Get-Content ml/labels_v1.json | ConvertFrom-Json)
```
Wenn keine Ausgabe erfolgt → identisch.

### Zusätzliche Trainingsoptionen
Neue hilfreiche Flags (CLI Parameter, nicht YAML):
- `--fast-dev-run` führt nur 2 Trainingsbatches + 1 Val-Batch aus (schneller Smoke-Test der Pipeline)
- `--resume path/to/best.pt` setzt Training nach letztem Best-Checkpoint fort

Beispiele (PowerShell):
python ml/train.py --config ml/configs/baseline.yaml --out ml/outputs/run_010

python ml/train.py --config ml/configs/baseline.yaml --out ml/outputs/dev_smoke --fast-dev-run

# Resume nach Abbruch (Checkpoint best.pt)
python ml/train.py --config ml/configs/baseline.yaml --out ml/outputs/run_010 --resume ml/outputs/run_010/best.pt

# Deterministisch (Debug / Vergleich)
python ml/train.py --config ml/configs/baseline.yaml --out ml/outputs/run_det --deterministic

# Mit TensorBoard Logging
python ml/train.py --config ml/configs/baseline.yaml --out ml/outputs/run_tb --log-dir tb_logs/run_tb

# Learning Rate Finder
python ml/train.py --config ml/configs/baseline.yaml --out ml/outputs/lr_probe --lr-find --lr-find-min 1e-5 --lr-find-max 1.0 --lr-find-steps 150
```

```jsonc
"test": {
  "per_class": {"0": {"precision": ...}, ...}
}
```

### Class Weights
Im YAML unter `loss.class_weights` entweder:
```yaml
loss:
  class_weights: [1.0, 1.2, 0.8, 1.5, 1.0, 1.0, 1.3, 1.0, 0.9, 1.0]
```
oder Pfad zu JSON-Datei mit einer Liste. Wird geladen → `CrossEntropyLoss(weight=...)`.

### TensorBoard Anzeigen
Start lokal:
```powershell
tensorboard --logdir tb_logs
```
Anzeigen: Scalars (Loss, Macro F1, LR), Images (Confusion Matrix), optional LR Finder Kurve (`lr_find`).

### Precision/Recall Kurve (Macro)
Aktiviere mit `--pr-curve`. Ergebnis: `pr_curve.json` mit Feldern
```jsonc
{
  "thresholds": [...],
  "macro_precision": [...],
  "macro_recall": [...],
  "macro_pr_auc": 0.8931
}
```
Bei aktiviertem TensorBoard wird zusätzlich ein Bild (`val/macro_pr_curve`) geloggt.

### ROC Kurve (Macro One-vs-Rest)
Aktiviere mit `--roc-curve`. Ergebnis: `roc_curve.json` mit Feldern
```jsonc
{
  "fpr": [...],
  "tpr": [...],
  "macro_roc_auc": 0.9214
}
```
In TensorBoard (falls `--log-dir` aktiv) wird zusätzlich ein Bild (`val/macro_roc_curve`) geloggt.

Interpretation:
- PR AUC besser bei Klassen-Imbalance Fokus (Fehlerreduktion bei positiven Fällen)
- ROC AUC robuster bei sehr unausgeglichenem Thresholding Überblick
Empfehlung: Beide lesen, aber PR AUC bevorzugt für stark unbalancierte Datensätze.

### Fehlklassifikationen Export
Flag: `--save-miscls K` speichert die Top-K Fehlklassifikationen (höchster Loss) aus dem Validierungs-Set als Thumbnails unter `misclassifications/` im Run-Ordner.
Dateinamen Muster: `rankXX_loss{:.4f}_true{c}_pred{p}.jpg`

Verwendung (z.B. Top 24):
```powershell
python ml/train.py --config ml/configs/baseline.yaml --out ml/outputs/run_mis --save-miscls 24 --pr-curve --roc-curve
```

Tipps:
- Hohe False Positives derselben vorhergesagten Klasse → Konfusionsmatrix + Augmentierung prüfen
- Viele sehr ähnliche falsch-negative Bilder → Datendrift / Augmentierung erweitern

### Grad-CAM Visualisierung
Skript: `ml/grad_cam.py`

Beispiel (ein Bild):
```powershell
python ml/grad_cam.py --checkpoint ml/outputs/run_010/best.pt --image samples/leaf_a.jpg --out cam_leaf_a.jpg
```
Mehrere Bilder in Verzeichnis:
```powershell
python ml/grad_cam.py --checkpoint ml/outputs/run_010/best.pt --image samples/a.jpg --image samples/b.jpg --out-dir cams
```
Ausgabe: Heatmap Overlay (`_cam_clsX`). Nutzt letzte Conv2d (MobileNetV3 Small). Für andere Architekturen ggf. Layer-Auswahl erweitern (TODO Erkennung verbessern).

Interpretation Hinweise:
- Aktivierungen fokussieren sich auf irrelevanten Hintergrund? → Crop / Fokus-Augmentierung verstärken
- Diffuse CAM ohne klaren Hotspot → Modell unsicher / Klasse visuell heterogen

### Multi-Run Vergleich
Skript: `ml/compare_runs.py`
Aggregiert Kennzahlen mehrerer Läufe (standardmäßig `ml/outputs/*`).
## Modell Export & Quantisierung

Skript: `ml/export_model.py`

Beispiel (TorchScript + ONNX + Representative Set):
```powershell
python ml/export_model.py --checkpoint ml/experiments/run_123/best.pt --labels-json ml/labels_v1.json --outdir ml/exports/run_123
```

Ergebnis:
- `model_ts.pt` (TorchScript traced)
- `model.onnx` (Opset 17, dynamische Batch-Achse)
- `representative_set.json` (Liste relativer Trainingsbild-Pfade für INT8 PTQ)
- `labels.json` (Klassenreihenfolge)
- `export_config.json` (Provenance Manifest: Hashes, Größen, Timestamp, Input-Shape)

INT8 TFLite (Stub Workflow – extern ausführen):
1. ONNX -> TF Graph (onnx-tf) oder direkt nach TFLite via PyTorch -> TorchScript -> FX -> TFLite Pipeline (zukünftig).
2. Representative Dataset Loader: sample Bilder aus `representative_set.json`, nach (224,224) preprocessen, normalisieren.
3. Converter mit `optimizations=[tf.lite.Optimize.DEFAULT]` und `representative_dataset` Callback.

Kalibrierungskonflikte vermeiden: Representative Set darf keine Val/Test Bilder enthalten.

### Provenance / Integritätsprüfung
`export_config.json` enthält SHA-256 Hashes der generierten Artefakte.

Schneller Check (PowerShell):
```powershell
Get-FileHash ml/exports/run_123/model.onnx -Algorithm SHA256 | Select-Object Hash
Get-Content ml/exports/run_123/export_config.json | ConvertFrom-Json | Select-Object -ExpandProperty export | ConvertTo-Json -Depth 5
```
Vergleich automatisieren (Pseudocode): Hash aus Manifest == berechneter Hash → OK.

Verwendungszweck:
- Reproduzierbarkeit (Dokumentation der genauen Bytes)
- Lieferketten-Sicherheit (Sicherstellen, dass Modell nicht manipuliert wurde)
- Build-Pipeline Gate (CI kann Hash matchen bevor Deployment signiert wird)

#### Environment Abschnitt
Beispielauszug aus `export_config.json`:
```json
{
  "environment": {
    "python_version": "3.11.6",
    "platform": "Windows-10-10.0.22631-SP0",
    "processor": "Intel64 Family 6 Model ...",
    "torch_version": "2.2.0",
    "packages": {
      "torchvision": "0.17.0",
      "numpy": "1.26.2",
      "pillow": "10.0.1",
      "sklearn": "1.3.2",
      "onnx": "1.15.0"
    }
  }
}
```
Nutzen:
- Rebuild Audit: Sicherstellen, dass identische Toolchain genutzt wurde
- Kompatibilitätsanalyse: Auffällige Divergenzen (z.B. andere ONNX Version) früh erkennen
- Langfristige Nachvollziehbarkeit für Modellkaskaden / Regressionen

#### Benchmark Abschnitt
Optional durch `--benchmark` beim Export erzeugt:
```json
"benchmark": {
  "device": "cpu",
  "warmup": 5,
  "iters": 30,
  "mean_ms": 6.23,
  "p50_ms": 6.11,
  "p90_ms": 6.58,
  "p95_ms": 6.72,
  "std_ms": 0.21
}
```
Interpretation:
- `mean_ms` / `p50_ms`: typischer Einzel-Inferenz-Latenzwert.
- `p90_ms` / `p95_ms`: Tail (UI Budget planen; > frame budget? ggf. reduce model size / quantisieren).
- Schwankungen (`std_ms`) > 30% des Mittelwerts → Warmup/Affinität/Power-Settings prüfen.

Empfehlungen:
- Ziel für flüssige Live-Analyse: < 15ms (reine Modellzeit) auf Zielgerät.
- Falls > 25ms: INT8 oder kleinere Architektur (MobileNetV3-Small / EfficientNet-Lite0) erwägen.

Zusätzliche Optionen:
- `--device cuda` (falls verfügbar) misst GPU Latenz (nur Benchmark – Artefakte bleiben CPU export).

`model_stats` Abschnitt (immer vorhanden):
```json
"model_stats": {
  "total_params": 5478936,
  "trainable_params": 5478936
}
```
Wenn `trainable_params` << `total_params` → Teile eingefroren (Fine-Tuning / Feature Extraction Mode).

### Automatisiertes Verifizieren
Skript: `ml/verify_export.py`

Beispiel:
```powershell
python ml/verify_export.py --manifest ml/exports/run_123/export_config.json

# Mit JSON Summary für CI Parsing
python ml/verify_export.py --manifest ml/exports/run_123/export_config.json --json-out ml/exports/run_123/verify_summary.json
```
Exit Codes:
- 0: OK
- 1: Datei fehlt
- 2: Hash Mismatch
- 3: Manifest ungültig
- 4: Environment Diff (nur wenn `--fail-on-env-diff` gesetzt)

CI Integration (GitHub Actions YAML Ausschnitt):
```yaml
    - name: Verify model artifacts
      run: |
        python ml/verify_export.py --manifest ml/exports/run_123/export_config.json
    - name: Persist verification JSON
      if: always()
      run: |
        python ml/verify_export.py --manifest ml/exports/run_123/export_config.json --json-out ml/exports/run_123/verify_summary.json
    - name: Compare environment provenance
      run: |
        python ml/verify_export.py --manifest ml/exports/run_123/export_config.json --baseline ml/exports/prev_run/export_config.json --fail-on-env-diff --json-out ml/exports/run_123/verify_env.json
    - name: Upload verification summary
      uses: actions/upload-artifact@v4
      with:
        name: model-verify
        path: ml/exports/run_123/verify_summary.json
```

## Representative Dataset Auswahl
Empfehlungen:
- Größe: 200–500 Bilder decken Farb- & Belichtungsvariation ab.
- Stratifikation: min. 5–10 pro Klasse falls verfügbar; rest nach Diversität (Licht, Hintergrund, Blattposition).
- Ausschlüsse: OOD / Negative nicht für Stage-1 Klassifizierer (nur in Stage-0 Filter Kalibrierung verwenden).

Automatische Vorschlags-Erstellung (bereits implementiert – Zufalls-Subset):
`representative_set.json` wird beim Export erstellt (Parameter `--rep-limit`). Für feinere Kontrolle künftig Ranking nach Aktivations-Embeddings (TODO).


Beispiel:
```powershell
python ml/compare_runs.py --runs ml/outputs/run_* --out-dir ml/outputs/summary
```
Erzeugt:
- `comparison.csv`
- `comparison.md`
- `summary.json`
- `best_by_metric_macro_f1.txt`, etc.

Custom Fokusmetriken:
```powershell
python ml/compare_runs.py --metric macro_f1 --metric test_macro_f1 --metric pr_auc --metric roc_auc
```

Interpretation:
- `stopped_early=true` + hohe Macro-F1 → Early Stop sinnvoll
- `median_early_stop=true` zeigt Nutzung des median-basierten Mechanismus
- Vergleich `pr_auc` vs `macro_f1`: Divergenz kann auf Entscheidungsschwellenbedarf hinweisen

## Erweiterungen (geplant)
- Hydra für Config Compositions
- Quantization Aware Training (QAT)
- Knowledge Distillation (größeres Teacher-Modell)
- Multi-Label (Sigmoid + BCE)
- Active Learning (Unsicherheits-Queue)
- Repräsentatives Subset Export für PTQ
 - Inference Benchmark Skript

## Metriken (aktuell)
| Metrik | Zweck |
| ------ | ----- |
| macro_f1 | Klassenunabhängige Gesamtgüte |
| per_class (precision/recall/f1) | Diagnose-Sensitivität je Klasse |
| confusion | Fehlzuordnungsanalyse |

Geplant zusätzlich: inference_ms, model_size_mb, top-k accuracy.

## Export (Status)
ONNX Export integriert (opset 17). TFLite derzeit Platzhalter.
Geplanter Pfad:
Torch → ONNX → (optional onnxsim) → ONNX→TF (onnx2tf) → TFLite Converter → Int8 PTQ (repr. Set) → Optionale QAT/Distillation.

### ONNX Vereinfachung
- Aktivierung: `export.simplify_onnx: true` (führt onnx-simplifier direkt nach Export aus)
- Separat manuell: `python ml/simplify_onnx.py --input model.onnx`
Vorteile:
- Entfernt redundante Knoten → kleineres Modell
- Potenziell schnellere Laufzeit (weniger Operatoren)
- Besser für nachfolgende Konvertierung / Quantisierung stabil

## Mixed Precision & Scheduler
- Aktivierung: `experiment.mixed_precision: true` (nur wirksam bei CUDA)
- Warmup: Linear von 0 → LR über `optimizer.warmup_epochs`
- Cosine Decay: Falls `optimizer.cosine_schedule: true`, Übergang nach Warmup bis `optimizer.min_lr`
- Verlauf wird in `metrics_final.json` (`lr_history`) gespeichert.

## Evaluation
Nach Abschluss des Trainings:
```bash
python ml/eval.py \
  --config ml/configs/baseline.yaml \
  --root datasets/plant_v1/images \
  --meta datasets/plant_v1/meta.csv \
  --checkpoint ml/outputs/run_001/best.pt \
  --out ml/outputs/run_001
```
Erzeugt: `eval_results.json` mit `val` & `test` Kennzahlen (loss, macro_f1, per_class, confusion).
Wichtig: Split wird mit dem selben Seed (42) rekonstruiert – eigene Split-Strategie später möglich.

## Benchmark (Inference Performance)
Script: `ml/benchmark_infer.py`

Beispiel:
```bash
python ml/benchmark_infer.py \
  --config ml/configs/baseline.yaml \
  --checkpoint ml/outputs/run_001/best.pt \
  --onnx ml/outputs/run_001/model.onnx \
  --repeats 200
```
Ergebnis: `benchmark_results.json` (p50/p90/p95/p99, mean, FPS, load_ms) pro Backend.

Interpretation:
- Cold Load (load_ms) hoch? → Lazy Gewichtsladung / Modell verkleinern.
- Hohe p95 vs p50 Differenz → Jitter, eventuell Garbage Collection / GPU Frequency Scaling.
- FPS < Ziel (z.B. 15fps)? → Quantisierung, kleinere Architektur, Batch=1 optimieren, Operator-Fusion.

Zielrichtwerte (Beispiel für Midrange-Gerät):
- mean_ms < 40ms (25 FPS möglich)
- p95_ms < 60ms
- ONNX Runtime CPU sollte nahe PyTorch liegen; GPU unverhältnismäßig langsam -> Kernel-Fallback prüfen.

## Quantisierung (PTQ)
Skript: `ml/quantize_ptq.py`

Beispiel (Dynamic + Full Int8):
```bash
python ml/quantize_ptq.py \
  --onnx ml/outputs/run_001/model_simplified.onnx \
  --repr-dir datasets/plant_v1/images/healthy \
  --count 128 \
  --out ml/outputs/run_001/quant
```
Erzeugt:

Hinweise:

Nächste Schritte (später): QAT (Fake Quant), Distillation vor Quantisierung, Layerfusion.

## TFLite Genauigkeits-Vergleich (FP32 vs Int8)
Script: `ml/compare_tflite_accuracy.py`

Zweck: Validiert, ob die quantisierten Modelle (Dynamic / Full Int8) einen akzeptablen Genauigkeitsverlust gegenüber dem FP32-TFLite Baseline-Modell haben.

Beispiel:
```bash
python ml/compare_tflite_accuracy.py \
  --models-dir ml/outputs/run_001/quant \
  --data-root datasets/plant_v1/images \
  --meta datasets/plant_v1/meta_test.csv \
  --classes healthy,chlorosis,fungus,necrosis \
  --image-size 224
```
Erzeugt: `artifacts/quant_eval/<timestamp>_tflite_eval.json`

JSON Struktur (Auszug):
```jsonc
{
  "classes": ["healthy", ...],
  "reports": {
    "fp32": {"macro_f1": 0.91, "accuracy": 0.92, ...},
    "int8_dynamic": {"macro_f1": 0.905, "delta_macro_f1_pct": -0.5, ...},
    "int8_full": {"macro_f1": 0.908, "delta_macro_f1_pct": -0.2, ...}
  }
}
```

Interpretation:
- `delta_macro_f1_pct`: Veränderung relativ zur FP32 Basis (negativ = Verlust)
- Faustregel: |delta_macro_f1_pct| <= 3.0 akzeptabel (meist unsichtbar für User)
- Größerer Drop? Maßnahmen:
  1. Repräsentatives Set vergrößern (>=256 Bilder, divers)
  2. ONNX Vereinfachung aktiv lassen (reduziert Quant-Rauschen)
  3. Full Int8 nur nutzen, wenn dynamische Int8 nicht ausreichend verkleinert / beschleunigt
  4. Danach: QAT oder Distillation (Teacher=FP32, Student=int8-freundlich)

Tipps zur Beschleunigung:
- Full Int8 lohnt sich besonders auf ARM CPUs; Dynamic Int8 ist schneller zu testen.
- Ergänzende Laufzeitmessung: später native TFLite In-App Benchmark.

Geplante Ergänzungen:
- Automatische Warnung bei >3% Macro-F1 Verlust
- Zusammenführung mit Trainingsreport
- Optionale ROC/PR Kurven

---

## Knowledge Distillation (Teacher -> Student)
Script: `ml/distill.py`

Motivation:
- Erhalte Genauigkeit eines größeren/teuren Teacher-Modells bei geringerer Latenz / Größe
- Glättet Targets (Temperatur >1) → robustere Student-Verteilung, oft besser unter Quantisierung

Konfig (`distillation` Block in `baseline.yaml`):
```yaml
distillation:
  enabled: true
  teacher_checkpoint: ml/outputs/run_teacher/best.pt  # oder null (lädt Arch frisch)
  teacher_arch: mobilenet_v3_large
  temperature: 4.0
  alpha: 0.7
  freeze_teacher: true
  student_from_scratch: true
  kd_loss: kl_div
```

Formel:
Loss = alpha * CE(Student, HardLabel) + (1-alpha) * KL( softmax(S/T) || softmax(Tch/T) ) * T^2

Aufruf:
```bash
python ml/distill.py \
  --config ml/configs/baseline.yaml \
  --out ml/outputs/run_002_distill \
  --teacher-checkpoint ml/outputs/run_teacher/best.pt
```
Ausgaben:
- `best.pt` (Student)
- `metrics_final.json` (Train/Val Verlauf)
- `distill_meta.json` (eingefrorene KD Parameter)

Tuning Hinweise:
- alpha hoch (>=0.7): stärkerer Fokus auf harte Labels (wenn Teacher overfittet befürchtet)
- alpha kleiner (0.3–0.5): mehr Wissenstransfer aus Teacher Verteilungen
- Temperatur 2–6 typischer Bereich; zu hoch → sehr flache Verteilung, zu wenig Signal

Wenn Teacher fehlt / Pfad ungültig → Fallback auf normales Training (Warnung). Für effektive Distillation sollte Teacher > Student Macro-F1 liefern (≥2–3 Punkte Differenz).

Nächste Schritte (optional):
- Distillation vor PTQ durchführen → dann quantisieren
- Kombination mit QAT (Teacher FP32, Student FakeQuant)
- Logits Caching (Teacher einmal vorab laufen, beschleunigt große Datensätze)

---

## Android On-Device Inference (Deployment)
Dieser Abschnitt beschreibt, wie das exportierte Modell (FP32 oder Int8) in die App integriert wird.

### Dateinamen & Varianten
Nach PTQ (`ml/quantize_ptq.py`) stehen typischerweise folgende Artefakte unter `ml/outputs/<run>/quant/` zur Verfügung:
- `model_fp32.tflite` – Baseline (höhere Genauigkeit, größere Datei)
- `model_int8_dynamic.tflite` – Dynamische Quantisierung (kleiner, schneller; Aktivierungen bleiben float)
- `model_int8_full.tflite` – Voll-Int8 (Gewichte + Aktivierungen), maximale Größen- & CPU-Latency-Reduktion

Typische Größen (Beispiel – variiert nach Architektur):
- FP32: 4–12 MB
- Dynamic Int8: ~75–80% der FP32 Größe
- Full Int8: ~25–35% der FP32 Größe

### Auswahlstrategie
1. Bevorzuge `model_int8_full.tflite` wenn Macro-F1 Drop <= 3% (siehe Vergleichsskript).
2. Sonst verwende `model_int8_dynamic.tflite` wenn Drop akzeptabel, aber Full Int8 zu großem Qualitätsverlust führt.
3. Fallback auf `model_fp32.tflite` wenn keine Quant-Variante die Qualitätskriterien erfüllt.

### Integration in Assets
Kopiere die gewählte Datei in: `app/src/main/assets/leafsense_model.tflite`
Erstelle / Aktualisiere Labels-Datei: `app/src/main/assets/leafsense_labels.txt` (eine Label-Zeile pro Klasse in Trainingsreihenfolge).

Beispiel Labels:
```
healthy
nitrogen_deficiency
overwatering
pests_risk
leaf_spot
ph_imbalance
```

### Analyzer Verhalten
Die Klasse `TFLiteLeafSenseAnalyzer` lädt beim ersten Aufruf Modell + Labels.
Falls Laden fehlschlägt:
- Rückgabe eines Platzhalter-Resultats (Dummy Hinweis + „Demo Healthy“).
- UI kann optional einen Fehler-Badge anzeigen (Future Work: Telemetrie / Logging).

### Performance Hinweise
- CPU Pfad: Int8 Full > Dynamic Int8 > FP32 (Latenz). Unterschiede geräteabhängig (ARMv8 NEON / AArch64).
- Auf GPU (TFLite GPU Delegate) sind Int8 Modelle nicht immer schneller; ggf. FP16 Konvertierung für GPU überlegen.
- Vermeide mehrfaches Neu-Instantiieren des Interpreters – einmal halten (bereits umgesetzt via lazy load).
- Messung: Später separates In-App Benchmark (Frames pro Sekunde & Wartezeit).

### Optional: NNAPI / GPU Delegates
Delegates können später bedingt aktiviert werden:
```kotlin
// Beispiel (Pseudo):
val options = Interpreter.Options()
// options.addDelegate(NnApiDelegate())  // Achtung: zusätzliche Abhängigkeit nötig
// options.addDelegate(GpuDelegate())    // Für FP16 Float-Modelle sinnvoll
```
Nur aktivieren, wenn tatsächlich messbarer Gewinn; sonst Overhead vermeiden.

### Validierungsschritte vor Release
1. `compare_tflite_accuracy.py` ausführen – Deltas prüfen.

| Problem | Ursache | Lösung |
|---------|---------|-------|
| Nur Dummy Ergebnisse | Datei nicht korrekt benannt oder fehlt | Prüfe `leafsense_model.tflite` im assets Ordner |
| Falsche Klassennamen | Reihenfolge / Inhalt der Labels-Datei stimmt nicht | Labels in Trainingsreihenfolge exportieren |
| Schlechte Genauigkeit auf Gerät | Beleuchtung / Domain Shift | Repräsentatives Set erweitern, Distillation oder Fine-Tune |
| Langsame erste Prediction | Cold Start / Model Load | Warmup Frame triggern beim Start |

---

## CI Gate & Model Manifest
Ziel: Automatisierte Qualitätssicherung vor Deployment.

### Komponenten
- `ml/ci_gate.py`: Orchestriert Quantisierung → Vergleich → Manifest → Exit Code.
### Manifest Felder (Beispiel)
```jsonc
{
  "model_version": "20250929_101500_model_int8_full",
  "model_file": "model_int8_full.tflite",
  "input_size": 224,
  "format": "tflite",
  "quantization": "int8_full",
  "classes": ["healthy","nitrogen_deficiency", "overwatering", "pests_risk", "leaf_spot", "ph_imbalance"],
  "normalization": {"mean":[0.485,0.456,0.406],"std":[0.229,0.224,0.225]},
  "macro_f1": 0.908,
  "baseline_fp32_macro_f1": 0.915,
  "delta_macro_f1_pct": -0.7
}
```

### CI Beispielaufruf
```bash
python ml/ci_gate.py \
  --onnx ml/outputs/run_007/model_simplified.onnx \
  --quant-dir ml/outputs/run_007/quant \
  --data-root datasets/plant_v1/images \
  --meta datasets/plant_v1/meta_test.csv \
  --manifest-out app/src/main/assets/leafsense_model.json
Exit Code != 0 → Build fail (Quality unter Schwellwert oder Prozessfehler).

### Schwellenwert Strategie
- Default Macro-F1 & Accuracy Drop ≤ 3.0%. Anpassen bei reifen Datensätzen ggf. strenger (2%).
- Optional zweistufig: Warnung bei >2%, Fail bei >4% (kann ins Skript erweitert werden).

### Integration in Analyzer
`TFLiteLeafSenseAnalyzer` versucht zuerst:
1. Manifest `leafsense_model.json` lesen (model_file, classes, input_size)
2. Fallback Priorität: `model_int8_full.tflite` → `model_int8_dynamic.tflite` → `model_fp32.tflite` → `leafsense_model.tflite`
3. Falls alles fehlt: Dummy Ergebnisse (sichtbarer Fallback).

### Vorteile
- Reproduzierbare Auswahl statt manuelles Kopieren
- Minimiert Risiko schlechterer Modelle in Produktion
- Automatische Dokumentation (Version + Metriken direkt im Asset)

---


## Sicherheit & Governance
- Keine privaten Bilder in Repo
- Anonymisierte Feedback-Daten getrennt speichern

---
Version: 0.1 Scaffold

---

## CI Quality Gate (Automatisierter Modellcheck)
Der Workflow `.github/workflows/model-ci.yml` führt folgende Schritte aus:
1. (Optional) Datensatz laden (Git LFS / Download / Artifact)
2. Quantisierung (falls Artefakte fehlen) via `ml/ci_gate.py`
3. Genauigkeitsvergleich (`compare_tflite_accuracy.py` Strict Mode)
4. Manifest-Generierung (`generate_manifest.py`)
5. Upload von Manifest + Eval Reports als Build Artifacts

### Standard-Trigger
- Push oder Pull Request nach `main` wenn Dateien unter `ml/**` oder `app/src/main/assets/**` geändert wurden.
- Manuelle Auslösung: GitHub UI → Actions → Workflow auswählen → `Run workflow`.

### Wichtige ENV Variablen / Inputs
| Variable | Bedeutung |
|----------|-----------|
| ONNX_MODEL | Pfad zur ONNX Basis (vereinfacht) |
| QUANT_DIR | Zielverzeichnis für TFLite Varianten |
| DATA_ROOT | Bilderwurzel für Eval |
| META_CSV | meta_test.csv oder Val/Test CSV |
| CLASSES | Komma-separierte Klassen in Trainingsreihenfolge |
| MAX_DROP | Max erlaubter % Verlust Macro-F1 & Accuracy |
| IMAGE_SIZE | Evaluationsinputgröße (Standard 224) |

### Manuelle lokale Ausführung (PowerShell)
```powershell
python ml/ci_gate.py `
  --onnx ml/outputs/run_007/model_simplified.onnx `
  --quant-dir ml/outputs/run_007/quant `
  --data-root datasets/plant_v1/images `
  --meta datasets/plant_v1/meta_test.csv `
  --classes healthy,chlorosis,fungus,necrosis `
  --max-drop 3.0 `
  --manifest-out app/src/main/assets/leafsense_model.json
```

### Artefakte
- `ptq_report.json` (Größen & erfolgreiche Konvertierungen)
- `artifacts/quant_eval/*_tflite_eval.json` (Metriken + Deltas)
- `app/src/main/assets/leafsense_model.json` aktualisiertes Manifest (auch im Artifact `leafsense-manifest`)

### Fehlerschwellen
- Exit Code ≠ 0 wenn Macro-F1 oder Accuracy Drop < -MAX_DROP (Strict Mode).
- Modell wird nicht deployed; PR kann blockiert werden.

### Anpassungen / Erweiterungen
- Zusätzliche absolute Mindest-Macro-F1 Schwelle (Erweiterungsidee: neues Flag `--min-macro-f1`).
- Slack / Teams Notification bei Failure (Webhook Secret setzen).
- Nightly Job für Trendanalyse (Historien-CSV anhängen).

### Modell-Integritätsprüfung (SHA256)
Zur Absicherung, dass das Modell zwischen Evaluation und App-Packaging unverändert bleibt, erzeugt `generate_manifest.py` ein Feld `model_sha256`.

Workflow:
1. Generierung: `ml/generate_manifest.py` berechnet SHA256 über die ausgewählte TFLite Datei (`model_file`) und schreibt `model_sha256` ins Manifest.
2. Deployment: `ml/deploy_ml_assets.py` verifiziert beim Kopieren in `app/src/main/assets/` den Hash. Abweichung -> Fehler (Exit Code ≠ 0).
3. Runtime (App): `TFLiteLeafSenseAnalyzer` berechnet beim ersten erfolgreichen Laden des Modells den Hash und vergleicht mit `model_sha256`.
  - Match → `modelIntegrityVerified()==true`
  - Mismatch → Warning Log & `modelIntegrityMismatch()==true` (Modell wird dennoch genutzt, um hartes Crashen zu vermeiden; optionales Feature Flag könnte später Fail erzwingen).

Vorteile:
- Erkennung unerwarteter Rebuilds / Austausch (z.B. manuelles Überschreiben im Asset Ordner).
- Audit-Pfad (Manifest + CI Report zeigt Hash an).

PowerShell Beispiel (lokal Hash prüfen):
```powershell
Get-FileHash app/src/main/assets/model_int8_full.tflite -Algorithm SHA256
```
Vergleich mit `model_sha256` im Manifest.

CI Report (`build_ci_report.py`) zeigt den Hash unter "Model SHA256".

Geplante Erweiterungen:
- CI Gate, das bei fehlendem / leerem Hash failt.
- Signiertes Manifest (HMAC / GPG) für Supply-Chain Hardening.
- Optionales Device Telemetrie Flag bei Mismatch.

#### Separates Hash-Verification Skript
Skript: `ml/verify_model_hash.py`

CI Beispielschritt (PowerShell Runner):
```powershell
python ml/verify_model_hash.py `
  --manifest app/src/main/assets/leafsense_model.json `
  --search-dirs app/src/main/assets ml/outputs/run_007/quant `
  --require
```
Lokaler Check ohne Fail bei fehlendem Hash:
```powershell
python ml/verify_model_hash.py --manifest app/src/main/assets/leafsense_model.json --search-dirs app/src/main/assets
```
Exit Codes:
- 0: Hash passt oder Hash fehlt ohne --require
- 1: Mismatch, Manifest nicht lesbar, Datei fehlt (mit --require) oder Hash erwartet aber fehlt

#### CI Workflow Schritt (Auszug)
Im Workflow `.github/workflows/model-ci.yml` wird nach dem CI Gate automatisch die Integrität geprüft:
```yaml
      - name: Verify Model Hash
        run: |
          if [ -f "${MANIFEST_OUT}" ]; then
            echo "Verifying model hash integrity...";
            python ml/verify_model_hash.py \
              --manifest "${MANIFEST_OUT}" \
              --search-dirs app/src/main/assets "${QUANT_DIR}" \
              --require;
          else
            echo "Manifest not found at ${MANIFEST_OUT}; cannot verify hash"; exit 1;
          fi
        shell: bash
```

### Häufige Fehler & Lösungen
| Problem | Ursache | Lösung |
|---------|---------|-------|
| Missing model_fp32.tflite | Quantisierung nicht erzeugt | ONNX Pfad prüfen / erneute Ausführung |
| Strict Fail wegen >3% Drop | Repräsentatives Set zu klein | `--repr-dir` Umfang erhöhen; Distillation/QAT erwägen |
| Keine Eval Files | META / DATA_ROOT Pfade falsch | Pfade in Workflow env korrigieren |

---

# ML / Training Scaffold (LeafSense)

## Daten-Pipeline & Continuous Learning

### Übersicht Flow
Gerät (App) → (Consent) → Upload Queue (Room) → FastAPI Ingest (/v1/ingest/image) → Rohdaten + meta_log.jsonl → Snapshot Build → Training/CI → Registry Publish → App Fetch (Manifest + Modell) → Neues Modell aktiv.

### 1. Device Upload
- Consent Toggle in Settings (DataStore)
- Prediction Hook speichert Top-1 Bild + Meta (`predictionLabel`, `top1Score`, `confidenceBucket`, `modelVersion`)
- Periodischer `ImageUploadWorker` (6h) + OneTime nach Enqueue

Konfiguration in Analyzer:
```kotlin
analyzer.enableEnqueueUploads = true
analyzer.uploadBaseUrl = "https://api.example.com" // TODO setzen
```

### 2. Ingest Backend (FastAPI)
Pfad: `backend/`
- Endpoint: `POST /v1/ingest/image` (Multipart: image + meta JSON String Feld `meta`)
- API-Key Header: `X-API-Key`
- Speicherung: `data/uploads/` + Append JSONL `data/meta_log.jsonl`

Start (Dev):
```bash
uvicorn backend.main:app --reload
```

### 3. Dataset Snapshot
Script: `ml/build_dataset_snapshot.py`
```bash
python ml/build_dataset_snapshot.py \
  --images datasets/plant_v1/images \
  --meta datasets/plant_v1/meta.csv \
  --out snapshots/plants_v1_20250930 \
  --class-col label --val-ratio 0.15 --test-ratio 0.10 --hash-limit 150
```
Ergebnis: `dataset_manifest.json` + `splits/` CSVs.

### 4. Active Learning Loop
Export unsicherer Beispiele:
```bash
python ml/export_unlabeled.py \
  --meta snapshots/plants_v1_20250930/splits/train.csv \
  --images datasets/plant_v1/images \
  --out al_round1 --strategy entropy --limit 200 --copy-images
```
Annotieren → Import:
```bash
python ml/import_annotations.py \
  --base snapshots/plants_v1_20250930/splits/train.csv \
  --annotations al_round1/annotated.csv \
  --out snapshots/plants_v1_20250930/splits/train_enriched.csv \
  --output-label-field human_label
```
Strategien:
- confidence: niedrigste `top1Score`
- margin: kleinster p1-p2 Abstand (`p1/p2` oder `probs` Spalte)
- entropy: höchste Entropie aus `probs`

### 5. Training & Evaluation
Nutze angereichertes CSV (z.B. human_label bevorzugt, fallback predictionLabel → Mapping Logik in eigenem Preprocessing Script).

### 6. Modell Registry
Publizieren:
```bash
python ml/publish_model.py \
  --model ml/outputs/run_010/model_int8_full.tflite \
  --manifest ml/outputs/run_010/leafsense_model.json \
  --registry registry \
  --version 20250930_run010_int8
```
Abrufen (z.B. für Packaging oder Smoke-Test):
```bash
python ml/fetch_latest_model.py --registry registry --out fetched_model
```
Index Struktur (`registry/index.json`):
```jsonc
{
  "latest": "20250930_run010_int8",
  "versions": [
    {"version": "20250929_run009_int8", "model_sha256": "..."},
    {"version": "20250930_run010_int8", "model_sha256": "...}
  ]
}
```

### 7. App Model Fetch (Future)
- Geplanter Endpunkt: `/v1/model/latest` → liefert manifest + signierte URL
- Fallback: Filesystem Registry Sync per CI → Assets Update
- On-Device: Hash Prüfung vs `model_sha256` (bereits implementiert in Analyzer Integritätsstatus)

### 8. Quality Gates (Recap)
- CI Gate vergleicht Macro-F1 Drop vs Baseline (≤3%)
- Manifest erzwingt Integrität via SHA256
- Optional: Staged Rollout (Manifest Feld `rollout_percent` – TODO)

### 9. Erweiterungsideen
| Bereich | Erweiterung |
|--------|-------------|
| Active Learning | Diversitäts-Clustering (Embeddings), BALD (Bayesian) |
| Ingest | Duplicate Detection (Perceptual Hash), Rate Limiting |
| Registry | Signierter Manifest (HMAC/GPG) |
| Training | Pseudo-Labeling high-confidence Beispiele |
| Telemetrie | Drift Detection (Klassendrift, Confidence Shift) |
