package com.example.personalfinance.models

import java.io.Serializable

data class Account(
    var accountId: Int? = null,
    var userId: Int? = null,
    var accountName: String? = null,
    var accountType: String? = null,
    var balance: Double = 0.0,
    var currency: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null
) : Serializable
