from __future__ import annotations

from pathlib import Path
import joblib
import matplotlib.pyplot as plt
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import (
    ConfusionMatrixDisplay,
    classification_report,
    confusion_matrix,
)
from sklearn.model_selection import StratifiedKFold, cross_val_score, train_test_split

from config import RF_FEATURES
from train_rf import load_data


def cross_validate(
    model: RandomForestClassifier,
    features: pd.DataFrame,
    labels: pd.Series,
) -> None:
    # Evaluate model using 5-fold Stratified Cross Validation.

    # Run BEFORE training to estimate generalizability
    # of the model without leakage from the final test set.

    print("\n" + "=" * 60)
    print("CROSS VALIDATION")
    print("=" * 60)

    # StratifiedKFold maintains class ratio in each fold
    stratified_kfold = StratifiedKFold(
        n_splits=5,
        shuffle=True,
        random_state=42,
    )

    metrics = {
        "accuracy": "Accuracy",
        "f1_macro": "F1 Macro",  # F1 Macro is suitable for imbalanced classes
    }

    for scoring_name, display_name in metrics.items():
        scores = cross_val_score(
            model,
            features,
            labels,
            cv=stratified_kfold,
            scoring=scoring_name,
            n_jobs=-1,
        )

        print(
            f"{display_name:<15}: "
            f"{scores.mean():.4f} ± {scores.std():.4f}"
        )


def print_feature_importance(
    model: RandomForestClassifier,
) -> pd.Series:
    # Display and return feature importance ranking.

    # Based on the mean decrease in impurity (Gini) of the entire forest.
    # Features with importance < 0.01 can usually be removed.

    importance_scores = pd.Series(
        model.feature_importances_,
        index=RF_FEATURES,
    )

    importance_scores = importance_scores.sort_values(ascending=False)

    print("\n" + "=" * 60)
    print("FEATURE IMPORTANCE")
    print("=" * 60)

    for feature_name, score in importance_scores.items():
        print(f"{feature_name:<30} {score:.4f}")

    return importance_scores


def save_confusion_matrix(
    true_labels,
    predicted_labels,
    class_names,
    output_dir: Path,
) -> None:
    # Save confusion matrix as PNG image.

    matrix = confusion_matrix(true_labels, predicted_labels)

    display = ConfusionMatrixDisplay(
        matrix,
        display_labels=class_names,
    )

    figure, axes = plt.subplots(figsize=(7, 6))

    display.plot(
        ax=axes,
        cmap="Blues",
        colorbar=False,
    )

    axes.set_title("Confusion Matrix")

    output_file = output_dir / "confusion_matrix.png"

    plt.tight_layout()
    plt.savefig(output_file, dpi=150)
    plt.close()

    print(f"Saved: {output_file}")


def save_feature_importance_chart(
    importance_scores: pd.Series,
    output_dir: Path,
) -> None:
    # Save horizontal bar chart of feature importances as PNG.

    figure, axes = plt.subplots(
        figsize=(8, max(4, len(RF_FEATURES) * 0.4))
    )

    # Sort ascending so the longest bar is at the top
    importance_scores.sort_values().plot(
        kind="barh",
        ax=axes,
    )

    axes.set_title("Feature Importance")
    axes.set_xlabel("Importance Score")

    output_file = output_dir / "feature_importance.png"

    plt.tight_layout()
    plt.savefig(output_file, dpi=150)
    plt.close()

    print(f"Saved: {output_file}")


def evaluate_saved_model(
    model_path: str,
    dataset_path: str,
    output_dir: Path,
) -> None:
    # Load model and data, split, and run full evaluation.

    # 1. Load model purely from file
    print(f"Loading model from file: {model_path}...")
    if not Path(model_path).exists():
        print(f"[ERROR] Model file not found: {model_path}. Run train_rf.py first.")
        return
        
    model = joblib.load(model_path)

    # 2. Load data
    features, labels = load_data(dataset_path)

    # 3. Split data to get test set
    features_train, features_test, labels_train, labels_test = train_test_split(
        features,
        labels,
        test_size=0.2,
        stratify=labels,
        random_state=42,
    )

    # 4. Cross-validate (using full dataset)
    cross_validate(model, features, labels)

    # 5. Evaluate on train/test sets
    train_accuracy = model.score(features_train, labels_train)
    test_accuracy = model.score(features_test, labels_test)

    print("\n" + "=" * 60)
    print("MODEL PERFORMANCE")
    print("=" * 60)

    print(f"Training Accuracy : {train_accuracy:.4f}")
    print(f"Testing Accuracy  : {test_accuracy:.4f}")

    # A difference of > 10% between train and test is a sign of overfitting
    if (train_accuracy - test_accuracy) > 0.10:
        print("Warning: Possible overfitting detected.")
        print("Consider reducing max_depth or increasing min_samples_leaf.")

    labels_predicted = model.predict(features_test)

    print("\nClassification Report")
    print(
        classification_report(
            labels_test,
            labels_predicted,
            zero_division=0,
        )
    )

    # predict_proba returns class probabilities; taking the max yields confidence
    probabilities = model.predict_proba(features_test)
    confidence_scores = probabilities.max(axis=1)

    print("\nConfidence Analysis")
    print(f"Average Confidence : {confidence_scores.mean():.2%}")

    # 6. Save visualizations and print importance ranking
    importance_scores = print_feature_importance(model)

    save_confusion_matrix(
        labels_test,
        labels_predicted,
        model.classes_,
        output_dir,
    )

    save_feature_importance_chart(
        importance_scores,
        output_dir,
    )


if __name__ == "__main__":
    evaluate_saved_model(
        model_path="outputs/random_forest_model.joblib",
        dataset_path="outputs/dataset.csv",
        output_dir=Path("outputs"),
    )
