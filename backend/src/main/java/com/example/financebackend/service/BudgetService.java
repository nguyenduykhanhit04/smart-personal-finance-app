package com.example.financebackend.service;

import com.example.financebackend.dto.BudgetDTO;
import com.example.financebackend.model.Budget;
import com.example.financebackend.model.Category;
import com.example.financebackend.model.Transaction;
import com.example.financebackend.model.User;
import com.example.financebackend.repository.BudgetRepository;
import com.example.financebackend.repository.CategoryRepository;
import com.example.financebackend.repository.TransactionRepository;
import com.example.financebackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         UserRepository userRepository,
                         CategoryRepository categoryRepository,
                         TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<BudgetDTO> getBudgetsByUserId(Integer userId) {
        return budgetRepository.findByUserUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<BudgetDTO> getActiveBudgets(Integer userId, LocalDate date) {
        return budgetRepository.findByUserUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userId, date, date)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BudgetDTO getBudgetById(Integer budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + budgetId));
        return toDTO(budget);
    }

    @Transactional
    public BudgetDTO createBudget(BudgetDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
        }

        Budget budget = Budget.builder()
                .user(user)
                .category(category)
                .budgetName(dto.getBudgetName())
                .amountLimit(dto.getAmountLimit())
                .dailyAmountLimit(dto.getDailyAmountLimit())
                .spentAmount(BigDecimal.ZERO)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();

        budget = budgetRepository.save(budget);
        return toDTO(budget);
    }

    @Transactional
    public BudgetDTO updateBudget(Integer budgetId, BudgetDTO dto) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + budgetId));

        if (dto.getBudgetName() != null) budget.setBudgetName(dto.getBudgetName());
        if (dto.getAmountLimit() != null) budget.setAmountLimit(dto.getAmountLimit());
        budget.setDailyAmountLimit(dto.getDailyAmountLimit());
        if (dto.getStartDate() != null) budget.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) budget.setEndDate(dto.getEndDate());
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
            budget.setCategory(category);
        }

        budget = budgetRepository.save(budget);
        return toDTO(budget);
    }

    @Transactional
    public void deleteBudget(Integer budgetId) {
        if (!budgetRepository.existsById(budgetId)) {
            throw new RuntimeException("Budget not found with id: " + budgetId);
        }
        budgetRepository.deleteById(budgetId);
    }

    public BigDecimal calculateSpentAmount(Budget budget) {
        List<Transaction> transactions = transactionRepository
                .findByUserUserIdAndTransactionDateBetween(
                        budget.getUser().getUserId(),
                        budget.getStartDate(),
                        budget.getEndDate());

        return transactions.stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getTransactionType()))
                .filter(t -> budget.getCategory() == null ||
                        (t.getCategory() != null && t.getCategory().getCategoryId().equals(budget.getCategory().getCategoryId())))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BudgetDTO toDTO(Budget budget) {
        BigDecimal spent = calculateSpentAmount(budget);
        BigDecimal remaining = budget.getAmountLimit().subtract(spent);
        double percentUsed = budget.getAmountLimit().compareTo(BigDecimal.ZERO) > 0
                ? spent.multiply(BigDecimal.valueOf(100)).divide(budget.getAmountLimit(), 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return BudgetDTO.builder()
                .budgetId(budget.getBudgetId())
                .userId(budget.getUser().getUserId())
                .categoryId(budget.getCategory() != null ? budget.getCategory().getCategoryId() : null)
                .categoryName(budget.getCategory() != null ? budget.getCategory().getCategoryName() : null)
                .budgetName(budget.getBudgetName())
                .amountLimit(budget.getAmountLimit())
                .dailyAmountLimit(budget.getDailyAmountLimit())
                .spentAmount(spent)
                .remainingAmount(remaining)
                .percentUsed(percentUsed)
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .exceeded(spent.compareTo(budget.getAmountLimit()) > 0)
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}
