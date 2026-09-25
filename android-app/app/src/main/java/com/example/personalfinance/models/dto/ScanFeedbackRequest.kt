package com.example.personalfinance.models.dto

data class ScanFeedbackRequest(
    var aiScanLogId: Int? = null,
    var transactionId: Int? = null,
    var actualCategoryId: Int? = null
)
