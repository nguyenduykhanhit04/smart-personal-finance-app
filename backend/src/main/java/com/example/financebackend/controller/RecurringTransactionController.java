package com.example.financebackend.controller;

import com.example.financebackend.dto.RecurringTransactionDTO;
import com.example.financebackend.dto.response.ApiResponse;
import com.example.financebackend.service.RecurringTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-transactions")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    public RecurringTransactionController(RecurringTransactionService recurringTransactionService) {
        this.recurringTransactionService = recurringTransactionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecurringTransactionDTO>>> getRecurringTransactions(@RequestParam Integer userId) {
        List<RecurringTransactionDTO> list = recurringTransactionService.getRecurringByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringTransactionDTO>> createRecurringTransaction(@Valid @RequestBody RecurringTransactionDTO dto) {
        RecurringTransactionDTO created = recurringTransactionService.createRecurring(dto);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringTransactionDTO>> updateRecurringTransaction(@PathVariable Integer id, @Valid @RequestBody RecurringTransactionDTO dto) {
        RecurringTransactionDTO updated = recurringTransactionService.updateRecurring(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecurringTransaction(@PathVariable Integer id) {
        recurringTransactionService.deleteRecurring(id);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction deleted successfully", null));
    }

    // Force manual trigger endpoint for convenience/testing recurring auto-creation
    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<Void>> triggerRecurringTransactions() {
        recurringTransactionService.processRecurringTransactions();
        return ResponseEntity.ok(ApiResponse.success("Recurring transactions processed successfully", null));
    }
}
