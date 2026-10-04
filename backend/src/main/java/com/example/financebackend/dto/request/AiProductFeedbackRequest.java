package com.example.financebackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiProductFeedbackRequest {
    private Integer aiProductLogId;
    private Integer transactionId;
}
