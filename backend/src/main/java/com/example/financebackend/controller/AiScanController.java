package com.example.financebackend.controller;

import com.example.financebackend.dto.ApiResponse;
import com.example.financebackend.model.AiScanLog;
import com.example.financebackend.model.Category;
import com.example.financebackend.service.AiScanService;
import com.example.financebackend.service.CategoryService;
import com.example.financebackend.service.GeminiService;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/ai-scan")
public class AiScanController {

    private static final Logger logger = LoggerFactory.getLogger(AiScanController.class);

    private final AiScanService aiScanService;
    private final CategoryService categoryService;
    private final GeminiService geminiService;

    public AiScanController(AiScanService aiScanService,
                            CategoryService categoryService,
                            GeminiService geminiService) {
        this.aiScanService = aiScanService;
        this.categoryService = categoryService;
        this.geminiService = geminiService;
    }

    @PostMapping("/classify")
    public ResponseEntity<ApiResponse<AiScanResult>> classifyBill(@RequestBody OcrRequest request) {
        String rawText = request.getRawOcrText();
        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("OCR text is empty");
        }

        logger.info("OCR classify request userId={}, rawLength={}", request.getUserId(), rawText.length());
        logger.info("OCR raw text:\n{}", rawText);

        GeminiService.GeminiReceiptResult geminiResult = geminiService.analyzeReceipt(rawText);
        BigDecimal amount = geminiResult.getAmount();
        LocalDate date = geminiResult.getDate();
        String merchant = geminiResult.getMerchant();
        String categoryKeyword = geminiResult.getCategory();

        logger.info("OCR parsed fields merchant='{}', amount={}, date={}, categoryKeyword='{}'",
                merchant, amount, date, categoryKeyword);

        Category matchedCategory = categoryService.findCategoryByKeyword(request.getUserId(), categoryKeyword);
        Integer categoryId = matchedCategory != null ? matchedCategory.getCategoryId() : null;
        String categoryName = matchedCategory != null ? matchedCategory.getCategoryName() : "Khác";
        logger.info("OCR category result keyword='{}', categoryId={}, categoryName='{}'",
                categoryKeyword, categoryId, categoryName);

        BigDecimal confidenceScore = BigDecimal.valueOf(0.95);
        AiScanLog log = aiScanService.saveLog(
                request.getUserId(),
                rawText,
                merchant,
                amount,
                date,
                categoryId,
                confidenceScore
        );

        AiScanResult result = new AiScanResult();
        result.setAiScanLogId(log.getAiScanLogId());
        result.setDetectedMerchant(merchant);
        result.setDetectedAmount(amount);
        result.setDetectedDate(date);
        result.setSuggestedCategoryId(categoryId);
        result.setSuggestedCategoryName(categoryName);
        result.setConfidenceScore(confidenceScore);
        return ResponseEntity.ok(ApiResponse.success("Bill scanned successfully", result));
    }

    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse<String>> saveFeedback(@RequestBody ScanFeedbackRequest request) {
        if (request.getAiScanLogId() == null || request.getAiScanLogId() <= 0) {
            throw new IllegalArgumentException("AI scan log id is required");
        }
        aiScanService.saveFeedback(
                request.getAiScanLogId(),
                request.getTransactionId(),
                request.getActualCategoryId()
        );
        return ResponseEntity.ok(ApiResponse.success("Feedback saved successfully", "Thank you for your feedback"));
    }

    @Data
    public static class OcrRequest {
        private Integer userId;
        private String rawOcrText;
    }

    @Data
    public static class ScanFeedbackRequest {
        private Integer aiScanLogId;
        private Integer transactionId;
        private Integer actualCategoryId;
    }

    @Data
    public static class AiScanResult {
        private Integer aiScanLogId;
        private String detectedMerchant;
        private BigDecimal detectedAmount;
        private LocalDate detectedDate;
        private Integer suggestedCategoryId;
        private String suggestedCategoryName;
        private BigDecimal confidenceScore;
    }
}
