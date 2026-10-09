package com.example.financebackend.service;

import com.example.financebackend.dto.BudgetDTO;
import com.example.financebackend.exception.ResourceNotFoundException;
import com.example.financebackend.model.Budget;
import com.example.financebackend.model.Category;
import com.example.financebackend.model.Transaction;
import com.example.financebackend.model.User;
import com.example.financebackend.repository.BudgetRepository;
import com.example.financebackend.repository.CategoryRepository;
import com.example.financebackend.repository.TransactionRepository;
import com.example.financebackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User user;
    private Category foodCategory;
    private Category transportCategory;
    private Budget budget;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1)
                .fullName("Test User")
                .email("test@example.com")
                .build();

        foodCategory = Category.builder()
                .categoryId(1)
                .categoryName("Ăn uống")
                .categoryType("expense")
                .build();

        transportCategory = Category.builder()
                .categoryId(2)
                .categoryName("Di chuyển")
                .categoryType("expense")
                .build();

        budget = Budget.builder()
                .budgetId(10)
                .user(user)
                .category(foodCategory)
                .budgetName("Ngân sách ăn uống T5")
                .amountLimit(new BigDecimal("1000000"))
                .dailyAmountLimit(new BigDecimal("50000"))
                .spentAmount(BigDecimal.ZERO)
                .startDate(LocalDate.of(2026, 5, 1))
                .endDate(LocalDate.of(2026, 5, 31))
                .build();
    }

    @Test
    void testGetBudgetsByUserId_Success() {
        when(budgetRepository.findByUserUserId(1)).thenReturn(List.of(budget));
        when(transactionRepository.findByUserUserIdAndTransactionDateBetween(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<BudgetDTO> result = budgetService.getBudgetsByUserId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Ngân sách ăn uống T5", result.get(0).getBudgetName());
        assertEquals(new BigDecimal("1000000"), result.get(0).getAmountLimit());
    }

    @Test
    void testGetBudgetById_Success() {
        when(budgetRepository.findById(10)).thenReturn(Optional.of(budget));
        when(transactionRepository.findByUserUserIdAndTransactionDateBetween(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        BudgetDTO result = budgetService.getBudgetById(10);

        assertNotNull(result);
        assertEquals(10, result.getBudgetId());
        assertEquals(1, result.getCategoryId());
        assertEquals("Ăn uống", result.getCategoryName());
    }

    @Test
    void testGetBudgetById_NotFound_ThrowsException() {
        when(budgetRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> budgetService.getBudgetById(999));
    }

    @Test
    void testCreateBudget_Success() {
        BudgetDTO createDTO = BudgetDTO.builder()
                .userId(1)
                .categoryId(1)
                .budgetName("Ngân sách mới")
                .amountLimit(new BigDecimal("2000000"))
                .dailyAmountLimit(new BigDecimal("100000"))
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(foodCategory));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget b = invocation.getArgument(0);
            b.setBudgetId(11);
            return b;
        });
        when(transactionRepository.findByUserUserIdAndTransactionDateBetween(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        BudgetDTO result = budgetService.createBudget(createDTO);

        assertNotNull(result);
        assertEquals(11, result.getBudgetId());
        assertEquals("Ngân sách mới", result.getBudgetName());
        assertEquals(new BigDecimal("2000000"), result.getAmountLimit());
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void testCreateBudget_UserNotFound_ThrowsException() {
        BudgetDTO createDTO = BudgetDTO.builder()
                .userId(999)
                .budgetName("Test")
                .build();

        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> budgetService.createBudget(createDTO));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void testCreateBudget_CategoryNotFound_ThrowsException() {
        BudgetDTO createDTO = BudgetDTO.builder()
                .userId(1)
                .categoryId(999)
                .budgetName("Test")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> budgetService.createBudget(createDTO));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void testCalculateSpentAmount_ExceededLimit() {
        // Budget limit: 1,000,000 VND for Food category
        Transaction tx1 = Transaction.builder()
                .transactionId(101)
                .amount(new BigDecimal("700000"))
                .transactionType("expense")
                .category(foodCategory)
                .build();

        Transaction tx2 = Transaction.builder()
                .transactionId(102)
                .amount(new BigDecimal("500000"))
                .transactionType("expense")
                .category(foodCategory)
                .build();

        // Different category - should not be counted
        Transaction txOtherCategory = Transaction.builder()
                .transactionId(103)
                .amount(new BigDecimal("400000"))
                .transactionType("expense")
                .category(transportCategory)
                .build();

        // Income - should not be counted
        Transaction txIncome = Transaction.builder()
                .transactionId(104)
                .amount(new BigDecimal("1000000"))
                .transactionType("income")
                .category(foodCategory)
                .build();

        when(transactionRepository.findByUserUserIdAndTransactionDateBetween(eq(1), any(), any()))
                .thenReturn(Arrays.asList(tx1, tx2, txOtherCategory, txIncome));

        BudgetDTO dto = budgetService.toDTO(budget);

        assertNotNull(dto);
        // Total spent on food: 700k + 500k = 1,200,000
        assertEquals(new BigDecimal("1200000"), dto.getSpentAmount());
        // Remaining: 1,000,000 - 1,200,000 = -200,000
        assertEquals(new BigDecimal("-200000"), dto.getRemainingAmount());
        // Percent used: 120.0%
        assertEquals(120.0, dto.getPercentUsed());
        // Exceeded flag must be true
        assertTrue(dto.getExceeded());
    }

    @Test
    void testCalculateSpentAmount_UnderLimit() {
        Transaction tx = Transaction.builder()
                .transactionId(101)
                .amount(new BigDecimal("400000"))
                .transactionType("expense")
                .category(foodCategory)
                .build();

        when(transactionRepository.findByUserUserIdAndTransactionDateBetween(eq(1), any(), any()))
                .thenReturn(List.of(tx));

        BudgetDTO dto = budgetService.toDTO(budget);

        assertNotNull(dto);
        assertEquals(new BigDecimal("400000"), dto.getSpentAmount());
        assertEquals(new BigDecimal("600000"), dto.getRemainingAmount());
        assertEquals(40.0, dto.getPercentUsed());
        assertFalse(dto.getExceeded());
    }

    @Test
    void testDeleteBudget_Success() {
        when(budgetRepository.existsById(10)).thenReturn(true);

        budgetService.deleteBudget(10);

        verify(budgetRepository).deleteById(10);
    }

    @Test
    void testDeleteBudget_NotFound_ThrowsException() {
        when(budgetRepository.existsById(999)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> budgetService.deleteBudget(999));
        verify(budgetRepository, never()).deleteById(any());
    }
}
