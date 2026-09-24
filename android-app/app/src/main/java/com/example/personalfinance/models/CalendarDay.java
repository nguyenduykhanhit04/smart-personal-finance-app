package com.example.personalfinance.models;

import java.util.ArrayList;
import java.util.List;

public class CalendarDay {
    private int dayNumber;
    private boolean isPlaceholder;
    private List<Transaction> transactions;

    public CalendarDay(int dayNumber, boolean isPlaceholder) {
        this.dayNumber = dayNumber;
        this.isPlaceholder = isPlaceholder;
        this.transactions = new ArrayList<>();
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public boolean isPlaceholder() {
        return isPlaceholder;
    }

    public void setPlaceholder(boolean placeholder) {
        isPlaceholder = placeholder;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void addTransaction(Transaction transaction) {
        if (this.transactions == null) {
            this.transactions = new ArrayList<>();
        }
        this.transactions.add(transaction);
    }
}
