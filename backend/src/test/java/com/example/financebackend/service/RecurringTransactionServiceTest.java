package com.example.financebackend.service;

import com.example.financebackend.dto.RecurringTransactionDTO;
import com.example.financebackend.exception.ResourceNotFoundException;
import com.example.financebackend.model.*;
import com.example.financebackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceTest {

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private RecurringTransactionService recurringTransactionService;

    private User sampleUser;
    private Account sampleAccount;
    private Category sampleCategory;
    private RecurringTransaction sampleRecurring;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder().userId(1).email("user@example.com").build();
        sampleAccount = Account.builder().accountId(1).build();
        sampleCategory = Category.builder().categoryId(1).categoryName("An uong").build();
        sampleRecurring = RecurringTransaction.builder()
                .recurringId(1)
                .user(sampleUser)
                .account(sampleAccount)
                .category(sampleCategory)
                .title("Tien thue nha")
                .amount(BigDecimal.valueOf(3000000))
                .transactionType("expense")
                .repeatType("MONTHLY")
                .repeatInterval(1)
                .startDate(LocalDate.of(2026, 1, 1))
                .nextRunDate(LocalDate.of(2026, 10, 1))
                .isActive(true)
                .build();
    }

    // ===== getRecurringByUserId =====

    @Test
    void getRecurringByUserId_ReturnsMappedDTOs() {
        when(recurringTransactionRepository.findByUserUserId(1)).thenReturn(List.of(sampleRecurring));

        List<RecurringTransactionDTO> result = recurringTransactionService.getRecurringByUserId(1);

        assertEquals(1, result.size());
        assertEquals("Tien thue nha", result.get(0).getTitle());
        assertEquals(1, result.get(0).getUserId());
        assertEquals("MONTHLY", result.get(0).getRepeatType());
    }

    // ===== createRecurring =====

    @Test
    void createRecurring_ValidDTO_SavesAndReturnsDTO() {
        RecurringTransactionDTO dto = RecurringTransactionDTO.builder()
                .userId(1)
                .accountId(1)
                .categoryId(1)
                .title("Bao hiem")
                .amount(BigDecimal.valueOf(500000))
                .transactionType("expense")
                .repeatType("MONTHLY")
                .repeatInterval(1)
                .startDate(LocalDate.of(2026, 10, 1))
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        when(accountRepository.findById(1)).thenReturn(Optional.of(sampleAccount));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(sampleCategory));
        when(recurringTransactionRepository.save(any(RecurringTransaction.class))).thenAnswer(inv -> {
            RecurringTransaction rt = inv.getArgument(0);
            rt.setRecurringId(2);
            return rt;
        });

        RecurringTransactionDTO result = recurringTransactionService.createRecurring(dto);

        assertNotNull(result);
        assertEquals(2, result.getRecurringId());
        assertEquals("Bao hiem", result.getTitle());
        assertEquals(LocalDate.of(2026, 10, 1), result.getNextRunDate()); // nextRunDate = startDate initially
        assertTrue(result.getIsActive());
        verify(recurringTransactionRepository).save(any(RecurringTransaction.class));
    }

    @Test
    void createRecurring_UserNotFound_ThrowsException() {
        RecurringTransactionDTO dto = RecurringTransactionDTO.builder()
                .userId(999)
                .accountId(1)
                .title("Test")
                .amount(BigDecimal.valueOf(100000))
                .repeatType("DAILY")
                .startDate(LocalDate.now())
                .build();

        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recurringTransactionService.createRecurring(dto));
        verify(recurringTransactionRepository, never()).save(any());
    }

    @Test
    void createRecurring_NoCategoryId_SavesWithNullCategory() {
        RecurringTransactionDTO dto = RecurringTransactionDTO.builder()
                .userId(1)
                .accountId(1)
                .title("Khong co danh muc")
                .amount(BigDecimal.valueOf(100000))
                .transactionType("expense")
                .repeatType("WEEKLY")
                .startDate(LocalDate.of(2026, 10, 1))
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        when(accountRepository.findById(1)).thenReturn(Optional.of(sampleAccount));
        when(recurringTransactionRepository.save(any(RecurringTransaction.class))).thenAnswer(inv -> {
            RecurringTransaction rt = inv.getArgument(0);
            rt.setRecurringId(3);
            return rt;
        });

        RecurringTransactionDTO result = recurringTransactionService.createRecurring(dto);

        assertNotNull(result);
        assertNull(result.getCategoryId());
    }

    // ===== updateRecurring =====

    @Test
    void updateRecurring_ValidDTO_UpdatesFields() {
        RecurringTransactionDTO dto = RecurringTransactionDTO.builder()
                .title("Tien thue updated")
                .amount(BigDecimal.valueOf(3500000))
                .isActive(false)
                .build();

        when(recurringTransactionRepository.findById(1)).thenReturn(Optional.of(sampleRecurring));
        when(recurringTransactionRepository.save(any(RecurringTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        RecurringTransactionDTO result = recurringTransactionService.updateRecurring(1, dto);

        assertEquals("Tien thue updated", result.getTitle());
        assertEquals(BigDecimal.valueOf(3500000), result.getAmount());
        assertFalse(result.getIsActive());
    }

    @Test
    void updateRecurring_NotFound_ThrowsException() {
        when(recurringTransactionRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> recurringTransactionService.updateRecurring(999, RecurringTransactionDTO.builder().build()));
    }

    // ===== deleteRecurring =====

    @Test
    void deleteRecurring_CallsRepository() {
        doNothing().when(recurringTransactionRepository).deleteById(1);

        recurringTransactionService.deleteRecurring(1);

        verify(recurringTransactionRepository).deleteById(1);
    }

    // ===== processRecurringTransactions (cron logic) =====

    @Test
    void processRecurringTransactions_DueTransaction_CreatesTransactionAndUpdatesNextRun() {
        sampleRecurring.setNextRunDate(LocalDate.now().minusDays(1)); // Due yesterday

        when(recurringTransactionRepository.findByIsActiveTrueAndNextRunDateLessThanEqual(any(LocalDate.class)))
                .thenReturn(List.of(sampleRecurring));
        when(transactionService.createTransaction(any())).thenReturn(null);
        when(recurringTransactionRepository.save(any(RecurringTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        recurringTransactionService.processRecurringTransactions();

        verify(transactionService).createTransaction(any());
        verify(recurringTransactionRepository).save(sampleRecurring);
        // nextRunDate should have advanced by 1 month (MONTHLY, interval=1)
        assertNotNull(sampleRecurring.getNextRunDate());
        assertTrue(sampleRecurring.getNextRunDate().isAfter(LocalDate.now().minusDays(2)));
    }

    @Test
    void processRecurringTransactions_ExpiredTransaction_DeactivatesWithoutCreating() {
        sampleRecurring.setNextRunDate(LocalDate.now());
        sampleRecurring.setEndDate(LocalDate.now().minusDays(1)); // Already expired

        when(recurringTransactionRepository.findByIsActiveTrueAndNextRunDateLessThanEqual(any(LocalDate.class)))
                .thenReturn(List.of(sampleRecurring));
        when(recurringTransactionRepository.save(any(RecurringTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        recurringTransactionService.processRecurringTransactions();

        assertFalse(sampleRecurring.getIsActive());
        verify(transactionService, never()).createTransaction(any());
    }
}
