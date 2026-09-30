package com.example.personalfinance.models.domain

data class User(
    var userId: Int? = null,
    var firebaseUid: String? = null,
    var fullName: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var avatarUrl: String? = null,
    var authProvider: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null
)
