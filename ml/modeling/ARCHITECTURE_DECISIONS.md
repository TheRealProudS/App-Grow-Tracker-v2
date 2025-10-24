# Architecture Decisions – LeafSense Model v1

## AD-001: Backbone Choice
Decision: MobileNetV3-Large 224 as primary backbone for v1.
Rationale: Balance of accuracy vs. on-device latency & size. Pretrained weights widely available, TFLite-friendly.
Alternatives Considered: EfficientNet-Lite0 (secondary candidate if accuracy shortfall), MobileNetV3-Small (too low capacity), Lite2 (higher latency), ConvNeXt / MobileViT (complex export, heavier).
Status: Accepted

## AD-002: Input Resolution
Decision: 224x224 RGB (center crop / random resized crop in train).
Rationale: Common pretrained checkpoint resolution, keeps latency moderate.
Status: Accepted

## AD-003: Fine-Tuning Strategy
Phase 1: Train new head (frozen backbone) 3–5 epochs (LR 3e-3)  
Phase 2: Unfreeze top ~60% (LR 8e-4 cosine decay)  
Phase 3: Optionally unfreeze full backbone (LR 2e-4)  
Status: Accepted

## AD-004: Loss & Class Imbalance
Decision: Weighted CrossEntropy (weights = 1/log(1.2+freq)), optional switch to Focal Loss (γ=1.5) if minority recall < target.
Status: Accepted

## AD-005: Regularization
Label smoothing 0.05, Dropout 0.15 in head, mild color & geometric augmentation, optional Mixup (α=0.2) if overfit emerges.
Status: Accepted

## AD-006: Quantization Path
PTQ INT8 with representative 200–300 images (≥8 per class). Accept accuracy drop ≤3% Macro F1; fallback FP16.
Status: Accepted

## AD-007: Evaluation Metrics
Primary: Macro F1. Secondary: Per-class Precision/Recall, Confusion Matrix, On-device Latency (median over 50 runs). Target Macro F1 ≥0.78.
Status: Accepted

## AD-008: Benchmark Strategy
If Macro F1 <0.78 after tuning: train EfficientNet-Lite0 replicate pipeline; choose better validated model.
Status: Accepted

---
Future ADs: Edge pruning (AD-009), Distillation (AD-010) if needed.
