package com.example.financebackend.dto.request;

import com.example.financebackend.dto.YoloDetectionDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiProductClassifyRequest {
    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotEmpty(message = "Detections list cannot be empty")
    private List<YoloDetectionDTO> detections;
}
