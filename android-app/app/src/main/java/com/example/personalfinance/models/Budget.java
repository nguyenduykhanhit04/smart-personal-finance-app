package com.example.personalfinance.models;

public class Budget implements java.io.Serializable {
    private Integer budgetId;
    private Integer userId;
    private Integer categoryId;
    private String categoryName;
    private String budgetName;
    private double amountLimit;
    private double dailyAmountLimit;
    private double spentAmount;
    private double remainingAmount;
    private double percentUsed;
    private String startDate;
    private String endDate;
    private Boolean exceeded;
    private String createdAt;
    private String updatedAt;

    public Budget() {}

    public Budget(Integer budgetId, Integer userId, Integer categoryId, String categoryName, String budgetName, double amountLimit, double dailyAmountLimit, double spentAmount, double remainingAmount, double percentUsed, String startDate, String endDate, Boolean exceeded, String createdAt, String updatedAt) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.budgetName = budgetName;
        this.amountLimit = amountLimit;
        this.dailyAmountLimit = dailyAmountLimit;
        this.spentAmount = spentAmount;
        this.remainingAmount = remainingAmount;
        this.percentUsed = percentUsed;
        this.startDate = startDate;
        this.endDate = endDate;
        this.exceeded = exceeded;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Integer budgetId) {
        this.budgetId = budgetId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public String getBudgetName() {
        return budgetName;
    }

    public void setBudgetName(String budgetName) {
        this.budgetName = budgetName;
    }

    public double getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(double amountLimit) {
        this.amountLimit = amountLimit;
    }

    public double getDailyAmountLimit() {
        return dailyAmountLimit;
    }

    public void setDailyAmountLimit(double dailyAmountLimit) {
        this.dailyAmountLimit = dailyAmountLimit;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public double getPercentUsed() {
        return percentUsed;
    }

    public void setPercentUsed(double percentUsed) {
        this.percentUsed = percentUsed;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Boolean getExceeded() {
        return exceeded;
    }

    public void setExceeded(Boolean exceeded) {
        this.exceeded = exceeded;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
