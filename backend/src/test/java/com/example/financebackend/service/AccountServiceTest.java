package com.example.financebackend.service;

import com.example.financebackend.dto.AccountDTO;
import com.example.financebackend.model.Account;
import com.example.financebackend.model.User;
import com.example.financebackend.repository.AccountRepository;
import com.example.financebackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1)
                .email("test@example.com")
                .fullName("Test User")
                .firebaseUid("firebase_uid_123")
                .build();

        account = Account.builder()
                .accountId(10)
                .user(user)
                .accountName("Ví chính")
                .accountType("cash")
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .build();
    }

    @Test
    void testGetOrCreateDefaultAccount_AlreadyExists() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserUserId(1)).thenReturn(Collections.singletonList(account));

        Account result = accountService.getOrCreateDefaultAccount(1);

        assertNotNull(result);
        assertEquals("Ví chính", result.getAccountName());
        assertEquals(10, result.getAccountId());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testGetOrCreateDefaultAccount_NotExists_ShouldCreate() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserUserId(1)).thenReturn(new ArrayList<>());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            saved.setAccountId(99); // simulate DB generated ID
            return saved;
        });

        Account result = accountService.getOrCreateDefaultAccount(1);

        assertNotNull(result);
        assertEquals("Ví chính", result.getAccountName());
        assertEquals(99, result.getAccountId());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testUpdateBalance_Income() {
        when(accountRepository.findById(10)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.updateBalance(10, new BigDecimal("50000"), "income");

        assertEquals(new BigDecimal("50000"), account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testUpdateBalance_Expense() {
        account.setBalance(new BigDecimal("100000"));
        when(accountRepository.findById(10)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.updateBalance(10, new BigDecimal("30000"), "expense");

        assertEquals(new BigDecimal("70000"), account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }
}
