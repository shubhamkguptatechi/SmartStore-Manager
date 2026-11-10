package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {
    private val indianFormat = DecimalFormat("##,##,##0.00", DecimalFormatSymbols(Locale.ENGLISH))
    private val wholeFormat = DecimalFormat("##,##,##0", DecimalFormatSymbols(Locale.ENGLISH))

    /**
     * Converts paise (e.g. 1050) to formatted currency string (e.g. "₹10.50")
     */
    fun formatPaise(paise: Long, includeSymbol: Boolean = true): String {
        val rupees = paise / 100.0
        val formatted = indianFormat.format(rupees)
        return if (includeSymbol) "₹$formatted" else formatted
    }

    /**
     * Formats rupees value directly
     */
    fun formatRupees(rupees: Double, includeSymbol: Boolean = true): String {
        val formatted = indianFormat.format(rupees)
        return if (includeSymbol) "₹$formatted" else formatted
    }

    /**
     * Converts rupees (e.g. 10.50) to integer paise (1050)
     */
    fun rupeesToPaise(rupees: Double): Long {
        return Math.round(rupees * 100.0)
    }

    /**
     * Parse text input in rupees to paise safely
     */
    fun parseRupeesInputToPaise(input: String): Long {
        val cleaned = input.trim().replace(",", "")
        val doubleVal = cleaned.toDoubleOrNull() ?: 0.0
        return rupeesToPaise(doubleVal)
    }

    /**
     * Format fractional quantity nicely (e.g. "1.5 kg" or "2 pcs")
     */
    fun formatQuantity(quantity: Double, unit: String): String {
        val formattedQty = if (quantity % 1.0 == 0.0) {
            quantity.toLong().toString()
        } else {
            String.format(Locale.ENGLISH, "%.2f", quantity).trimEnd('0').trimEnd('.')
        }
        return "$formattedQty $unit"
    }
}
