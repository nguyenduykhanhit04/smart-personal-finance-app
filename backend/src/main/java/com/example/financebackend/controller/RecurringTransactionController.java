package com.example.financebackend.controller;

import com.example.financebackend.dto.ApiResponse;
import com.example.financebackend.dto.RecurringTransactionDTO;
import com.example.financebackend.service.RecurringTransactionService;
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
        try {
            List<RecurringTransactionDTO> list = recurringTransactionService.getRecurringByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringTransactionDTO>> createRecurringTransaction(@RequestBody RecurringTransactionDTO dto) {
        try {
            RecurringTransactionDTO created = recurringTransactionService.createRecurring(dto);
            return ResponseEntity.ok(ApiResponse.success("Recurring transaction created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringTransactionDTO>> updateRecurringTransaction(@PathVariable Integer id, @RequestBody RecurringTransactionDTO dto) {
        try {
            RecurringTransactionDTO updated = recurringTransactionService.updateRecurring(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Recurring transaction updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecurringTransaction(@PathVariable Integer id) {
        try {
            recurringTransactionService.deleteRecurring(id);
            return ResponseEntity.ok(ApiResponse.success("Recurring transaction deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Force manual trigger endpoint for convenience/testing recurring auto-creation
    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<Void>> triggerRecurringTransactions() {
        try {
            recurringTransactionService.processRecurringTransactions();
            return ResponseEntity.ok(ApiResponse.success("Recurring transactions processed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
