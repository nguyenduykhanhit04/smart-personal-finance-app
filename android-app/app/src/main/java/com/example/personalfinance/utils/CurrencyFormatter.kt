package com.example.personalfinance.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun formatVND(amount: Double): String {
        return try {
            val vietnam = Locale("vi", "VN")
            val format = NumberFormat.getCurrencyInstance(vietnam)
            format.format(amount)
        } catch (e: Exception) {
            String.format(Locale.getDefault(), "%,.0f đ", amount)
        }
    }
}
