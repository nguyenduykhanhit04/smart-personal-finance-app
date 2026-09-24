package com.example.financebackend.service;

import com.example.financebackend.dto.AccountDTO;
import com.example.financebackend.model.Account;
import com.example.financebackend.model.User;
import com.example.financebackend.repository.AccountRepository;
import com.example.financebackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public List<AccountDTO> getAccountsByUserId(Integer userId) {
        return accountRepository.findByUserUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountDTO createAccount(AccountDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

        Account account = Account.builder()
                .user(user)
                .accountName(dto.getAccountName())
                .accountType(dto.getAccountType())
                .balance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO)
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "VND")
                .build();

        account = accountRepository.save(account);
        return toDTO(account);
    }

    @Transactional
    public AccountDTO updateAccount(Integer accountId, AccountDTO dto) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        if (dto.getAccountName() != null) account.setAccountName(dto.getAccountName());
        if (dto.getAccountType() != null) account.setAccountType(dto.getAccountType());
        if (dto.getCurrency() != null) account.setCurrency(dto.getCurrency());
        if (dto.getBalance() != null) account.setBalance(dto.getBalance());

        account = accountRepository.save(account);
        return toDTO(account);
    }

    @Transactional
    public void deleteAccount(Integer accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new RuntimeException("Account not found with id: " + accountId);
        }
        accountRepository.deleteById(accountId);
    }

    @Transactional
    public void updateBalance(Integer accountId, BigDecimal amount, String transactionType) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        if ("income".equalsIgnoreCase(transactionType)) {
            account.setBalance(account.getBalance().add(amount));
        } else if ("expense".equalsIgnoreCase(transactionType)) {
            account.setBalance(account.getBalance().subtract(amount));
        }

        accountRepository.save(account);
    }

    @Transactional
    public Account getOrCreateDefaultAccount(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Account> accounts = accountRepository.findByUserUserId(userId);
        java.util.Optional<Account> defaultAccountOpt = accounts.stream()
                .filter(a -> "Ví chính".equals(a.getAccountName()))
                .findFirst();

        if (defaultAccountOpt.isPresent()) {
            return defaultAccountOpt.get();
        }

        // If no "Ví chính" exists, create it
        Account defaultAccount = Account.builder()
                .user(user)
                .accountName("Ví chính")
                .accountType("cash")
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .build();

        return accountRepository.save(defaultAccount);
    }

    public AccountDTO toDTO(Account account) {
        return AccountDTO.builder()
                .accountId(account.getAccountId())
                .userId(account.getUser().getUserId())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
