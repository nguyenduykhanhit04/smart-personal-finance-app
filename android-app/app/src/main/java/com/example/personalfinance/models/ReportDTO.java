package com.example.personalfinance.models;

import java.util.List;

public class ReportDTO {
    private Integer userId;
    private String reportType;
    private String period;
    private double totalIncome;
    private double totalExpense;
    private double netAmount;
    private List<CategoryBreakdown> categoryBreakdowns;
    private List<DailyBreakdown> dailyBreakdowns;

    public ReportDTO() {}

    public ReportDTO(Integer userId, String reportType, String period, double totalIncome, double totalExpense, double netAmount, List<CategoryBreakdown> categoryBreakdowns, List<DailyBreakdown> dailyBreakdowns) {
        this.userId = userId;
        this.reportType = reportType;
        this.period = period;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netAmount = netAmount;
        this.categoryBreakdowns = categoryBreakdowns;
        this.dailyBreakdowns = dailyBreakdowns;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
    }

    public double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(double netAmount) {
        this.netAmount = netAmount;
    }

    public List<CategoryBreakdown> getCategoryBreakdowns() {
        return categoryBreakdowns;
    }

    public void setCategoryBreakdowns(List<CategoryBreakdown> categoryBreakdowns) {
        this.categoryBreakdowns = categoryBreakdowns;
    }

    public List<DailyBreakdown> getDailyBreakdowns() {
        return dailyBreakdowns;
    }

    public void setDailyBreakdowns(List<DailyBreakdown> dailyBreakdowns) {
        this.dailyBreakdowns = dailyBreakdowns;
    }

    public static class CategoryBreakdown {
        private Integer categoryId;
        private String categoryName;
        private String categoryType;
        private double totalAmount;
        private Double percentage;

        public CategoryBreakdown() {}

        public CategoryBreakdown(Integer categoryId, String categoryName, String categoryType, double totalAmount, Double percentage) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.categoryType = categoryType;
            this.totalAmount = totalAmount;
            this.percentage = percentage;
        }

        public Integer getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Integer categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getCategoryType() {
            return categoryType;
        }

        public void setCategoryType(String categoryType) {
            this.categoryType = categoryType;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
    }

    public static class DailyBreakdown {
        private String date;
        private double income;
        private double expense;

        public DailyBreakdown() {}

        public DailyBreakdown(String date, double income, double expense) {
            this.date = date;
            this.income = income;
            this.expense = expense;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public double getIncome() {
            return income;
        }

        public void setIncome(double income) {
            this.income = income;
        }

        public double getExpense() {
            return expense;
        }

        public void setExpense(double expense) {
            this.expense = expense;
        }
    }
}
