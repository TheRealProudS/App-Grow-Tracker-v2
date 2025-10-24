# Benchmarks (Placeholder)

Will contain empirical comparisons once models are trained.

## Planned Sections
- Hardware & Environment Description
- Training Summary Table
- Validation Metrics (Macro F1, per-class PR)
- On-Device Latency (CPU / NNAPI) – median & p95
- Model Size (unquantized / FP16 / INT8)
- Quantization Accuracy Impact

## Initial Targets
| Metric | Target |
|--------|--------|
| Macro F1 | ≥ 0.78 |
| Minority Class Recall (worst 5 classes) | ≥ 0.65 |
| On-Device Latency (INT8) | < 25 ms |
| Model Size INT8 | < 8 MB |
| Accuracy Drop Post-Quant | ≤ 3% Macro F1 |

---
Populate after first training cycle.
