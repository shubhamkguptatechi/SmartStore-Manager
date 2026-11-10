package com.example.ui.reports

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.CategoryExpense
import com.example.data.local.dao.TopSellingProduct
import com.example.data.local.entity.Expense
import com.example.data.local.entity.Product
import com.example.data.local.entity.Purchase
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SalesReturn
import com.example.data.repository.StoreRepository
import com.example.ui.dashboard.DateFilter
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

data class ReportAnalytics(
    val filter: DateFilter = DateFilter.THIS_MONTH,
    val grossSalesPaise: Long = 0L,
    val totalReturnsPaise: Long = 0L,
    val netSalesPaise: Long = 0L,
    val cogsPaise: Long = 0L,
    val grossProfitPaise: Long = 0L,
    val operatingExpensesPaise: Long = 0L,
    val netProfitPaise: Long = 0L,
    val totalPurchasesPaise: Long = 0L,
    val inventoryValuationPaise: Long = 0L,
    val lowStockCount: Int = 0,
    val totalTransactions: Int = 0,
    val cashSalesPaise: Long = 0L,
    val upiSalesPaise: Long = 0L,
    val cardSalesPaise: Long = 0L,
    val topSelling: List<TopSellingProduct> = emptyList(),
    val slowMoving: List<Product> = emptyList(),
    val expenseCategories: List<CategoryExpense> = emptyList()
)

class ReportsViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(DateFilter.THIS_MONTH)
    val selectedFilter: StateFlow<DateFilter> = _selectedFilter.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun setFilter(filter: DateFilter) {
        _selectedFilter.value = filter
    }

    private fun getFilterRange(filter: DateFilter): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        return when (filter) {
            DateFilter.TODAY -> Pair(DateUtils.getStartOfDay(now), DateUtils.getEndOfDay(now))
            DateFilter.YESTERDAY -> Pair(DateUtils.getStartOfYesterday(), DateUtils.getEndOfYesterday())
            DateFilter.LAST_7_DAYS -> Pair(DateUtils.getStartOfDaysAgo(7), DateUtils.getEndOfDay(now))
            DateFilter.THIS_MONTH -> Pair(DateUtils.getStartOfMonth(), DateUtils.getEndOfDay(now))
            DateFilter.LAST_MONTH -> Pair(DateUtils.getStartOfLastMonth(), DateUtils.getEndOfLastMonth())
            DateFilter.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
        }
    }

    val reportState: StateFlow<ReportAnalytics> = combine(
        _selectedFilter,
        repository.allSales,
        repository.allExpenses,
        repository.allPurchases,
        repository.allReturns,
        repository.totalInventoryValuePaise,
        repository.lowStockCount,
        repository.allProducts,
        repository.getTopSellingProducts(10)
    ) { values ->
        val filter = values[0] as DateFilter
        @Suppress("UNCHECKED_CAST") val sales = values[1] as List<Sale>
        @Suppress("UNCHECKED_CAST") val expenses = values[2] as List<Expense>
        @Suppress("UNCHECKED_CAST") val purchases = values[3] as List<Purchase>
        @Suppress("UNCHECKED_CAST") val returns = values[4] as List<SalesReturn>
        val inventoryVal = (values[5] as? Long) ?: 0L
        val lowStock = values[6] as Int
        @Suppress("UNCHECKED_CAST") val products = values[7] as List<Product>
        @Suppress("UNCHECKED_CAST") val topSelling = values[8] as List<TopSellingProduct>

        val (startTime, endTime) = getFilterRange(filter)
        val filteredSales = sales.filter { it.saleDate in startTime..endTime }
        val filteredExpenses = expenses.filter { it.expenseDate in startTime..endTime }
        val filteredPurchases = purchases.filter { it.purchaseDate in startTime..endTime }
        val filteredReturns = returns.filter { it.returnDate in startTime..endTime }

        val grossSales = filteredSales.sumOf { it.finalTotalPaise }
        val totalRefunds = filteredReturns.sumOf { it.refundAmountPaise }
        val netSales = maxOf(0L, grossSales - totalRefunds)
        val cogs = filteredSales.sumOf { it.totalCostPaise }
        val grossProfit = netSales - cogs
        val totalExpenses = filteredExpenses.sumOf { it.amountPaise }
        val netProfit = grossProfit - totalExpenses
        val totalPurchases = filteredPurchases.sumOf { it.totalAmountPaise }

        val cashSales = filteredSales.filter { it.paymentMethod == "CASH" }.sumOf { it.finalTotalPaise } +
                filteredSales.filter { it.paymentMethod == "SPLIT" }.sumOf { it.cashPaidPaise }
        val upiSales = filteredSales.filter { it.paymentMethod == "UPI" }.sumOf { it.finalTotalPaise } +
                filteredSales.filter { it.paymentMethod == "SPLIT" }.sumOf { it.upiPaidPaise }
        val cardSales = filteredSales.filter { it.paymentMethod == "CARD" }.sumOf { it.finalTotalPaise } +
                filteredSales.filter { it.paymentMethod == "SPLIT" }.sumOf { it.cardPaidPaise }

        val expenseByCat = filteredExpenses.groupBy { it.category }.map { (cat, list) ->
            CategoryExpense(category = cat, totalAmountPaise = list.sumOf { it.amountPaise })
        }.sortedByDescending { it.totalAmountPaise }

        val topIds = topSelling.map { it.productId }.toSet()
        val slowProds = products.filter { it.id !in topIds && it.currentStock > 0 }.take(5)

        ReportAnalytics(
            filter = filter,
            grossSalesPaise = grossSales,
            totalReturnsPaise = totalRefunds,
            netSalesPaise = netSales,
            cogsPaise = cogs,
            grossProfitPaise = grossProfit,
            operatingExpensesPaise = totalExpenses,
            netProfitPaise = netProfit,
            totalPurchasesPaise = totalPurchases,
            inventoryValuationPaise = inventoryVal,
            lowStockCount = lowStock,
            totalTransactions = filteredSales.size,
            cashSalesPaise = cashSales,
            upiSalesPaise = upiSales,
            cardSalesPaise = cardSales,
            topSelling = topSelling,
            slowMoving = slowProds,
            expenseCategories = expenseByCat
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportAnalytics())

    fun exportReportCsv(context: Context) {
        viewModelScope.launch {
            try {
                val rep = reportState.value
                val file = File(context.cacheDir, "SmartStore_Report_${rep.filter.name}.csv")
                FileWriter(file).use { writer ->
                    writer.appendLine("SmartStore Manager - Financial & Business Report")
                    writer.appendLine("Period,${rep.filter.label}")
                    writer.appendLine("Generated At,${DateUtils.formatDateTime(System.currentTimeMillis())}")
                    writer.appendLine()
                    writer.appendLine("Metric,Amount (INR)")
                    writer.appendLine("Gross Sales,${CurrencyUtils.formatPaise(rep.grossSalesPaise, false)}")
                    writer.appendLine("Sales Returns & Refunds,${CurrencyUtils.formatPaise(rep.totalReturnsPaise, false)}")
                    writer.appendLine("Net Sales,${CurrencyUtils.formatPaise(rep.netSalesPaise, false)}")
                    writer.appendLine("Cost of Goods Sold (COGS),${CurrencyUtils.formatPaise(rep.cogsPaise, false)}")
                    writer.appendLine("Gross Profit,${CurrencyUtils.formatPaise(rep.grossProfitPaise, false)}")
                    writer.appendLine("Total Operating Expenses,${CurrencyUtils.formatPaise(rep.operatingExpensesPaise, false)}")
                    writer.appendLine("Net Profit,${CurrencyUtils.formatPaise(rep.netProfitPaise, false)}")
                    writer.appendLine("Total Purchases,${CurrencyUtils.formatPaise(rep.totalPurchasesPaise, false)}")
                    writer.appendLine("Current Inventory Valuation,${CurrencyUtils.formatPaise(rep.inventoryValuationPaise, false)}")
                    writer.appendLine("Total Invoices,${rep.totalTransactions}")
                    writer.appendLine()
                    writer.appendLine("Payment Breakdown,Amount (INR)")
                    writer.appendLine("Cash,${CurrencyUtils.formatPaise(rep.cashSalesPaise, false)}")
                    writer.appendLine("UPI,${CurrencyUtils.formatPaise(rep.upiSalesPaise, false)}")
                    writer.appendLine("Card,${CurrencyUtils.formatPaise(rep.cardSalesPaise, false)}")
                    writer.appendLine()
                    writer.appendLine("Expense Category,Amount (INR)")
                    rep.expenseCategories.forEach {
                        writer.appendLine("${it.category},${CurrencyUtils.formatPaise(it.totalAmountPaise, false)}")
                    }
                }

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "SmartStore Report ${rep.filter.label}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Export Report CSV"))
                _messages.emit("Report CSV generated and ready to share")
            } catch (e: Exception) {
                _messages.emit("Export CSV: ${e.message}")
            }
        }
    }
}
