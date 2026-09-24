package com.example.financebackend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private Integer transactionId;
    private Integer userId;
    private Integer accountId;
    private String accountName;
    private Integer categoryId;
    private String categoryName;
    private String title;
    private BigDecimal amount;
    private String transactionType;
    private LocalDate transactionDate;
    private String note;
    private String status;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
