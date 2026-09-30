package com.example.personalfinance.models.domain

import java.io.Serializable

data class Budget(
    var budgetId: Int? = null,
    var userId: Int? = null,
    var categoryId: Int? = null,
    var categoryName: String? = null,
    var budgetName: String? = null,
    var amountLimit: Double = 0.0,
    var dailyAmountLimit: Double = 0.0,
    var spentAmount: Double = 0.0,
    var remainingAmount: Double = 0.0,
    var percentUsed: Double = 0.0,
    var startDate: String? = null,
    var endDate: String? = null,
    var exceeded: Boolean? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null
) : Serializable
