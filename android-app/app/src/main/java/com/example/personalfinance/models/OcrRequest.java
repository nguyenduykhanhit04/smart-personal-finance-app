package com.example.personalfinance.models;

public class OcrRequest {
    private Integer userId;
    private String rawOcrText;

    public OcrRequest() {}

    public OcrRequest(Integer userId, String rawOcrText) {
        this.userId = userId;
        this.rawOcrText = rawOcrText;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getRawOcrText() {
        return rawOcrText;
    }

    public void setRawOcrText(String rawOcrText) {
        this.rawOcrText = rawOcrText;
    }
}
