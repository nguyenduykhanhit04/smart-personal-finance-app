package com.example.financebackend.service;

import com.example.financebackend.dto.TransactionDTO;
import com.example.financebackend.model.Account;
import com.example.financebackend.model.Category;
import com.example.financebackend.model.Transaction;
import com.example.financebackend.model.User;
import com.example.financebackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private TransactionImageRepository transactionImageRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Account account;
    private Category category;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1)
                .email("test@example.com")
                .fullName("Test User")
                .build();

        account = Account.builder()
                .accountId(10)
                .accountName("Ví chính")
                .balance(new BigDecimal("100000"))
                .user(user)
                .build();

        category = Category.builder()
                .categoryId(5)
                .categoryName("food")
                .categoryType("expense")
                .build();

        transaction = Transaction.builder()
                .transactionId(100)
                .user(user)
                .account(account)
                .category(category)
                .title("Ăn trưa")
                .amount(new BigDecimal("25000"))
                .transactionType("expense")
                .transactionDate(LocalDate.now())
                .status("confirmed")
                .build();
    }

    @Test
    void testCreateTransaction_Success_WithAccountId() {
        TransactionDTO dto = TransactionDTO.builder()
                .userId(1)
                .accountId(10)
                .categoryId(5)
                .title("Ăn trưa")
                .amount(new BigDecimal("25000"))
                .transactionType("expense")
                .transactionDate(LocalDate.now())
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(accountRepository.findById(10)).thenReturn(Optional.of(account));
        when(categoryRepository.findById(5)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setTransactionId(100);
            return tx;
        });

        TransactionDTO result = transactionService.createTransaction(dto);

        assertNotNull(result);
        assertEquals(100, result.getTransactionId());
        assertEquals("Ăn trưa", result.getTitle());
        verify(accountService, times(1)).updateBalance(10, new BigDecimal("25000"), "expense");
    }

    @Test
    void testCreateTransaction_Success_AutoAssignDefaultWallet() {
        TransactionDTO dto = TransactionDTO.builder()
                .userId(1)
                .accountId(null)
                .categoryId(5)
                .title("Ăn sáng")
                .amount(new BigDecimal("15000"))
                .transactionType("expense")
                .transactionDate(LocalDate.now())
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(5)).thenReturn(Optional.of(category));
        when(accountService.getOrCreateDefaultAccount(1)).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setTransactionId(101);
            return tx;
        });

        TransactionDTO result = transactionService.createTransaction(dto);

        assertNotNull(result);
        assertEquals(101, result.getTransactionId());
        assertEquals(10, result.getAccountId());
        verify(accountService, times(1)).updateBalance(10, new BigDecimal("15000"), "expense");
    }

    @Test
    void testCreateTransaction_FutureDate_ThrowsException() {
        TransactionDTO dto = TransactionDTO.builder()
                .userId(1)
                .accountId(10)
                .title("Ăn trưa")
                .amount(new BigDecimal("25000"))
                .transactionType("expense")
                .transactionDate(LocalDate.now().plusDays(1))
                .build();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.createTransaction(dto);
        });

        assertEquals("Không thể tạo giao dịch cho ngày trong tương lai", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testUpdateTransaction_Success() {
        TransactionDTO dto = TransactionDTO.builder()
                .title("Ăn trưa sang chảnh")
                .amount(new BigDecimal("50000"))
                .transactionType("expense")
                .transactionDate(LocalDate.now())
                .build();

        when(transactionRepository.findById(100)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionDTO result = transactionService.updateTransaction(100, dto);

        assertNotNull(result);
        verify(accountService, times(1)).updateBalance(10, new BigDecimal("25000"), "income");
        verify(accountService, times(1)).updateBalance(10, new BigDecimal("50000"), "expense");
    }

    @Test
    void testDeleteTransaction_Success() {
        when(transactionRepository.findById(100)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(100);

        verify(accountService, times(1)).updateBalance(10, new BigDecimal("25000"), "income");
        verify(transactionRepository, times(1)).delete(transaction);
    }
}
