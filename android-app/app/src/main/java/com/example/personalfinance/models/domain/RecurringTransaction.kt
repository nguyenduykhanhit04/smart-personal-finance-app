package com.example.personalfinance.models.domain

import java.io.Serializable

data class RecurringTransaction(
    var recurringId: Int? = null,
    var userId: Int? = null,
    var accountId: Int? = null,
    var categoryId: Int? = null,
    var categoryName: String? = null,        // for convenience on UI
    var categoryColor: String? = null,       // for convenience on UI
    var title: String? = null,
    var amount: Double? = null,
    var transactionType: String? = null,     // "INCOME" or "EXPENSE"
    var repeatType: String? = null,          // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    var repeatInterval: Int? = null,
    var startDate: String? = null,           // "yyyy-MM-dd"
    var endDate: String? = null,             // "yyyy-MM-dd"
    var nextRunDate: String? = null,         // "yyyy-MM-dd"
    var note: String? = null,
    var isActive: Boolean? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null
) : Serializable
