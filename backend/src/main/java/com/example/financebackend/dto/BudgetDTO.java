package com.example.financebackend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetDTO {
    private Integer budgetId;
    private Integer userId;
    private Integer categoryId;
    private String categoryName;
    private String budgetName;
    private BigDecimal amountLimit;
    private BigDecimal dailyAmountLimit;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Double percentUsed;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean exceeded;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
