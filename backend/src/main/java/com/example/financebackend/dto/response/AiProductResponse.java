package com.example.financebackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiProductResponse {
    private Integer aiProductLogId;
    private Integer suggestedCategoryId;
    private String suggestedCategoryName;
}
