package com.example.financebackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import com.example.financebackend.service.ocr.ReceiptFeatures;
import com.example.financebackend.service.ocr.ReceiptParser;
import com.example.financebackend.service.ocr.ReceiptFeatureExtractor;
import com.example.financebackend.service.ocr.ReceiptData;
import com.example.financebackend.repository.AiScanLogRepository;
import com.example.financebackend.model.AiScanLog;
import com.example.financebackend.model.Category;

import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.type.*;
import smile.data.Tuple;
import smile.data.vector.IntVector;

/**
 * Expense classifier service using RandomForest machine learning from Smile ML.
 * Falls back to rule-based keyword matching if the model is not trained.
 */
@Service
public class ExpenseClassifierService {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseClassifierService.class);
    private static final String MODEL_PATH = "uploads/random_forest_model.ser";

    private final Map<String, String> keywordCategoryMap;
    private final AiScanLogRepository aiScanLogRepository;
    private final ReceiptParser receiptParser;
    private final ReceiptFeatureExtractor receiptFeatureExtractor;

    private RandomForest model;
    private StructType schema;

    public ExpenseClassifierService(AiScanLogRepository aiScanLogRepository,
                                    ReceiptParser receiptParser,
                                    ReceiptFeatureExtractor receiptFeatureExtractor) {
        this.aiScanLogRepository = aiScanLogRepository;
        this.receiptParser = receiptParser;
        this.receiptFeatureExtractor = receiptFeatureExtractor;

        keywordCategoryMap = new HashMap<>();
        // Food & Beverage
        keywordCategoryMap.put("com", "food");
        keywordCategoryMap.put("pho", "food");
        keywordCategoryMap.put("bun", "food");
        keywordCategoryMap.put("cafe", "food");
        keywordCategoryMap.put("coffee", "food");
        keywordCategoryMap.put("tra", "food");
        keywordCategoryMap.put("sua", "food");
        keywordCategoryMap.put("banh", "food");
        keywordCategoryMap.put("rice", "food");
        keywordCategoryMap.put("chicken", "food");
        keywordCategoryMap.put("ga", "food");
        keywordCategoryMap.put("bo", "food");

        // Transport
        keywordCategoryMap.put("grab", "transport");
        keywordCategoryMap.put("taxi", "transport");
        keywordCategoryMap.put("xang", "transport");
        keywordCategoryMap.put("petrol", "transport");
        keywordCategoryMap.put("bus", "transport");
        keywordCategoryMap.put("parking", "transport");

        // Shopping
        keywordCategoryMap.put("ao", "shopping");
        keywordCategoryMap.put("quan", "shopping");
        keywordCategoryMap.put("giay", "shopping");
        keywordCategoryMap.put("dep", "shopping");
        keywordCategoryMap.put("clothes", "shopping");
        keywordCategoryMap.put("shoes", "shopping");

        // Bills
        keywordCategoryMap.put("dien", "bills");
        keywordCategoryMap.put("nuoc", "bills");
        keywordCategoryMap.put("internet", "bills");
        keywordCategoryMap.put("phone", "bills");
        keywordCategoryMap.put("electric", "bills");
        keywordCategoryMap.put("water", "bills");

        // Entertainment
        keywordCategoryMap.put("phim", "entertainment");
        keywordCategoryMap.put("game", "entertainment");
        keywordCategoryMap.put("movie", "entertainment");
        keywordCategoryMap.put("karaoke", "entertainment");

        // Health
        keywordCategoryMap.put("thuoc", "health");
        keywordCategoryMap.put("medicine", "health");
        keywordCategoryMap.put("doctor", "health");
        keywordCategoryMap.put("hospital", "health");
        keywordCategoryMap.put("benh vien", "health");

        // Education
        keywordCategoryMap.put("sach", "education");
        keywordCategoryMap.put("book", "education");
        keywordCategoryMap.put("hoc", "education");
        keywordCategoryMap.put("course", "education");

        // YOLO classes mapping
        keywordCategoryMap.put("bottled_water", "food");
        keywordCategoryMap.put("milk_tea", "food");
        keywordCategoryMap.put("coffee_cup", "food");
        keywordCategoryMap.put("fastfood", "food");
        keywordCategoryMap.put("noodle", "food");
        keywordCategoryMap.put("rice_meal", "food");
        keywordCategoryMap.put("snack", "food");
        keywordCategoryMap.put("soft_drink", "food");
        keywordCategoryMap.put("bread", "food");
        keywordCategoryMap.put("clothes", "shopping");
        keywordCategoryMap.put("cosmetic", "shopping");
        keywordCategoryMap.put("shoes", "shopping");
        keywordCategoryMap.put("electronic_item", "shopping");
        keywordCategoryMap.put("helmet", "transport");
        keywordCategoryMap.put("motorbike", "transport");
        keywordCategoryMap.put("taxi_car", "transport");
        keywordCategoryMap.put("medicine", "health");
        keywordCategoryMap.put("toy_game", "entertainment");
    }

    @PostConstruct
    public void init() {
        // Construct standard StructType schema for the 18 double features
        StructField[] fields = new StructField[18];
        for (int i = 0; i < 18; i++) {
            fields[i] = new StructField("V" + i, DataTypes.DoubleType);
        }
        schema = new StructType(fields);

        // Load pre-trained model if exists
        File modelFile = new File(MODEL_PATH);
        if (modelFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelFile))) {
                model = (RandomForest) ois.readObject();
                logger.info("Loaded RandomForest model successfully from: {}", MODEL_PATH);
            } catch (Exception e) {
                logger.error("Failed to load RandomForest model, falling back to rule-based classification.", e);
            }
        } else {
            logger.info("No pre-trained RandomForest model found at {}. Using rule-based classification fallback.", MODEL_PATH);
        }
    }

    public String classify(String productName) {
        if (productName == null || productName.isBlank()) {
            return "other";
        }

        String lowerName = productName.toLowerCase().trim();

        for (Map.Entry<String, String> entry : keywordCategoryMap.entrySet()) {
            if (lowerName.contains(entry.getKey())) {
                logger.info("Classified '{}' as '{}' (matched keyword: '{}')", productName, entry.getValue(), entry.getKey());
                return entry.getValue();
            }
        }

        logger.info("Could not classify '{}', defaulting to 'other'", productName);
        return "other";
    }

    public String classify(ReceiptFeatures features) {
        if (features == null) {
            return "other";
        }

        if (model != null) {
            try {
                double[] x = features.getVector().stream().mapToDouble(Double::doubleValue).toArray();
                Tuple tuple = Tuple.of(x, schema);
                int predictedLabel = model.predict(tuple);
                String keyword = mapLabelToKeyword(predictedLabel);
                logger.info("Classified receipt features using RandomForest as '{}' (label: {})", keyword, predictedLabel);
                return keyword;
            } catch (Exception e) {
                logger.error("Failed to predict using RandomForest model, falling back to rule-based.", e);
            }
        }

        // Fallback to rule-based
        int foodScore = features.getFoodKeywordCount() + features.getDrinkKeywordCount();
        int transportScore = features.getTransportKeywordCount() + features.getMerchantTransport();
        int shoppingScore = features.getShoppingKeywordCount()
                + features.getMerchantSupermarket()
                + features.getMerchantConvenienceStore();
        int billScore = features.getBillKeywordCount();

        String bestCategory = "other";
        int bestScore = 0;

        if (foodScore > bestScore) {
            bestScore = foodScore;
            bestCategory = "food";
        }
        if (transportScore > bestScore) {
            bestScore = transportScore;
            bestCategory = "transport";
        }
        if (shoppingScore > bestScore) {
            bestScore = shoppingScore;
            bestCategory = "shopping";
        }
        if (billScore > bestScore) {
            bestCategory = "bills";
        }

        logger.info("Classified receipt features using Rule-based fallback as '{}'", bestCategory);
        return bestCategory;
    }

    @Transactional
    public void retrain() {
        logger.info("Model retraining triggered using smile-core RandomForest...");

        List<AiScanLog> logs = aiScanLogRepository.findAll();
        List<AiScanLog> trainingLogs = logs.stream()
                .filter(log -> log.getActualCategory() != null && log.getRawOcrText() != null && !log.getRawOcrText().isBlank())
                .toList();

        if (trainingLogs.isEmpty()) {
            logger.warn("No valid training logs found in database. Retraining cancelled.");
            return;
        }

        logger.info("Found {} valid training logs. Preparing features...", trainingLogs.size());

        List<double[]> featuresList = new ArrayList<>();
        List<Integer> labelsList = new ArrayList<>();

        for (AiScanLog log : trainingLogs) {
            try {
                ReceiptData receiptData = receiptParser.parse(log.getRawOcrText());
                ReceiptFeatures features = receiptFeatureExtractor.extract(receiptData);

                double[] x = features.getVector().stream().mapToDouble(Double::doubleValue).toArray();
                int y = mapCategoryToLabel(log.getActualCategory());

                featuresList.add(x);
                labelsList.add(y);
            } catch (Exception e) {
                logger.error("Failed to process log ID: {} during feature extraction", log.getAiScanLogId(), e);
            }
        }

        if (featuresList.size() < 2) {
            logger.warn("Too few training samples ({}) to fit RandomForest model.", featuresList.size());
            return;
        }

        double[][] xData = featuresList.toArray(new double[0][]);
        int[] yData = labelsList.stream().mapToInt(Integer::intValue).toArray();

        try {
            String[] featureNames = new String[18];
            for (int i = 0; i < 18; i++) featureNames[i] = "V" + i;

            DataFrame df = DataFrame.of(xData, featureNames);
            df = df.merge(IntVector.of("class", yData));

            logger.info("Fitting RandomForest with 100 trees...");
            RandomForest trainedModel = RandomForest.fit(Formula.lhs("class"), df);

            File dir = new File("uploads");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MODEL_PATH))) {
                oos.writeObject(trainedModel);
                logger.info("Saved trained RandomForest model to: {}", MODEL_PATH);
            }

            this.model = trainedModel;
            logger.info("RandomForest model retrained and reloaded successfully!");

        } catch (Exception e) {
            logger.error("Error during RandomForest model training", e);
            throw new RuntimeException("Huấn luyện mô hình thất bại: " + e.getMessage(), e);
        }
    }

    private int mapCategoryToLabel(Category category) {
        if (category == null) return 7;
        String name = category.getCategoryName().toLowerCase();
        if (name.contains("ăn") || name.contains("uống") || name.contains("food") || name.contains("drink")) return 0;
        if (name.contains("di chuyển") || name.contains("xe") || name.contains("transport") || name.contains("grab") || name.contains("xăng")) return 1;
        if (name.contains("mua sắm") || name.contains("shopping") || name.contains("áo") || name.contains("quần")) return 2;
        if (name.contains("hóa đơn") || name.contains("bills") || name.contains("điện") || name.contains("nước")) return 3;
        if (name.contains("giải trí") || name.contains("entertainment") || name.contains("phim") || name.contains("game")) return 4;
        if (name.contains("sức khỏe") || name.contains("y tế") || name.contains("health") || name.contains("thuốc")) return 5;
        if (name.contains("giáo dục") || name.contains("học") || name.contains("education") || name.contains("sách")) return 6;
        return 7;
    }

    private String mapLabelToKeyword(int label) {
        switch (label) {
            case 0: return "food";
            case 1: return "transport";
            case 2: return "shopping";
            case 3: return "bills";
            case 4: return "entertainment";
            case 5: return "health";
            case 6: return "education";
            default: return "other";
        }
    }
}
