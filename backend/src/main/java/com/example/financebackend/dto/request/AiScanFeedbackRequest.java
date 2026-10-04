package com.example.financebackend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiScanFeedbackRequest {
    @NotNull(message = "AI scan log ID is required")
    @Positive(message = "AI scan log ID must be positive")
    private Integer aiScanLogId;

    private Integer transactionId;
    private Integer actualCategoryId;
}
