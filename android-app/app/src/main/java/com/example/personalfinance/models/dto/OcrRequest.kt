package com.example.personalfinance.models.dto

data class OcrRequest(
    var userId: Int? = null,
    var rawOcrText: String? = null
)
