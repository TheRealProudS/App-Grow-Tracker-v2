# Dataset Changelog

All notable changes to the LeafSense dataset will be documented here.

## [v1.0.0] - Unreleased
### Added
- Initial class taxonomy established (16 primary classes)
- Repository structure scaffolded (`raw/`, `metadata/`, `qa/`)
- Placeholder metadata files planned (`labels_v1.csv`, `split_v1.csv`, `stats_v1.json`)

### Pending
- Populate raw images per class
- Generate first pass labels_v1.csv
- Compute stats (class counts, mean/std)
- Create QA reports (duplicates, blur flags)

---
Versioning follows SemVer adapted for datasets:
- MAJOR: Breaking changes (class additions/removals, schema changes)
- MINOR: Significant new data (>10% size increase) or large relabel set
- PATCH: Minor corrections (<1% relabel) or metadata clarifications
