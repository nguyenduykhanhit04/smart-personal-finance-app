package com.example.financebackend.controller;

import com.example.financebackend.dto.request.AiScanClassifyRequest;
import com.example.financebackend.dto.request.AiScanFeedbackRequest;
import com.example.financebackend.dto.response.AiScanResponse;
import com.example.financebackend.dto.response.ApiResponse;
import com.example.financebackend.dto.response.GeminiReceiptResponse;
import com.example.financebackend.model.AiScanLog;
import com.example.financebackend.model.Category;
import com.example.financebackend.service.CategoryService;
import com.example.financebackend.service.ai.AiScanService;
import com.example.financebackend.service.ai.GeminiService;
import com.example.financebackend.service.ai.ocr.ReceiptData;
import com.example.financebackend.service.ai.ocr.ReceiptParser;

import jakarta.validation.Valid;
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
    private final ReceiptParser receiptParser;

    public AiScanController(AiScanService aiScanService,
                            CategoryService categoryService,
                            GeminiService geminiService,
                            ReceiptParser receiptParser) {
        this.aiScanService = aiScanService;
        this.categoryService = categoryService;
        this.geminiService = geminiService;
        this.receiptParser = receiptParser;
    }

    @PostMapping("/classify")
    public ResponseEntity<ApiResponse<AiScanResponse>> classifyBill(@Valid @RequestBody AiScanClassifyRequest request) {
        String rawText = request.getRawOcrText();

        logger.info("OCR classify request userId={}, rawLength={}", request.getUserId(), rawText.length());
        logger.info("OCR raw text:\n{}", rawText);

        BigDecimal amount;
        LocalDate date;
        String merchant;
        String categoryKeyword;
        BigDecimal confidenceScore;

        try {
            GeminiReceiptResponse geminiResult = geminiService.analyzeReceipt(rawText);
            amount = geminiResult.getAmount();
            date = geminiResult.getDate();
            merchant = geminiResult.getMerchant();
            categoryKeyword = geminiResult.getCategory();
            confidenceScore = BigDecimal.valueOf(0.95);
            logger.info("Gemini parsed fields merchant='{}', amount={}, date={}, categoryKeyword='{}'",
                    merchant, amount, date, categoryKeyword);
        } catch (Exception e) {
            logger.warn("Gemini API call failed or unavailable ({}). Falling back to local ReceiptParser heuristic regex.", e.getMessage());
            ReceiptData parsed = receiptParser.parse(rawText);
            amount = parsed.getAmount();
            date = parsed.getDate();
            merchant = parsed.getMerchant();
            categoryKeyword = guessFallbackCategory(parsed.getNormalizedText(), merchant);
            confidenceScore = BigDecimal.valueOf(0.65);
            logger.info("Fallback regex parsed fields merchant='{}', amount={}, date={}, categoryKeyword='{}'",
                    merchant, amount, date, categoryKeyword);
        }





        logger.info("OCR parsed fields merchant='{}', amount={}, date={}, categoryKeyword='{}'",
                merchant, amount, date, categoryKeyword);

        Category matchedCategory = categoryService.findCategoryByKeyword(request.getUserId(), categoryKeyword);
        Integer categoryId = matchedCategory != null ? matchedCategory.getCategoryId() : null;
        String categoryName = matchedCategory != null ? matchedCategory.getCategoryName() : "Khác";
        logger.info("OCR category result keyword='{}', categoryId={}, categoryName='{}'",
                categoryKeyword, categoryId, categoryName);


        AiScanLog log = aiScanService.saveLog(
                request.getUserId(),
                rawText,
                merchant,
                amount,
                date,
                categoryId,
                confidenceScore
        );

        AiScanResponse result = new AiScanResponse();
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
    public ResponseEntity<ApiResponse<String>> saveFeedback(@Valid @RequestBody AiScanFeedbackRequest request) {
        aiScanService.saveFeedback(
                request.getAiScanLogId(),
                request.getTransactionId(),
                request.getActualCategoryId()
        );
        return ResponseEntity.ok(ApiResponse.success("Feedback saved successfully", "Thank you for your feedback"));
    }

    private String guessFallbackCategory(String normalizedText, String merchant) {
        String combined = ((merchant != null ? merchant : "") + " " + (normalizedText != null ? normalizedText : "")).toLowerCase();
        if (combined.contains("cafe") || combined.contains("coffee") || combined.contains("phuc long")
                || combined.contains("highlands") || combined.contains("tra sua") || combined.contains("tea")
                || combined.contains("com") || combined.contains("pho") || combined.contains("bun")
                || combined.contains("banh") || combined.contains("food") || combined.contains("ga") || combined.contains("bo")) {
            return "food";
        }
        if (combined.contains("grab") || combined.contains("taxi") || combined.contains("xang")
                || combined.contains("petrol") || combined.contains("bus") || combined.contains("be ") || combined.contains("gojek")) {
            return "transport";
        }
        if (combined.contains("winmart") || combined.contains("coopmart") || combined.contains("bach hoa xanh")
                || combined.contains("circle k") || combined.contains("gs25") || combined.contains("lotte mart")
                || combined.contains("sieu thi") || combined.contains("shopping") || combined.contains("quan") || combined.contains("ao") || combined.contains("giay")) {
            return "shopping";
        }
        if (combined.contains("dien") || combined.contains("nuoc") || combined.contains("internet")
                || combined.contains("wifi") || combined.contains("hoa don") || combined.contains("cuoc")) {
            return "bills";
        }
        if (combined.contains("thuoc") || combined.contains("pharmacy") || combined.contains("benh vien") || combined.contains("kham")) {
            return "health";
        }
        if (combined.contains("phim") || combined.contains("game") || combined.contains("karaoke") || combined.contains("cinema")) {
            return "entertainment";
        }
        return "other";
    }

}
