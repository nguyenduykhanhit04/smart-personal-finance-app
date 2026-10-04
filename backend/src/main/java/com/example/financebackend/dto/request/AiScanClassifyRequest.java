package com.example.financebackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiScanClassifyRequest {
    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotBlank(message = "OCR text cannot be blank")
    private String rawOcrText;
}
