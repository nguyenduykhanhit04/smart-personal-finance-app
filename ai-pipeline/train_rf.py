from __future__ import annotations

from pathlib import Path

import joblib
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split

from config import RF_FEATURES


# DATA LOADING

def load_data(csv_path: str) -> tuple[pd.DataFrame, pd.Series]:

    # Read the first line to detect the separator (; or ,)
    with open(csv_path, "r", encoding="utf-8") as file:
        first_line = file.readline()

    separator = ";" if ";" in first_line else ","

    dataframe = pd.read_csv(csv_path, sep=separator)

    print(f"Dataset loaded: {csv_path}")
    print(f"Number of samples: {len(dataframe)}")

    # Remove rows without category_id label
    dataframe = dataframe.dropna(subset=["category_id"])
    dataframe = dataframe[
        dataframe["category_id"].astype(str).str.strip() != ""
    ]

    dataframe["category_id"] = dataframe["category_id"].astype(int)

    features = dataframe[RF_FEATURES].fillna(0)  # Fill missing values with 0
    labels = dataframe["category_id"]

    print("\nClass Distribution")
    print(labels.value_counts().sort_index())

    return features, labels


# MODEL CREATION


def build_model() -> RandomForestClassifier:
    
    # Create Random Forest model with fixed hyperparameters.

    # - n_estimators=200   : Number of trees in the forest — more trees are more stable
    # - max_depth=20       : Limit maximum depth to avoid overfitting
    # - min_samples_leaf=3 : Each leaf must have at least 3 samples
    # - class_weight=balanced: Automatically adjust weights for imbalanced classes
    

    return RandomForestClassifier(
        n_estimators=200,
        max_depth=20,
        min_samples_leaf=3,
        max_features="sqrt",
        class_weight="balanced",
        random_state=42,
        n_jobs=-1,
    )


# SAVE MODEL

def save_model(
    model: RandomForestClassifier,
    model_path: str,
) -> None:
    # Serialize and save trained model to disk using joblib.

    Path(model_path).parent.mkdir(
        parents=True,
        exist_ok=True,
    )

    joblib.dump(model, model_path)

    print(f"\nModel saved: {model_path}")



# MAIN PIPELINE

def train_rf(
    dataset_path: str,
    model_path: str,
) -> None:

    # Full training pipeline:
    #     1. Load data
    #     2. Split data
    #     3. Build & Train model
    #     4. Save model
  

    features, labels = load_data(dataset_path)

    # Use stratify=labels to keep the class ratio consistent in both train and test sets.
    features_train, _, labels_train, _ = train_test_split(
        features,
        labels,
        test_size=0.2,
        stratify=labels,
        random_state=42,
    )

    # Create and train model
    model = build_model()
    model.fit(features_train, labels_train)

    # Save model
    save_model(model, model_path)


if __name__ == "__main__":
    train_rf(
        dataset_path="outputs/dataset.csv",
        model_path="outputs/random_forest_model.joblib",
    )
