package com.example.financebackend.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.Result;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProductClassifierService {

    private static final Logger logger = LoggerFactory.getLogger(ProductClassifierService.class);
    private static final String MODEL_RESOURCE_NAME = "random_forest_model.onnx";
    private static final double LOW_CONFIDENCE_THRESHOLD = 0.5;

    // Valid DB category IDs that the ONNX model was trained with
    // 1=Ăn uống, 2=Di chuyển, 3=Mua sắm, 5=Giải trí, 6=Sức khỏe, 7=Khác
    private static final Set<Integer> VALID_CATEGORY_IDS = Set.of(1, 2, 3, 5, 6, 7);

    private static final String[] CLASS_NAMES = {
            "bottled_water", "bread", "clothes", "coffee_cup", "cosmetic",
            "electronic_item", "fastfood", "helmet", "medicine", "milk_tea",
            "motorbike", "noodle", "rice_meal", "shoes", "snack",
            "soft_drink", "taxi_car", "toy_game"
    };

    private static final Map<String, Integer> CLASS_INDEX = buildClassIndex();

    private OrtEnvironment env;
    private OrtSession session;

    @Data
    public static class YoloDetection {
        private String className;
        private double confidence;
    }

    @PostConstruct
    public void init() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(MODEL_RESOURCE_NAME)) {
            if (is != null) {
                byte[] modelBytes = is.readAllBytes();
                env = OrtEnvironment.getEnvironment();
                session = env.createSession(modelBytes, new OrtSession.SessionOptions());
                logger.info("ONNX Product RandomForest model loaded successfully from resources: {}", MODEL_RESOURCE_NAME);
            } else {
                logger.warn("ONNX model file not found in resources: {}. Product classification will use rule-based fallback.", MODEL_RESOURCE_NAME);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize ONNX runtime / load model from resources", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) {
                session.close();
            }
            if (env != null) {
                env.close();
            }
        } catch (Exception e) {
            logger.error("Error closing ONNX runtime resources", e);
        }
    }

    public int classify(List<YoloDetection> detections) {
        if (session == null) {
            logger.info("No ONNX session active, falling back to rule-based classification.");
            return fallbackClassify(detections);
        }

        try {
            double[] features = extractFeatures(detections);
            float[][] inputData = new float[1][27];
            for (int i = 0; i < 27; i++) {
                inputData[0][i] = (float) features[i];
            }

            String inputName = session.getInputNames().iterator().next();

            try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputData);
                 Result results = session.run(Map.of(inputName, inputTensor))) {

                var labelOutput = results.get(0);
                Object value = labelOutput.getValue();
                int categoryId = 7; // Default to 'other' (Khác)

                // ONNX model was trained with DB category IDs directly (1,2,3,5,6,7)
                // so output is already a valid DB category ID
                if (value instanceof long[]) {
                    categoryId = (int) ((long[]) value)[0];
                } else if (value instanceof int[]) {
                    categoryId = ((int[]) value)[0];
                } else if (value instanceof String[]) {
                    String strLabel = ((String[]) value)[0];
                    try {
                        categoryId = Integer.parseInt(strLabel);
                    } catch (NumberFormatException e) {
                        int index = mapCategoryNameToIndex(strLabel);
                        categoryId = mapIndexToCategoryId(index);
                    }
                } else {
                    logger.warn("Unexpected label output type: {}", value.getClass().getName());
                }

                // Validate that categoryId is a valid DB category ID
                if (!VALID_CATEGORY_IDS.contains(categoryId)) {
                    logger.warn("ONNX model returned unexpected category ID: {}. Falling back to 7 (Khác)", categoryId);
                    categoryId = 7;
                }

                logger.info("Classified product detections using ONNX RandomForest. Category ID: {}", categoryId);
                return categoryId;
            }
        } catch (Exception e) {
            logger.error("Error running ONNX model prediction, falling back to rule-based", e);
            return fallbackClassify(detections);
        }
    }

    private double[] extractFeatures(List<YoloDetection> detections) {
        double[] features = new double[27];
        if (detections == null || detections.isEmpty()) {
            return features;
        }

        double confidenceSum = 0.0;
        int total = 0;
        int lowConfidenceCount = 0;

        for (YoloDetection detection : detections) {
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

    private int fallbackClassify(List<YoloDetection> detections) {
        double[] features = extractFeatures(detections);
        double foodScore = features[18];
        double transportScore = features[19];
        double shoppingScore = features[20];
        double entertainmentScore = features[21];
        double healthScore = features[22];

        double maxScore = 0;
        int bestIndex = 5; // Default to 'other'

        if (foodScore > maxScore) {
            maxScore = foodScore;
            bestIndex = 0;
        }
        if (transportScore > maxScore) {
            maxScore = transportScore;
            bestIndex = 1;
        }
        if (shoppingScore > maxScore) {
            maxScore = shoppingScore;
            bestIndex = 2;
        }
        if (entertainmentScore > maxScore) {
            maxScore = entertainmentScore;
            bestIndex = 3;
        }
        if (healthScore > maxScore) {
            bestIndex = 4;
        }

        return mapIndexToCategoryId(bestIndex);
    }

    private int mapCategoryNameToIndex(String name) {
        if (name == null) return 5;
        switch (name.toLowerCase()) {
            case "food": return 0;
            case "transport": return 1;
            case "shopping": return 2;
            case "entertainment": return 3;
            case "health": return 4;
            default: return 5;
        }
    }

    private int mapIndexToCategoryId(int index) {
        switch (index) {
            case 0: return 1; // Ăn uống
            case 1: return 2; // Di chuyển
            case 2: return 3; // Mua sắm
            case 3: return 5; // Giải trí
            case 4: return 6; // Sức khỏe
            default: return 7; // Khác
        }
    }

    private static Map<String, Integer> buildClassIndex() {
        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < CLASS_NAMES.length; i++) {
            result.put(CLASS_NAMES[i], i);
        }
        return result;
    }
}
