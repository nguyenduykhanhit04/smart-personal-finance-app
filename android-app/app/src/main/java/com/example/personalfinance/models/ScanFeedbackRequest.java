package com.example.personalfinance.models;

public class ScanFeedbackRequest {
    private Integer aiScanLogId;
    private Integer transactionId;
    private Integer actualCategoryId;

    public ScanFeedbackRequest() {}

    public ScanFeedbackRequest(Integer aiScanLogId, Integer transactionId, Integer actualCategoryId) {
        this.aiScanLogId = aiScanLogId;
        this.transactionId = transactionId;
        this.actualCategoryId = actualCategoryId;
    }

    public Integer getAiScanLogId() {
        return aiScanLogId;
    }

    public void setAiScanLogId(Integer aiScanLogId) {
        this.aiScanLogId = aiScanLogId;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getActualCategoryId() {
        return actualCategoryId;
    }

    public void setActualCategoryId(Integer actualCategoryId) {
        this.actualCategoryId = actualCategoryId;
    }
}
