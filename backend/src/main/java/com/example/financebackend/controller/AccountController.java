package com.example.financebackend.controller;

import com.example.financebackend.dto.AccountDTO;
import com.example.financebackend.dto.response.ApiResponse;
import com.example.financebackend.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getAccountsByUserId(@RequestParam Integer userId) {
        List<AccountDTO> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDTO>> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
        AccountDTO created = accountService.createAccount(accountDTO);
        return ResponseEntity.ok(ApiResponse.success("Account created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDTO>> updateAccount(@PathVariable Integer id, @Valid @RequestBody AccountDTO accountDTO) {
        AccountDTO updated = accountService.updateAccount(id, accountDTO);
        return ResponseEntity.ok(ApiResponse.success("Account updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Integer id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }
}
