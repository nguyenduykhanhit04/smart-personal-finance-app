package com.example.personalfinance.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val DEFAULT_FORMAT = "yyyy-MM-dd"
    private const val DISPLAY_FORMAT = "dd/MM/yyyy"
    private val API_LOCALE = Locale.US
    private val DISPLAY_LOCALE = Locale.getDefault()

    private val apiFormatter = ThreadLocal.withInitial { SimpleDateFormat(DEFAULT_FORMAT, API_LOCALE) }
    private val displayFormatter = ThreadLocal.withInitial { SimpleDateFormat(DISPLAY_FORMAT, DISPLAY_LOCALE) }
    private val vietnameseDayTitleFormatter = ThreadLocal.withInitial {
        SimpleDateFormat("EEEE, 'ngày' d 'thg' M, yyyy", Locale("vi", "VN"))
    }
    private val monthYearFormatter = ThreadLocal.withInitial { SimpleDateFormat("MM/yyyy", DISPLAY_LOCALE) }
    private val monthTitleFormatter = ThreadLocal.withInitial { SimpleDateFormat("'Tháng' MM/yyyy", DISPLAY_LOCALE) }

    fun getCurrentDateString(): String = formatApiDate(Date())

    fun formatApiDate(date: Date): String = apiFormatter.get()!!.format(date)

    fun formatVietnameseDayTitle(date: Date): String {
        val title = vietnameseDayTitleFormatter.get()!!.format(date)
        return if (title.isEmpty()) title
        else title.substring(0, 1).uppercase(Locale("vi", "VN")) + title.substring(1)
    }

    fun formatDisplayDate(date: Date): String = displayFormatter.get()!!.format(date)

    fun formatMonthYear(date: Date): String = monthYearFormatter.get()!!.format(date)

    fun formatMonthTitle(date: Date): String = monthTitleFormatter.get()!!.format(date)

    fun parseApiDate(serverDate: String?): Date? {
        if (serverDate.isNullOrBlank()) return null
        return try {
            apiFormatter.get()!!.parse(normalizeDateOnly(serverDate) ?: return null)
        } catch (ignored: Exception) {
            null
        }
    }

    fun normalizeDateOnly(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return if (value.contains("T")) value.split("T")[0] else value
    }

    fun formatDateForDisplay(serverDate: String?): String {
        if (serverDate.isNullOrBlank()) return ""
        return try {
            parseApiDate(serverDate)?.let { displayFormatter.get()!!.format(it) } ?: serverDate
        } catch (ignored: Exception) {
            serverDate
        }
    }
}
