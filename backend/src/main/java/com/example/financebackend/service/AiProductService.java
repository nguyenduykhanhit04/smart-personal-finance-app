package com.example.financebackend.service;

import com.example.financebackend.model.AiProductLog;
import com.example.financebackend.model.Category;
import com.example.financebackend.model.User;
import com.example.financebackend.repository.AiProductLogRepository;
import com.example.financebackend.repository.CategoryRepository;
import com.example.financebackend.repository.TransactionRepository;
import com.example.financebackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiProductService {

    private final AiProductLogRepository aiProductLogRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public AiProductService(AiProductLogRepository aiProductLogRepository,
                            UserRepository userRepository,
                            CategoryRepository categoryRepository,
                            TransactionRepository transactionRepository) {
        this.aiProductLogRepository = aiProductLogRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public AiProductLog saveLog(Integer userId, String rawYoloJson, Integer suggestedCategoryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Category suggestedCategory = categoryRepository.findById(suggestedCategoryId)
                .orElseThrow(() -> new RuntimeException("Suggested category not found with id: " + suggestedCategoryId));

        AiProductLog log = AiProductLog.builder()
                .user(user)
                .rawYoloJson(rawYoloJson)
                .suggestedCategory(suggestedCategory)
                .build();

        return aiProductLogRepository.save(log);
    }

    @Transactional
    public AiProductLog saveFeedback(Integer aiProductLogId, Integer transactionId) {
        AiProductLog log = aiProductLogRepository.findById(aiProductLogId)
                .orElseThrow(() -> new RuntimeException("AI product log not found with id: " + aiProductLogId));

        if (transactionId != null) {
            log.setTransaction(transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId)));
        }

        return aiProductLogRepository.save(log);
    }
}
