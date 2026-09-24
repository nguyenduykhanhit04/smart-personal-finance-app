package com.example.personalfinance.models;

public class AiProductResult {
    private Integer aiProductLogId;
    private Integer suggestedCategoryId;
    private String suggestedCategoryName;

    public Integer getAiProductLogId() {
        return aiProductLogId;
    }

    public void setAiProductLogId(Integer aiProductLogId) {
        this.aiProductLogId = aiProductLogId;
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
}
