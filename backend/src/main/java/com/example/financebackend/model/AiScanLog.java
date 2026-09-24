package com.example.financebackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_scan_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiScanLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_scan_log_id")
    private Integer aiScanLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(name = "raw_ocr_text", columnDefinition = "TEXT")
    private String rawOcrText;

    @Column(name = "detected_merchant", length = 150)
    private String detectedMerchant;

    @Column(name = "detected_amount", precision = 15, scale = 2)
    private BigDecimal detectedAmount;

    @Column(name = "detected_date")
    private LocalDate detectedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggested_category_id")
    private Category suggestedCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actual_category_id")
    private Category actualCategory;

    @Column(name = "was_corrected")
    private Boolean wasCorrected;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (wasCorrected == null) wasCorrected = false;
    }
}
