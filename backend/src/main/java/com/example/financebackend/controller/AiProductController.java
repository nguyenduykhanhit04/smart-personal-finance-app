package com.example.financebackend.controller;

import com.example.financebackend.dto.ApiResponse;
import com.example.financebackend.model.AiProductLog;
import com.example.financebackend.model.Category;
import com.example.financebackend.repository.CategoryRepository;
import com.example.financebackend.service.AiProductService;
import com.example.financebackend.service.ProductClassifierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-product")
public class AiProductController {

    private static final Logger logger = LoggerFactory.getLogger(AiProductController.class);

    private final ProductClassifierService productClassifierService;
    private final AiProductService aiProductService;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    public AiProductController(ProductClassifierService productClassifierService,
                               AiProductService aiProductService,
                               CategoryRepository categoryRepository,
                               ObjectMapper objectMapper) {
        this.productClassifierService = productClassifierService;
        this.aiProductService = aiProductService;
        this.categoryRepository = categoryRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/classify")
    public ResponseEntity<ApiResponse<AiProductResult>> classifyProduct(@RequestBody ProductClassificationRequest request) {
        if (request.getDetections() == null || request.getDetections().isEmpty()) {
            throw new IllegalArgumentException("Detections list is empty");
        }

        logger.info("YOLO classify request userId={}, detectionsCount={}", request.getUserId(), request.getDetections().size());

        int suggestedCategoryId = productClassifierService.classify(request.getDetections());

        Category category = categoryRepository.findById(suggestedCategoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + suggestedCategoryId));
        String categoryName = category.getCategoryName();

        String rawYoloJson = "[]";
        try {
            rawYoloJson = objectMapper.writeValueAsString(request.getDetections());
        } catch (Exception e) {
            logger.error("Failed to serialize YOLO detections to JSON", e);
        }

        AiProductLog log = aiProductService.saveLog(request.getUserId(), rawYoloJson, suggestedCategoryId);

        AiProductResult result = new AiProductResult();
        result.setAiProductLogId(log.getAiProductLogId());
        result.setSuggestedCategoryId(suggestedCategoryId);
        result.setSuggestedCategoryName(categoryName);

        return ResponseEntity.ok(ApiResponse.success("Product classified successfully", result));
    }

    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse<String>> saveFeedback(@RequestBody ProductFeedbackRequest request) {
        if (request.getAiProductLogId() == null || request.getAiProductLogId() <= 0) {
            throw new IllegalArgumentException("AI product log id is required");
        }
        aiProductService.saveFeedback(request.getAiProductLogId(), request.getTransactionId());
        return ResponseEntity.ok(ApiResponse.success("Feedback saved successfully", "Thank you for your feedback"));
    }

    @Data
    public static class ProductClassificationRequest {
        private Integer userId;
        private List<ProductClassifierService.YoloDetection> detections;
    }

    @Data
    public static class ProductFeedbackRequest {
        private Integer aiProductLogId;
        private Integer transactionId;
    }

    @Data
    public static class AiProductResult {
        private Integer aiProductLogId;
        private Integer suggestedCategoryId;
        private String suggestedCategoryName;
    }
}
