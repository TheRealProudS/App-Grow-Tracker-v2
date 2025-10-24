# LeafSense Dataset (v1 Draft)

This directory hosts dataset assets and metadata for the on‑device leaf condition classifier (LeafSense v1).

## Goals
- Robust classification of leaf state into health / deficiency / stress / pathogen-indicator groups.
- Balanced, clean, deduplicated dataset for mobile ML training.

## Directory Layout
```
ml/dataset/
  raw/                # Original collected images (per class subfolders) – DO NOT MODIFY IN-PLACE
  processed/          # Preprocessed / resized / augmented previews (gitignore large binaries later)
  metadata/           # CSV / JSON label maps, splits, stats
  qa/                 # Automated quality assurance reports (duplicates, blur flags, etc.)
  CHANGELOG.md        # Versioning of dataset states
  README.md           # This file
```

## Class Set (v1)
```
HEALTHY
NUTRIENT_DEF_N
NUTRIENT_DEF_P
NUTRIENT_DEF_K
NUTRIENT_DEF_MG
NUTRIENT_DEF_FE
OVERWATER_STRESS
UNDERWATER_STRESS
HEAT_STRESS
COLD_STRESS
LIGHT_BURN
FUNGAL_SPOTS_GENERIC
MILDEW_LIKE
LEAF_PEST_INDICATOR
NECROSIS_EDGE
GENERAL_CHLOROSIS
```
(Reserve / future: CALCIUM_DEF, SULFUR_DEF, ROOT_ISSUE_SIGNAL)

## Metadata Files
- `labels_v1.csv` – file_name,class_label,source,lighting,leaf_position,notes
- `split_v1.csv` – file_name,split (train|val|test)
- `stats_v1.json` – aggregate class counts, mean/std, imbalance metrics

## Guidelines (Short)
- Single dominant leaf, planar, minimal motion blur.
- Neutral or low-noise background preferred.
- Disable aggressive HDR / beauty filters.
- Avoid heavy compression: JPEG quality ≥ 85.

## Quality Assurance
- Perceptual hash duplicate scan (Hamming distance <4 flagged)
- Blur detection (Laplacian variance threshold)
- Exposure check (histogram clipping)

## Versioning
All dataset changes require:
1. Update `CHANGELOG.md`
2. Increment dataset version (v1.x.y) if labels added/removed or >1% relabel.

## License & Sources
Only include images you own or that are licensed for this purpose (CC0 / CC-BY with attribution tracked). Document external sources in `metadata/license_sources.md`.

## Next Steps
- Populate `raw/` with initial samples
- Fill `labels_v1.csv`
- Generate `split_v1.csv` after class balance check
- Compute `stats_v1.json`

---
*This is an initial scaffold. Expand with annotation guidelines once finalized.*
