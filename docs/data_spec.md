# LeafSense Daten-Spezifikation (v0.1)

Ziel: Fundierte, reproduzierbare Datengrundlage für ein On-Device Modell zur Erkennung von Pflanzengesundheit & Stress / Nährstoffmängeln.

## 1. Label-Taxonomie (Iteration 1)
Single-Label (Übergang zu Multi-Label in späterer Version). Klassen:
- healthy: Blatt ohne erkennbare Symptome
- nitrogen_deficiency: Chlorose älterer Blätter, gesamtes Blatt hellgelb
- calcium_deficiency: Junge Blätter deformiert / Nekrosen an Spitzen
- overwatering: Blätter schlaff, teils dunkelgrün, Substrat sichtbar nass
- underwatering: Welke, eingerollte oder hängende Blätter, trockene Erde
- heat_stress: Blattspitzen verbrannt / hellbraun, oft lokalisierte Flecken
- pest_suspect: Sichtbare Fraßspuren / punktuelle helle Punkte / Insekt sichtbar
- fungal_suspect: Puderige, fleckige oder schimmelartige Beläge
- nutrient_other: Unspezifischer Nährstoffmangel (unsicher)
- unknown: Nicht eindeutig zuordenbar / schlechte Qualität

Versionierbar via `labels_version` (Start: `labels_v1`).

## 2. Ziel-Bildanzahl (Minimum Phase 1)
| Klasse | Minimum | Ziel | Anmerkung |
| ------ | ------- | ---- | --------- |
| healthy | 1000 | 2000 | Viele Varianten nötig |
| nitrogen_deficiency | 600 | 1200 | Häufiger Mangel |
| calcium_deficiency | 400 | 900 | Schwieriger zu labeln |
| overwatering | 500 | 1000 | Kontext (Erde) hilfreich |
| underwatering | 500 | 1000 | Blattstruktur + Erde |
| heat_stress | 400 | 900 | Verwechslungsgefahr mit Sonnenbrand |
| pest_suspect | 600 | 1300 | Enthält mehrere Schädlingsformen |
| fungal_suspect | 400 | 900 | Weißer / grauer Belag |
| nutrient_other | 300 | 600 | Sammelklasse |
| unknown | 300 | 600 | Niedrig halten |
Total Minimum: ~5.0k, Ziel: ~11.4k

## 3. Aufnahmeprotokoll
Parameter:
- Gerät: Smartphone (versch. Modelle), Kamera rückseitig bevorzugt
- Distanz: 15–45 cm (sodass Blattstruktur erkennbar)
- Winkel: 0°, 15°, 30°, 45° variieren
- Licht: Natürlich (diffus) bevorzugt; auch Kunstlicht erfassen für Robustheit
- Fokus: Manuell (Tap-to-Focus) auf symptomtragende Zone
- Hintergrund: Möglichst ruhig, aber Variation akzeptiert
- Mehrere Aufnahmen pro Pflanze (max 3 mit sichtbarer Variation)
- Ausschluss: Bewegungsunschärfe, stark über-/unterbelichtet

## 4. Datei- und Metadatenstruktur
Pfadkonzept:
```
datasets/plant_v1/
  images/
    healthy/IMG_1234.jpg
    nitrogen_deficiency/ND_0001.jpg
  meta.csv
```
`meta.csv` Felder:
- filename
- label (String)
- capture_device (optional)
- capture_iso / exposure_time (optional, falls vorh.)
- captured_at (UTC ISO8601)
- latitude/longitude (falls Opt-in anonymisiert auf grobe Region)
- reviewer_id
- quality_flag (ok, blurry, lowlight)
- notes

## 5. Quality Control
- 10% Stichprobe: Zweitlabel → Cohen Kappa Ziel > 0.78
- Unstimmige Fälle in Review-Pool
- Unknown Anteil < 8% Ziel
- Monatlicher Drift-Check (Labelverteilung)

## 6. Augmentierungs-Profil (Baseline)
Training augment (probabilities):
- RandomResizedCrop(224) 1.0
- HorizontalFlip 0.5 (nicht bei asymmetrischen Schadsignaturen → später filtern)
- ColorJitter (B±0.2, C±0.15, S±0.15, H±0.05) 0.8
- GaussianBlur (ksize 3) 0.15
- RandomBrightnessContrast (±0.15) 0.5
- (Später) MixUp / CutMix optional

## 7. Splitting-Strategie
Stratifiziert nach Label. Falls mehrere Bilder selbe Pflanze: Gruppierung per `plant_id` → alle zu gleicher Split-Gruppe (Leak Prevention).
Train 70%, Val 15%, Test 15%.
Snapshot `split_version = split_v1`.

## 8. Modell-Evaluationsmetriken
- Macro F1
- Per-Class Recall (kritisch für seltene Klassen)
- Confusion Matrix (Top 3 Verwechslungen tracken)
- Inference Zeit (ms) auf Referenzgerät
- Modellgröße (MB)

## 9. Versionierung & Repro
Konventionen:
- `model_name`: mobilenet_v3_small
- `exp_id`: `mnet_v3s_augA_lr8e-4_v1`
- `data_version`: `plant_v1`
- `labels_version`: `labels_v1`
- `split_version`: `split_v1`
- `augment_profile`: `augA`

## 10. Datenschutz & Governance (Kurz)
- Nutzerbilder lokal verarbeitet
- Upload für Verbesserung nur explizit opt-in
- Anonymisierung: Entferne EXIF GPS oder runde auf 1°
- Löschrecht: In-App “Meine Diagnosebilder löschen”

## 11. Nächste Iterationen
- Multi-Label Übergang (kombinierte Symptome)
- Leaf Area Segmentation optional für Kontext
- Active Learning Queue (Unsichere Fälle: Softmax Entropy > τ)

---
Letzte Änderung: <DATUM_AUTOMATISCH_ANPASSEN>
Autor: KI-Assistent Vorschlag
