package com.example.personalfinance.models

data class ApiResponse<T>(
    var success: Boolean = false,
    var message: String? = null,
    var data: T? = null
)
