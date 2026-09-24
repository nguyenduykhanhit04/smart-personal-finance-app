package com.example.financebackend.service;

import com.example.financebackend.config.AppTime;
import com.example.financebackend.dto.TransactionDTO;
import com.example.financebackend.model.*;
import com.example.financebackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final AccountService accountService;
    private final TransactionImageRepository transactionImageRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              UserRepository userRepository,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository,
                              AccountService accountService,
                              TransactionImageRepository transactionImageRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.accountService = accountService;
        this.transactionImageRepository = transactionImageRepository;
    }

    public List<TransactionDTO> getTransactionsByUserId(Integer userId) {
        return transactionRepository.findByUserUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getTransactionsByDateRange(Integer userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserUserIdAndTransactionDateBetween(userId, startDate, endDate)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO getTransactionById(Integer transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));
        return toDTO(transaction);
    }

    @Transactional
    public TransactionDTO createTransaction(TransactionDTO dto) {
        validateTransactionDateNotInFuture(dto.getTransactionDate());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

        Account account;
        if (dto.getAccountId() != null) {
            account = accountRepository.findById(dto.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found with id: " + dto.getAccountId()));
        } else {
            // Auto-assign default wallet "Ví chính"
            account = accountService.getOrCreateDefaultAccount(dto.getUserId());
        }

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
        }

        Transaction transaction = Transaction.builder()
                .user(user)
                .account(account)
                .category(category)
                .title(dto.getTitle())
                .amount(dto.getAmount())
                .transactionType(dto.getTransactionType())
                .transactionDate(dto.getTransactionDate())
                .note(dto.getNote())
                .status(dto.getStatus() != null ? dto.getStatus() : "confirmed")
                .build();

        transaction = transactionRepository.save(transaction);

        // Update account balance using resolved account's accountId
        accountService.updateBalance(account.getAccountId(), dto.getAmount(), dto.getTransactionType());

        return toDTO(transaction);
    }

    @Transactional
    public TransactionDTO updateTransaction(Integer transactionId, TransactionDTO dto) {
        if (dto.getTransactionDate() != null) {
            validateTransactionDateNotInFuture(dto.getTransactionDate());
        }

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));

        // Reverse old balance effect
        accountService.updateBalance(
                transaction.getAccount().getAccountId(),
                transaction.getAmount(),
                "income".equalsIgnoreCase(transaction.getTransactionType()) ? "expense" : "income"
        );

        if (dto.getAccountId() != null) {
            Account account = accountRepository.findById(dto.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found with id: " + dto.getAccountId()));
            transaction.setAccount(account);
        }
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
            transaction.setCategory(category);
        }
        if (dto.getTitle() != null) transaction.setTitle(dto.getTitle());
        if (dto.getAmount() != null) transaction.setAmount(dto.getAmount());
        if (dto.getTransactionType() != null) transaction.setTransactionType(dto.getTransactionType());
        if (dto.getTransactionDate() != null) transaction.setTransactionDate(dto.getTransactionDate());
        if (dto.getNote() != null) transaction.setNote(dto.getNote());
        if (dto.getStatus() != null) transaction.setStatus(dto.getStatus());

        transaction = transactionRepository.save(transaction);

        // Apply new balance effect
        accountService.updateBalance(
                transaction.getAccount().getAccountId(),
                transaction.getAmount(),
                transaction.getTransactionType()
        );

        return toDTO(transaction);
    }

    @Transactional
    public void deleteTransaction(Integer transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));

        // Reverse balance effect
        accountService.updateBalance(
                transaction.getAccount().getAccountId(),
                transaction.getAmount(),
                "income".equalsIgnoreCase(transaction.getTransactionType()) ? "expense" : "income"
        );

        transactionRepository.delete(transaction);
    }

    private void validateTransactionDateNotInFuture(LocalDate transactionDate) {
        if (transactionDate == null) {
            throw new RuntimeException("Transaction date is required");
        }
        if (transactionDate.isAfter(AppTime.today())) {
            throw new RuntimeException("Không thể tạo giao dịch cho ngày trong tương lai");
        }
    }

    public TransactionDTO toDTO(Transaction transaction) {
        List<TransactionImage> images = transactionImageRepository.findByTransactionTransactionId(transaction.getTransactionId());
        String imageUrl = images.isEmpty() ? null : images.get(0).getImageUrl();

        return TransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                .userId(transaction.getUser().getUserId())
                .accountId(transaction.getAccount().getAccountId())
                .accountName(transaction.getAccount().getAccountName())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getCategoryId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getCategoryName() : null)
                .title(transaction.getTitle())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .transactionDate(transaction.getTransactionDate())
                .note(transaction.getNote())
                .status(transaction.getStatus())
                .imageUrl(imageUrl)
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
