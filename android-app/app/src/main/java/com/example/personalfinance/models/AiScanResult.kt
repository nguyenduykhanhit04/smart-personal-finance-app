package com.example.personalfinance.models

data class AiScanResult(
    var aiScanLogId: Int? = null,
    var detectedMerchant: String? = null,
    var detectedAmount: Double = 0.0,
    var detectedDate: String? = null,
    var suggestedCategoryId: Int? = null,
    var suggestedCategoryName: String? = null,
    var confidenceScore: Double = 0.0
)
