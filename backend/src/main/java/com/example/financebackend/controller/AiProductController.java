package com.example.financebackend.controller;

import com.example.financebackend.dto.CategoryDTO;
import com.example.financebackend.dto.request.AiProductClassifyRequest;
import com.example.financebackend.dto.request.AiProductFeedbackRequest;
import com.example.financebackend.dto.response.AiProductResponse;
import com.example.financebackend.dto.response.ApiResponse;
import com.example.financebackend.exception.ResourceNotFoundException;
import com.example.financebackend.model.AiProductLog;
import com.example.financebackend.service.CategoryService;
import com.example.financebackend.service.ai.AiProductService;
import com.example.financebackend.service.ai.ProductClassifierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-product")
public class AiProductController {

    private static final Logger logger = LoggerFactory.getLogger(AiProductController.class);

    private final ProductClassifierService productClassifierService;
    private final AiProductService aiProductService;
    private final CategoryService categoryService;
    private final ObjectMapper objectMapper;

    public AiProductController(ProductClassifierService productClassifierService,
                               AiProductService aiProductService,
                               CategoryService categoryService,
                               ObjectMapper objectMapper) {
        this.productClassifierService = productClassifierService;
        this.aiProductService = aiProductService;
        this.categoryService = categoryService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/classify")
    public ResponseEntity<ApiResponse<AiProductResponse>> classifyProduct(@Valid @RequestBody AiProductClassifyRequest request) {
        logger.info("YOLO classify request userId={}, detectionsCount={}", request.getUserId(), request.getDetections().size());

        int suggestedCategoryId = productClassifierService.classify(request.getDetections());

        CategoryDTO category = categoryService.getCategoriesByUserId(request.getUserId()).stream()
                .filter(c -> c.getCategoryId().equals(suggestedCategoryId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + suggestedCategoryId));
        String categoryName = category.getCategoryName();

        String rawYoloJson = "[]";
        try {
            rawYoloJson = objectMapper.writeValueAsString(request.getDetections());
        } catch (Exception e) {
            logger.error("Failed to serialize YOLO detections to JSON", e);
        }

        AiProductLog log = aiProductService.saveLog(request.getUserId(), rawYoloJson, suggestedCategoryId);

        AiProductResponse result = new AiProductResponse();
        result.setAiProductLogId(log.getAiProductLogId());
        result.setSuggestedCategoryId(suggestedCategoryId);
        result.setSuggestedCategoryName(categoryName);

        return ResponseEntity.ok(ApiResponse.success("Product classified successfully", result));
    }

    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse<String>> saveFeedback(@Valid @RequestBody AiProductFeedbackRequest request) {
        aiProductService.saveFeedback(request.getAiProductLogId(), request.getTransactionId());
        return ResponseEntity.ok(ApiResponse.success("Feedback saved successfully", "Thank you for your feedback"));
    }

}
