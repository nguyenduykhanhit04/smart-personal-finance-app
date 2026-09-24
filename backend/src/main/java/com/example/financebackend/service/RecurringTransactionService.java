package com.example.financebackend.service;

import com.example.financebackend.config.AppTime;
import com.example.financebackend.dto.RecurringTransactionDTO;
import com.example.financebackend.dto.TransactionDTO;
import com.example.financebackend.model.*;
import com.example.financebackend.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionService transactionService;

    public RecurringTransactionService(RecurringTransactionRepository recurringTransactionRepository,
                                       UserRepository userRepository,
                                       AccountRepository accountRepository,
                                       CategoryRepository categoryRepository,
                                       TransactionService transactionService) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionService = transactionService;
    }

    public List<RecurringTransactionDTO> getRecurringByUserId(Integer userId) {
        return recurringTransactionRepository.findByUserUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RecurringTransactionDTO createRecurring(RecurringTransactionDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId()).orElse(null);
        }

        RecurringTransaction rt = RecurringTransaction.builder()
                .user(user)
                .account(account)
                .category(category)
                .title(dto.getTitle())
                .amount(dto.getAmount())
                .transactionType(dto.getTransactionType())
                .repeatType(dto.getRepeatType())
                .repeatInterval(dto.getRepeatInterval() != null ? dto.getRepeatInterval() : 1)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .nextRunDate(dto.getStartDate()) // Initial next run is start date
                .note(dto.getNote())
                .isActive(true)
                .build();

        rt = recurringTransactionRepository.save(rt);
        return toDTO(rt);
    }

    @Transactional
    public RecurringTransactionDTO updateRecurring(Integer id, RecurringTransactionDTO dto) {
        RecurringTransaction rt = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurring transaction not found"));

        if (dto.getTitle() != null) rt.setTitle(dto.getTitle());
        if (dto.getAmount() != null) rt.setAmount(dto.getAmount());
        if (dto.getTransactionType() != null) rt.setTransactionType(dto.getTransactionType());
        if (dto.getRepeatType() != null) rt.setRepeatType(dto.getRepeatType());
        if (dto.getRepeatInterval() != null) rt.setRepeatInterval(dto.getRepeatInterval());
        if (dto.getStartDate() != null) {
            rt.setStartDate(dto.getStartDate());
            if (dto.getNextRunDate() == null) {
                rt.setNextRunDate(dto.getStartDate());
            }
        }
        if (dto.getEndDate() != null) rt.setEndDate(dto.getEndDate());
        if (dto.getNextRunDate() != null) rt.setNextRunDate(dto.getNextRunDate());
        if (dto.getNote() != null) rt.setNote(dto.getNote());
        if (dto.getIsActive() != null) rt.setIsActive(dto.getIsActive());

        if (dto.getAccountId() != null) {
            Account account = accountRepository.findById(dto.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            rt.setAccount(account);
        }

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId()).orElse(null);
            rt.setCategory(category);
        }

        rt = recurringTransactionRepository.save(rt);
        return toDTO(rt);
    }

    @Transactional
    public void deleteRecurring(Integer id) {
        recurringTransactionRepository.deleteById(id);
    }

    // Cron job running daily at midnight (12:05 AM) to automatically generate due transactions!
    @Scheduled(cron = "0 5 0 * * ?", zone = AppTime.TIME_ZONE)
    @Transactional
    public void processRecurringTransactions() {
        LocalDate today = AppTime.today();
        List<RecurringTransaction> dueList = recurringTransactionRepository
                .findByIsActiveTrueAndNextRunDateLessThanEqual(today);

        for (RecurringTransaction rt : dueList) {
            // Check if expired
            if (rt.getEndDate() != null && today.isAfter(rt.getEndDate())) {
                rt.setIsActive(false);
                recurringTransactionRepository.save(rt);
                continue;
            }

            // Create real transaction
            TransactionDTO tx = TransactionDTO.builder()
                    .userId(rt.getUser().getUserId())
                    .accountId(rt.getAccount().getAccountId())
                    .categoryId(rt.getCategory() != null ? rt.getCategory().getCategoryId() : null)
                    .title(rt.getTitle())
                    .amount(rt.getAmount())
                    .transactionType(rt.getTransactionType())
                    .transactionDate(today)
                    .note(rt.getNote() != null ? rt.getNote() : "Giao dịch định kỳ tự động")
                    .status("confirmed")
                    .build();

            transactionService.createTransaction(tx);

            // Calculate next run date
            LocalDate nextRun = calculateNextRunDate(
                    rt.getNextRunDate(),
                    rt.getStartDate(),
                    rt.getRepeatType(),
                    rt.getRepeatInterval()
            );
            rt.setNextRunDate(nextRun);

            // Deactivate if past end date
            if (rt.getEndDate() != null && nextRun.isAfter(rt.getEndDate())) {
                rt.setIsActive(false);
            }

            recurringTransactionRepository.save(rt);
        }
    }

    private LocalDate calculateNextRunDate(LocalDate current, LocalDate startDate, String repeatType, int interval) {
        int safeInterval = Math.max(interval, 1);
        switch (repeatType.toUpperCase()) {
            case "DAILY":
                return current.plusDays(safeInterval);
            case "WEEKLY":
                return current.plusWeeks(safeInterval);
            case "MONTHLY":
                return calculateMonthlyNextRunDate(current, startDate, safeInterval);
            case "YEARLY":
                return calculateYearlyNextRunDate(current, startDate, safeInterval);
            default:
                return calculateMonthlyNextRunDate(current, startDate, 1);
        }
    }

    private LocalDate calculateMonthlyNextRunDate(LocalDate current, LocalDate startDate, int interval) {
        int targetDay = startDate.getDayOfMonth();
        YearMonth nextMonth = YearMonth.from(current).plusMonths(interval);
        int safeDay = Math.min(targetDay, nextMonth.lengthOfMonth());
        return nextMonth.atDay(safeDay);
    }

    private LocalDate calculateYearlyNextRunDate(LocalDate current, LocalDate startDate, int interval) {
        int targetDay = startDate.getDayOfMonth();
        YearMonth nextYearMonth = YearMonth.from(current.plusYears(interval));
        int safeDay = Math.min(targetDay, nextYearMonth.lengthOfMonth());
        return nextYearMonth.atDay(safeDay);
    }

    public RecurringTransactionDTO toDTO(RecurringTransaction rt) {
        return RecurringTransactionDTO.builder()
                .recurringId(rt.getRecurringId())
                .userId(rt.getUser().getUserId())
                .accountId(rt.getAccount().getAccountId())
                .categoryId(rt.getCategory() != null ? rt.getCategory().getCategoryId() : null)
                .categoryName(rt.getCategory() != null ? rt.getCategory().getCategoryName() : null)
                .categoryColor(rt.getCategory() != null ? rt.getCategory().getColor() : null)
                .title(rt.getTitle())
                .amount(rt.getAmount())
                .transactionType(rt.getTransactionType())
                .repeatType(rt.getRepeatType())
                .repeatInterval(rt.getRepeatInterval())
                .startDate(rt.getStartDate())
                .endDate(rt.getEndDate())
                .nextRunDate(rt.getNextRunDate())
                .note(rt.getNote())
                .isActive(rt.getIsActive())
                .createdAt(rt.getCreatedAt())
                .updatedAt(rt.getUpdatedAt())
                .build();
    }
}
