package com.example.personalfinance.ml;

import android.util.Log;

import com.example.personalfinance.yolo.YoloDetector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductRandomForestClassifier {

    private static final String TAG = "RF_PRODUCT";
    private static final double LOW_CONFIDENCE_THRESHOLD = 0.5;

    private static final String[] CLASS_NAMES = {
            "bottled_water", "bread", "clothes", "coffee_cup", "cosmetic",
            "electronic_item", "fastfood", "helmet", "medicine", "milk_tea",
            "motorbike", "noodle", "rice_meal", "shoes", "snack",
            "soft_drink", "taxi_car", "toy_game"
    };

    private static final Map<String, Integer> CLASS_INDEX = buildClassIndex();

    public Prediction classify(List<YoloDetector.YoloDetection> detections) {
        double[] features = extractFeatures(detections);
        double[] scores = ProductRandomForestModel.score(features);
        if (scores == null || scores.length == 0) {
            Log.w(TAG, "Prediction failed: model returned no scores");
            return null;
        }

        int bestIndex = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[bestIndex]) {
                bestIndex = i;
            }
        }

        if (bestIndex >= ProductRandomForestModel.CATEGORY_IDS.length) {
            Log.w(TAG, "Prediction failed: score index has no matching categoryId");
            return null;
        }

        int categoryId = ProductRandomForestModel.CATEGORY_IDS[bestIndex];
        Log.d(TAG, "features=" + Arrays.toString(features));
        Log.d(TAG, "scores=" + Arrays.toString(scores)
                + ", categoryId=" + categoryId
                + ", confidence=" + scores[bestIndex]);
        return new Prediction(categoryId, scores[bestIndex]);
    }

    private double[] extractFeatures(List<YoloDetector.YoloDetection> detections) {
        double[] features = new double[ProductRandomForestModel.FEATURE_NAMES.length];
        if (detections == null || detections.isEmpty()) {
            return features;
        }

        double confidenceSum = 0.0;
        int total = 0;
        int lowConfidenceCount = 0;

        for (YoloDetector.YoloDetection detection : detections) {
            if (detection == null) {
                continue;
            }

            String className = detection.className != null ? detection.className : "";
            double confidence = detection.confidence;
            total++;
            confidenceSum += confidence;
            features[24] = Math.max(features[24], confidence);
            if (confidence < LOW_CONFIDENCE_THRESHOLD) {
                lowConfidenceCount++;
            }

            Integer classIndex = CLASS_INDEX.get(className);
            if (classIndex != null) {
                features[classIndex] = 1.0;
                incrementGroupCount(features, className);
            }
        }

        features[23] = total;
        features[25] = total > 0 ? confidenceSum / total : 0.0;
        features[26] = lowConfidenceCount;
        return features;
    }

    private void incrementGroupCount(double[] features, String className) {
        switch (className) {
            case "bottled_water":
            case "bread":
            case "coffee_cup":
            case "fastfood":
            case "milk_tea":
            case "noodle":
            case "rice_meal":
            case "snack":
            case "soft_drink":
                features[18]++;
                break;
            case "motorbike":
            case "taxi_car":
            case "helmet":
                features[19]++;
                break;
            case "clothes":
            case "cosmetic":
            case "electronic_item":
            case "shoes":
                features[20]++;
                break;
            case "toy_game":
                features[21]++;
                break;
            case "medicine":
                features[22]++;
                break;
            default:
                break;
        }
    }

    private static Map<String, Integer> buildClassIndex() {
        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < CLASS_NAMES.length; i++) {
            result.put(CLASS_NAMES[i], i);
        }
        return result;
    }

    public static class Prediction {
        private final int categoryId;
        private final double confidence;

        public Prediction(int categoryId, double confidence) {
            this.categoryId = categoryId;
            this.confidence = confidence;
        }

        public int getCategoryId() {
            return categoryId;
        }

        public double getConfidence() {
            return confidence;
        }
    }
}
