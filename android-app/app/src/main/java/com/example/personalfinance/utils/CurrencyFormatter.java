package com.example.personalfinance.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {
    public static String formatVND(double amount) {
        try {
            Locale vietnam = new Locale("vi", "VN");
            NumberFormat format = NumberFormat.getCurrencyInstance(vietnam);
            return format.format(amount);
        } catch (Exception e) {
            return String.format(Locale.getDefault(), "%,.0f ₫", amount);
        }
    }
}
