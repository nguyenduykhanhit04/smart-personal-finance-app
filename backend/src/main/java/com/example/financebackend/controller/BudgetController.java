package com.example.financebackend.controller;

import com.example.financebackend.dto.BudgetDTO;
import com.example.financebackend.dto.response.ApiResponse;
import com.example.financebackend.service.BudgetService;
import jakarta.validation.Valid;
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
        List<BudgetDTO> budgets = (activeDate != null)
                ? budgetService.getActiveBudgets(userId, activeDate)
                : budgetService.getBudgetsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(budgets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetDTO>> getBudgetById(@PathVariable Integer id) {
        BudgetDTO budget = budgetService.getBudgetById(id);
        return ResponseEntity.ok(ApiResponse.success(budget));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetDTO>> createBudget(@Valid @RequestBody BudgetDTO budgetDTO) {
        BudgetDTO created = budgetService.createBudget(budgetDTO);
        return ResponseEntity.ok(ApiResponse.success("Budget created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetDTO>> updateBudget(@PathVariable Integer id, @Valid @RequestBody BudgetDTO budgetDTO) {
        BudgetDTO updated = budgetService.updateBudget(id, budgetDTO);
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable Integer id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully", null));
    }
}
