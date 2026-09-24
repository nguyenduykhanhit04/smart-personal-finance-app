package com.example.financebackend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDTO {
    private Integer userId;
    private String reportType;
    private String period;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netAmount;
    private List<CategoryBreakdown> categoryBreakdowns;
    private List<DailyBreakdown> dailyBreakdowns;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryBreakdown {
        private Integer categoryId;
        private String categoryName;
        private String categoryType;
        private BigDecimal totalAmount;
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyBreakdown {
        private String date;
        private BigDecimal income;
        private BigDecimal expense;
    }
}
