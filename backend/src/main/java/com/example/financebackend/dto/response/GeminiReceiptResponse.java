package com.example.financebackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiReceiptResponse {
    private String merchant;
    private BigDecimal amount;
    private LocalDate date;
    private String category;
}
