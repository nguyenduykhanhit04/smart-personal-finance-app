package com.example.personalfinance.models.dto

data class LoginRequest(
    var firebaseUid: String? = null,
    var email: String? = null,
    var fullName: String? = null,
    var avatarUrl: String? = null
)
