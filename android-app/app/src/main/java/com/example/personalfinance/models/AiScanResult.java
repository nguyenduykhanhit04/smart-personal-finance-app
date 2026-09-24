package com.example.personalfinance.models;

public class AiScanResult {
    private Integer aiScanLogId;
    private String detectedMerchant;
    private double detectedAmount;
    private String detectedDate;
    private Integer suggestedCategoryId;
    private String suggestedCategoryName;
    private double confidenceScore;

    public AiScanResult() {}

    public AiScanResult(Integer aiScanLogId, String detectedMerchant, double detectedAmount, String detectedDate, Integer suggestedCategoryId, String suggestedCategoryName, double confidenceScore) {
        this.aiScanLogId = aiScanLogId;
        this.detectedMerchant = detectedMerchant;
        this.detectedAmount = detectedAmount;
        this.detectedDate = detectedDate;
        this.suggestedCategoryId = suggestedCategoryId;
        this.suggestedCategoryName = suggestedCategoryName;
        this.confidenceScore = confidenceScore;
    }

    public Integer getAiScanLogId() {
        return aiScanLogId;
    }

    public void setAiScanLogId(Integer aiScanLogId) {
        this.aiScanLogId = aiScanLogId;
    }

    public String getDetectedMerchant() {
        return detectedMerchant;
    }

    public void setDetectedMerchant(String detectedMerchant) {
        this.detectedMerchant = detectedMerchant;
    }

    public double getDetectedAmount() {
        return detectedAmount;
    }

    public void setDetectedAmount(double detectedAmount) {
        this.detectedAmount = detectedAmount;
    }

    public String getDetectedDate() {
        return detectedDate;
    }

    public void setDetectedDate(String detectedDate) {
        this.detectedDate = detectedDate;
    }

    public Integer getSuggestedCategoryId() {
        return suggestedCategoryId;
    }

    public void setSuggestedCategoryId(Integer suggestedCategoryId) {
        this.suggestedCategoryId = suggestedCategoryId;
    }

    public String getSuggestedCategoryName() {
        return suggestedCategoryName;
    }

    public void setSuggestedCategoryName(String suggestedCategoryName) {
        this.suggestedCategoryName = suggestedCategoryName;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}
