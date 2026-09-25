package com.example.personalfinance.models.dto

data class ReportDTO(
    var userId: Int? = null,
    var reportType: String? = null,
    var period: String? = null,
    var totalIncome: Double = 0.0,
    var totalExpense: Double = 0.0,
    var netAmount: Double = 0.0,
    var categoryBreakdowns: List<CategoryBreakdown>? = null,
    var dailyBreakdowns: List<DailyBreakdown>? = null
) {
    data class CategoryBreakdown(
        var categoryId: Int? = null,
        var categoryName: String? = null,
        var categoryType: String? = null,
        var totalAmount: Double = 0.0,
        var percentage: Double? = null
    )

    data class DailyBreakdown(
        var date: String? = null,
        var income: Double = 0.0,
        var expense: Double = 0.0
    )
}
