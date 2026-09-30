package com.example.personalfinance.models.domain

data class Category(
    var categoryId: Int? = null,
    var userId: Int? = null,
    var categoryName: String? = null,
    var categoryType: String? = null,
    var icon: String? = null,
    var color: String? = null,
    var isDefault: Boolean? = null,
    var createdAt: String? = null
)
