package com.example.personalfinance.models;

public class ProductFeedbackRequest {
    private Integer aiProductLogId;
    private Integer transactionId;

    public ProductFeedbackRequest(Integer aiProductLogId, Integer transactionId) {
        this.aiProductLogId = aiProductLogId;
        this.transactionId = transactionId;
    }

    public Integer getAiProductLogId() {
        return aiProductLogId;
    }

    public void setAiProductLogId(Integer aiProductLogId) {
        this.aiProductLogId = aiProductLogId;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }
}
