package com.example.personalfinance.ml

import android.util.Log
import com.example.personalfinance.ml.yolo.YoloDetector
import java.util.Arrays

class ProductRandomForestClassifier {

    companion object {
        private const val TAG = "RF_PRODUCT"
        private const val LOW_CONFIDENCE_THRESHOLD = 0.5

        private val CLASS_NAMES = arrayOf(
            "bottled_water", "bread", "clothes", "coffee_cup", "cosmetic",
            "electronic_item", "fastfood", "helmet", "medicine", "milk_tea",
            "motorbike", "noodle", "rice_meal", "shoes", "snack",
            "soft_drink", "taxi_car", "toy_game"
        )

        private val CLASS_INDEX: Map<String, Int> = buildClassIndex()

        private fun buildClassIndex(): Map<String, Int> {
            return CLASS_NAMES.indices.associateBy { CLASS_NAMES[it] }
        }
    }

    data class Prediction(val categoryId: Int, val confidence: Double)

    fun classify(detections: List<YoloDetector.YoloDetection>?): Prediction? {
        val features = extractFeatures(detections)
        val scores = ProductRandomForestModel.score(features)
        if (scores.isEmpty()) {
            Log.w(TAG, "Prediction failed: model returned no scores")
            return null
        }

        var bestIndex = 0
        for (i in 1 until scores.size) {
            if (scores[i] > scores[bestIndex]) {
                bestIndex = i
            }
        }

        if (bestIndex >= ProductRandomForestModel.CATEGORY_IDS.size) {
            Log.w(TAG, "Prediction failed: score index has no matching categoryId")
            return null
        }

        val categoryId = ProductRandomForestModel.CATEGORY_IDS[bestIndex]
        Log.d(TAG, "features=${Arrays.toString(features)}")
        Log.d(TAG, "scores=${Arrays.toString(scores)}, categoryId=$categoryId, confidence=${scores[bestIndex]}")
        return Prediction(categoryId, scores[bestIndex])
    }

    private fun extractFeatures(detections: List<YoloDetector.YoloDetection>?): DoubleArray {
        val features = DoubleArray(ProductRandomForestModel.FEATURE_NAMES.size)
        if (detections.isNullOrEmpty()) {
            return features
        }

        var confidenceSum = 0.0
        var total = 0
        var lowConfidenceCount = 0

        for (detection in detections) {
            val className = detection.className
            val confidence = detection.confidence.toDouble()
            total++
            confidenceSum += confidence
            features[24] = maxOf(features[24], confidence)
            if (confidence < LOW_CONFIDENCE_THRESHOLD) {
                lowConfidenceCount++
            }

            val classIndex = CLASS_INDEX[className]
            if (classIndex != null) {
                features[classIndex] = 1.0
                incrementGroupCount(features, className)
            }
        }

        features[23] = total.toDouble()
        features[25] = if (total > 0) confidenceSum / total else 0.0
        features[26] = lowConfidenceCount.toDouble()
        return features
    }

    private fun incrementGroupCount(features: DoubleArray, className: String) {
        when (className) {
            "bottled_water", "bread", "coffee_cup", "fastfood", "milk_tea", "noodle", "rice_meal", "snack", "soft_drink" ->
                features[18]++
            "motorbike", "taxi_car", "helmet" ->
                features[19]++
            "clothes", "cosmetic", "electronic_item", "shoes" ->
                features[20]++
            "toy_game" ->
                features[21]++
            "medicine" ->
                features[22]++
        }
    }
}
