package com.example.financebackend.service.ocr;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class ReceiptData {
    String rawText;
    String normalizedText;
    String merchant;
    BigDecimal amount;
    LocalDate date;
    Integer hour;
}
