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
public class AiScanResponse {
    private Integer aiScanLogId;
    private String detectedMerchant;
    private BigDecimal detectedAmount;
    private LocalDate detectedDate;
    private Integer suggestedCategoryId;
    private String suggestedCategoryName;
    private BigDecimal confidenceScore;
}
