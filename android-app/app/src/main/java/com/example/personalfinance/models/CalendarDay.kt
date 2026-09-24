package com.example.personalfinance.models

data class CalendarDay(
    var dayNumber: Int = 0,
    var isPlaceholder: Boolean = false,
    var transactions: MutableList<Transaction> = mutableListOf()
) {
    fun addTransaction(transaction: Transaction) {
        transactions.add(transaction)
    }
}
