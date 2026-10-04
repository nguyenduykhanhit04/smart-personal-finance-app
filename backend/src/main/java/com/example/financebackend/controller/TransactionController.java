package com.example.financebackend.controller;

import com.example.financebackend.dto.TransactionDTO;
import com.example.financebackend.dto.response.ApiResponse;
import com.example.financebackend.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactions(
            @RequestParam Integer userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TransactionDTO> transactions = (startDate != null && endDate != null)
                ? transactionService.getTransactionsByDateRange(userId, startDate, endDate)
                : transactionService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransactionById(@PathVariable Integer id) {
        TransactionDTO transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionDTO>> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        TransactionDTO created = transactionService.createTransaction(transactionDTO);
        return ResponseEntity.ok(ApiResponse.success("Transaction created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> updateTransaction(@PathVariable Integer id, @Valid @RequestBody TransactionDTO transactionDTO) {
        TransactionDTO updated = transactionService.updateTransaction(id, transactionDTO);
        return ResponseEntity.ok(ApiResponse.success("Transaction updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable Integer id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully", null));
    }
}
