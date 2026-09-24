package com.example.financebackend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringTransactionDTO {
    private Integer recurringId;
    private Integer userId;
    private Integer accountId;
    private Integer categoryId;
    private String categoryName;
    private String categoryColor;
    private String title;
    private BigDecimal amount;
    private String transactionType; // INCOME, EXPENSE
    private String repeatType; // DAILY, WEEKLY, MONTHLY, YEARLY
    private Integer repeatInterval;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextRunDate;
    private String note;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
