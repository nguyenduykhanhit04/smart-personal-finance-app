package com.example.personalfinance.models

data class OcrRequest(
    var userId: Int? = null,
    var rawOcrText: String? = null
)
