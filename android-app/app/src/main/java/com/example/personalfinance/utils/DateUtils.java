package com.example.personalfinance.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    
    private static final String DEFAULT_FORMAT = "yyyy-MM-dd";
    private static final String DISPLAY_FORMAT = "dd/MM/yyyy";
    private static final Locale API_LOCALE = Locale.US;
    private static final Locale DISPLAY_LOCALE = Locale.getDefault();
    private static final ThreadLocal<SimpleDateFormat> API_FORMATTER =
            ThreadLocal.withInitial(() -> new SimpleDateFormat(DEFAULT_FORMAT, API_LOCALE));
    private static final ThreadLocal<SimpleDateFormat> DISPLAY_FORMATTER =
            ThreadLocal.withInitial(() -> new SimpleDateFormat(DISPLAY_FORMAT, DISPLAY_LOCALE));
    private static final ThreadLocal<SimpleDateFormat> VIETNAMESE_DAY_TITLE_FORMATTER =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("EEEE, 'ngày' d 'thg' M, yyyy", new Locale("vi", "VN")));
    private static final ThreadLocal<SimpleDateFormat> MONTH_YEAR_FORMATTER =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("MM/yyyy", DISPLAY_LOCALE));
    private static final ThreadLocal<SimpleDateFormat> MONTH_TITLE_FORMATTER =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("'Tháng' MM/yyyy", DISPLAY_LOCALE));

    public static String getCurrentDateString() {
        return formatApiDate(new Date());
    }

    public static String formatApiDate(Date date) {
        return API_FORMATTER.get().format(date);
    }

    public static String formatVietnameseDayTitle(Date date) {
        String title = VIETNAMESE_DAY_TITLE_FORMATTER.get().format(date);
        if (title.isEmpty()) {
            return title;
        }
        return title.substring(0, 1).toUpperCase(new Locale("vi", "VN")) + title.substring(1);
    }

    public static String formatDisplayDate(Date date) {
        return DISPLAY_FORMATTER.get().format(date);
    }

    public static String formatMonthYear(Date date) {
        return MONTH_YEAR_FORMATTER.get().format(date);
    }

    public static String formatMonthTitle(Date date) {
        return MONTH_TITLE_FORMATTER.get().format(date);
    }

    public static Date parseApiDate(String serverDate) {
        if (serverDate == null || serverDate.isBlank()) return null;
        try {
            return API_FORMATTER.get().parse(normalizeDateOnly(serverDate));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String normalizeDateOnly(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.contains("T") ? value.split("T")[0] : value;
    }

    public static String formatDateForDisplay(String serverDate) {
        if (serverDate == null || serverDate.isBlank()) return "";
        try {
            Date parsed = parseApiDate(serverDate);
            if (parsed != null) {
                return DISPLAY_FORMATTER.get().format(parsed);
            }
        } catch (Exception ignored) {}
        return serverDate;
    }

}
