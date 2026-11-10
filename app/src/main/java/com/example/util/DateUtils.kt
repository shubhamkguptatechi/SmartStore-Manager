package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    private val receiptDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH)
    private val codeDateFormat = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)

    fun formatDate(timestamp: Long): String {
        return displayDateFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatReceiptDateTime(timestamp: Long): String {
        return receiptDateFormat.format(Date(timestamp))
    }

    fun getTodayCodeDate(): String {
        return codeDateFormat.format(Date())
    }

    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getStartOfYesterday(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return getStartOfDay(cal.timeInMillis)
    }

    fun getEndOfYesterday(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return getEndOfDay(cal.timeInMillis)
    }

    fun getStartOfDaysAgo(days: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return getStartOfDay(cal.timeInMillis)
    }

    fun getStartOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return getStartOfDay(cal.timeInMillis)
    }

    fun getStartOfLastMonth(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return getStartOfDay(cal.timeInMillis)
    }

    fun getEndOfLastMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        return cal.timeInMillis
    }

    fun getStartOfYear(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_YEAR, 1)
        return getStartOfDay(cal.timeInMillis)
    }
}
