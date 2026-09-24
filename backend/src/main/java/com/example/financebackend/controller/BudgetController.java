package com.example.financebackend.controller;

import com.example.financebackend.dto.ApiResponse;
import com.example.financebackend.dto.BudgetDTO;
import com.example.financebackend.service.BudgetService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetDTO>>> getBudgets(
            @RequestParam Integer userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate activeDate) {
        try {
            List<BudgetDTO> budgets;
            if (activeDate != null) {
                budgets = budgetService.getActiveBudgets(userId, activeDate);
            } else {
                budgets = budgetService.getBudgetsByUserId(userId);
            }
            return ResponseEntity.ok(ApiResponse.success(budgets));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetDTO>> getBudgetById(@PathVariable Integer id) {
        try {
            BudgetDTO budget = budgetService.getBudgetById(id);
            return ResponseEntity.ok(ApiResponse.success(budget));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetDTO>> createBudget(@RequestBody BudgetDTO budgetDTO) {
        try {
            BudgetDTO created = budgetService.createBudget(budgetDTO);
            return ResponseEntity.ok(ApiResponse.success("Budget created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetDTO>> updateBudget(@PathVariable Integer id, @RequestBody BudgetDTO budgetDTO) {
        try {
            BudgetDTO updated = budgetService.updateBudget(id, budgetDTO);
            return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable Integer id) {
        try {
            budgetService.deleteBudget(id);
            return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
