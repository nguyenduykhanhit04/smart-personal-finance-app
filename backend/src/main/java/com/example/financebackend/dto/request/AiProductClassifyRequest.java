package com.example.financebackend.dto.request;

import com.example.financebackend.dto.YoloDetectionDTO;
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
    private Integer userId;
    private List<YoloDetectionDTO> detections;
}
