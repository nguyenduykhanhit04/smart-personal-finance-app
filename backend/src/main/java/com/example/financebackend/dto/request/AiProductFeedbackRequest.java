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
public class AiProductFeedbackRequest {
    @NotNull(message = "AI product log ID is required")
    @Positive(message = "AI product log ID must be positive")
    private Integer aiProductLogId;
    private Integer transactionId;
}
