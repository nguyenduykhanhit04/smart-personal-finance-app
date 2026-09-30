package com.example.personalfinance.models.domain

import java.io.Serializable

data class Transaction(
    var transactionId: Int? = null,
    var userId: Int? = null,
    var accountId: Int? = null,
    var accountName: String? = null,
    var categoryId: Int? = null,
    var categoryName: String? = null,
    var title: String? = null,
    var amount: Double = 0.0,
    var transactionType: String? = null,
    var transactionDate: String? = null,
    var note: String? = null,
    var status: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var imageUrl: String? = null
) : Serializable
