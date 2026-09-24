package com.example.financebackend.service;

import com.example.financebackend.model.AiScanLog;
import com.example.financebackend.model.Category;
import com.example.financebackend.model.User;
import com.example.financebackend.repository.AiScanLogRepository;
import com.example.financebackend.repository.CategoryRepository;
import com.example.financebackend.repository.TransactionRepository;
import com.example.financebackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AiScanService {

    private final AiScanLogRepository aiScanLogRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public AiScanService(AiScanLogRepository aiScanLogRepository,
                         UserRepository userRepository,
                         CategoryRepository categoryRepository,
                         TransactionRepository transactionRepository) {
        this.aiScanLogRepository = aiScanLogRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public AiScanLog saveLog(Integer userId, String rawOcrText, String detectedMerchant,
                              BigDecimal detectedAmount, LocalDate detectedDate,
                              Integer suggestedCategoryId, BigDecimal confidenceScore) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Category suggestedCategory = null;
        if (suggestedCategoryId != null) {
            suggestedCategory = categoryRepository.findById(suggestedCategoryId)
                    .orElse(null);
        }

        AiScanLog log = AiScanLog.builder()
                .user(user)
                .rawOcrText(rawOcrText)
                .detectedMerchant(detectedMerchant)
                .detectedAmount(detectedAmount)
                .detectedDate(detectedDate)
                .suggestedCategory(suggestedCategory)
                .confidenceScore(confidenceScore)
                .build();

        return aiScanLogRepository.save(log);
    }

    @Transactional
    public AiScanLog saveFeedback(Integer aiScanLogId, Integer transactionId, Integer actualCategoryId) {
        AiScanLog log = aiScanLogRepository.findById(aiScanLogId)
                .orElseThrow(() -> new RuntimeException("AI scan log not found with id: " + aiScanLogId));

        if (transactionId != null) {
            log.setTransaction(transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId)));
        }

        Category actualCategory = null;
        if (actualCategoryId != null) {
            actualCategory = categoryRepository.findById(actualCategoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + actualCategoryId));
        }

        log.setActualCategory(actualCategory);
        boolean wasCorrected = actualCategory != null
                && (log.getSuggestedCategory() == null
                || !log.getSuggestedCategory().getCategoryId().equals(actualCategory.getCategoryId()));
        log.setWasCorrected(wasCorrected);
        log.setConfirmedAt(LocalDateTime.now());

        return aiScanLogRepository.save(log);
    }
}
