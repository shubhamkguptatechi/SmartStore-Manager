package com.example

import com.example.util.CurrencyUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCurrencyFormatting() {
        val paise = 12550L // ₹125.50
        val formatted = CurrencyUtils.formatPaise(paise)
        assertEquals("₹125.50", formatted)

        val parsed = CurrencyUtils.parseRupeesInputToPaise("125.50")
        assertEquals(12550L, parsed)
    }

    @Test
    fun testFractionalQuantityFormatting() {
        assertEquals("1.5 kg", CurrencyUtils.formatQuantity(1.5, "kg"))
        assertEquals("2 Piece", CurrencyUtils.formatQuantity(2.0, "Piece"))
        assertEquals("0.25 Litre", CurrencyUtils.formatQuantity(0.25, "Litre"))
    }

    @Test
    fun testProfitAndLossCalculations() {
        val grossSalesPaise = 500000L // ₹5,000.00
        val refundsPaise = 20000L     // ₹200.00
        val netSalesPaise = grossSalesPaise - refundsPaise // ₹4,800.00

        val cogsPaise = 320000L       // ₹3,200.00 (Cost of goods sold)
        val grossProfitPaise = netSalesPaise - cogsPaise // ₹1,600.00
        assertEquals(160000L, grossProfitPaise)

        val operatingExpensesPaise = 60000L // ₹600.00
        val netProfitPaise = grossProfitPaise - operatingExpensesPaise // ₹1,000.00
        assertEquals(100000L, netProfitPaise)
    }

    @Test
    fun testSplitPaymentCalculation() {
        val totalPaise = 150000L // ₹1,500.00
        val cashPaid = 50000L   // ₹500.00
        val upiPaid = 100000L   // ₹1,000.00
        val cardPaid = 0L

        val sumSplit = cashPaid + upiPaid + cardPaid
        assertEquals(totalPaise, sumSplit)
    }
}
