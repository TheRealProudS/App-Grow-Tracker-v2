"""Export skeleton: PyTorch -> ONNX -> TFLite (via intermediate TF) placeholder.
Fill real conversion steps once training artifacts exist.
"""
from __future__ import annotations
import argparse
from pathlib import Path
import torch
from torchvision import models
import subprocess
import json


def build_model(num_classes: int):
    m = models.mobilenet_v3_large(weights=None)
    in_features = m.classifier[0].in_features
    m.classifier = torch.nn.Sequential(
        torch.nn.Linear(in_features, 1024),
        torch.nn.Hardswish(),
        torch.nn.Dropout(p=0.15),
        torch.nn.Linear(1024, num_classes)
    )
    return m


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--checkpoint', type=Path, required=True)
    parser.add_argument('--classes-json', type=Path, required=True)
    parser.add_argument('--out-dir', type=Path, required=True)
    parser.add_argument('--int8', action='store_true')
    args = parser.parse_args()

    args.out_dir.mkdir(parents=True, exist_ok=True)
    classes = json.loads(args.classes_json.read_text())
    model = build_model(len(classes))
    sd = torch.load(args.checkpoint, map_location='cpu')
    model.load_state_dict(sd)
    model.eval()

    dummy = torch.randn(1, 3, 224, 224)
    onnx_path = args.out_dir / 'model.onnx'
    torch.onnx.export(model, dummy, onnx_path, input_names=['input'], output_names=['logits'], opset_version=13)

    # Placeholder: call external conversion chain (requires installed tooling)
    print(f"Exported ONNX placeholder at {onnx_path} (TFLite conversion not executed in skeleton).")
    # Real flow would:
    # 1. onnx -> tf (onnx-tf)
    # 2. tf -> tflite (converter) with optional rep dataset for int8


if __name__ == '__main__':
    main()
