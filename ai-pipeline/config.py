from __future__ import annotations

from dataclasses import dataclass
from typing import Any


# CONFIGURATION & CONSTANTS

YOLO_LABELS: list[str] = [
    "bottled_water", "bread", "clothes", "coffee_cup", "cosmetic", "electronic_item",
    "fastfood", "helmet", "medicine", "milk_tea", "motorbike", "noodle",
    "rice_meal", "shoes", "snack", "soft_drink", "taxi_car", "toy_game"
]

HAS_FEATURES = [f"has_{label}" for label in YOLO_LABELS]
GROUP_FEATURES = ["food_drink_count", "transport_count", "shopping_count", "entertainment_count", "health_count"]
YOLO_STAT_FEATURES = ["total_objects", "max_confidence", "avg_confidence", "low_confidence_count"]
RF_FEATURES = HAS_FEATURES + GROUP_FEATURES + YOLO_STAT_FEATURES
CSV_COLUMNS = RF_FEATURES + ["category_id"]

CATEGORY_ID_TO_NAME = {1: "An uong", 2: "Di chuyen", 3: "Mua sam", 5: "Giai tri", 6: "Suc khoe", 7: "Khac"}
VALID_CATEGORY_IDS = tuple(CATEGORY_ID_TO_NAME)

LABEL_TO_GROUP_FEATURE = {
    "bottled_water": "food_drink_count", "bread": "food_drink_count", "coffee_cup": "food_drink_count",
    "fastfood": "food_drink_count", "milk_tea": "food_drink_count", "noodle": "food_drink_count",
    "rice_meal": "food_drink_count", "snack": "food_drink_count", "soft_drink": "food_drink_count",
    "helmet": "transport_count", "motorbike": "transport_count", "taxi_car": "transport_count",
    "clothes": "shopping_count", "cosmetic": "shopping_count", "electronic_item": "shopping_count",
    "shoes": "shopping_count", "toy_game": "entertainment_count", "medicine": "health_count"
}

CONFIDENCE_THRESHOLD = 0.25
LOW_CONFIDENCE_THRESHOLD = 0.5


def compute_features(
    detections: list[dict[str, any]],
    low_conf_thresh: float = LOW_CONFIDENCE_THRESHOLD,
) -> dict[str, float | int]:
    features: dict[str, float | int] = {name: 0 for name in RF_FEATURES}
    confidences = []
    
    for item in detections:
        name = str(item.get("class_name", ""))
        conf = float(item.get("confidence", 0.0))

        if f"has_{name}" in features:
            features[f"has_{name}"] = 1
        group = LABEL_TO_GROUP_FEATURE.get(name)
        if group:
            features[group] = int(features[group]) + 1
        confidences.append(conf)

    n = len(confidences)
    features.update({
        "total_objects": n,
        "max_confidence": round(max(confidences), 6) if confidences else 0.0,
        "avg_confidence": round(sum(confidences) / n, 6) if n > 0 else 0.0,
        "low_confidence_count": sum(1 for c in confidences if c < low_conf_thresh)
    })
    return features

