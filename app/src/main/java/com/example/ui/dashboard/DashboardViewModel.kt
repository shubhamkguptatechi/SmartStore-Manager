package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.TopSellingProduct
import com.example.data.local.entity.Product
import com.example.data.local.entity.Sale
import com.example.data.repository.FinancialSummary
import com.example.data.repository.StoreRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DateFilter(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    LAST_7_DAYS("Last 7 Days"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    ALL_TIME("All Time")
}

data class DashboardUiState(
    val selectedFilter: DateFilter = DateFilter.TODAY,
    val summary: FinancialSummary = FinancialSummary(),
    val totalInventoryValuePaise: Long = 0L,
    val lowStockCount: Int = 0,
    val totalProductsCount: Int = 0,
    val recentSales: List<Sale> = emptyList(),
    val topSellingProducts: List<TopSellingProduct> = emptyList(),
    val lowStockProducts: List<Product> = emptyList(),
    val dailyTrendPoints: List<Pair<String, Long>> = emptyList() // Day label -> Sales paise
)

class DashboardViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(DateFilter.TODAY)
    val selectedFilter: StateFlow<DateFilter> = _selectedFilter.asStateFlow()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    fun setFilter(filter: DateFilter) {
        _selectedFilter.value = filter
        refreshStats()
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

    private fun observeData() {
        viewModelScope.launch {
            combine(
                _selectedFilter,
                repository.allSales,
                repository.allExpenses,
                repository.allReturns,
                repository.totalInventoryValuePaise,
                repository.lowStockCount,
                repository.totalProductsCount,
                repository.getTopSellingProducts(5),
                repository.lowStockProducts
            ) { values ->
                val filter = values[0] as DateFilter
                @Suppress("UNCHECKED_CAST")
                val sales = values[1] as List<Sale>
                @Suppress("UNCHECKED_CAST")
                val expenses = values[2] as List<com.example.data.local.entity.Expense>
                @Suppress("UNCHECKED_CAST")
                val returns = values[3] as List<com.example.data.local.entity.SalesReturn>
                val invVal = (values[4] as? Long) ?: 0L
                val lowStock = values[5] as Int
                val totalProds = values[6] as Int
                @Suppress("UNCHECKED_CAST")
                val topProducts = values[7] as List<TopSellingProduct>
                @Suppress("UNCHECKED_CAST")
                val lowStockList = values[8] as List<Product>

                val (startTime, endTime) = getFilterRange(filter)
                val filteredSales = sales.filter { it.saleDate in startTime..endTime }
                val filteredExpenses = expenses.filter { it.expenseDate in startTime..endTime }
                val filteredReturns = returns.filter { it.returnDate in startTime..endTime }

                val totalSalesPaise = filteredSales.sumOf { it.finalTotalPaise }
                val totalCostPaise = filteredSales.sumOf { it.totalCostPaise }
                val totalRefundsPaise = filteredReturns.sumOf { it.refundAmountPaise }
                val netSalesPaise = maxOf(0L, totalSalesPaise - totalRefundsPaise)

                // Gross Profit = Net Sales - Cost of Goods Sold
                val grossProfitPaise = netSalesPaise - totalCostPaise
                val totalExpensesPaise = filteredExpenses.sumOf { it.amountPaise }
                // Net Profit = Gross Profit - Operating Expenses
                val netProfitPaise = grossProfitPaise - totalExpensesPaise

                val cashSales = filteredSales.filter { it.paymentMethod == "CASH" }.sumOf { it.finalTotalPaise } +
                        filteredSales.filter { it.paymentMethod == "SPLIT" }.sumOf { it.cashPaidPaise }
                val upiSales = filteredSales.filter { it.paymentMethod == "UPI" }.sumOf { it.finalTotalPaise } +
                        filteredSales.filter { it.paymentMethod == "SPLIT" }.sumOf { it.upiPaidPaise }
                val cardSales = filteredSales.filter { it.paymentMethod == "CARD" }.sumOf { it.finalTotalPaise } +
                        filteredSales.filter { it.paymentMethod == "SPLIT" }.sumOf { it.cardPaidPaise }

                val summary = FinancialSummary(
                    totalSalesPaise = totalSalesPaise,
                    totalCostPaise = totalCostPaise,
                    grossProfitPaise = grossProfitPaise,
                    totalExpensesPaise = totalExpensesPaise,
                    netProfitPaise = netProfitPaise,
                    totalTransactions = filteredSales.size,
                    totalRefundsPaise = totalRefundsPaise,
                    cashSalesPaise = cashSales,
                    upiSalesPaise = upiSales,
                    cardSalesPaise = cardSales
                )

                // Calculate daily trend points for the last 7 days
                val trendPoints = (6 downTo 0).map { daysAgo ->
                    val dayStart = DateUtils.getStartOfDaysAgo(daysAgo)
                    val dayEnd = DateUtils.getEndOfDay(dayStart)
                    val daySales = sales.filter { it.saleDate in dayStart..dayEnd }.sumOf { it.finalTotalPaise }
                    val label = when (daysAgo) {
                        0 -> "Today"
                        1 -> "Y'day"
                        else -> "${daysAgo}d ago"
                    }
                    Pair(label, daySales)
                }

                DashboardUiState(
                    selectedFilter = filter,
                    summary = summary,
                    totalInventoryValuePaise = invVal,
                    lowStockCount = lowStock,
                    totalProductsCount = totalProds,
                    recentSales = sales.take(5),
                    topSellingProducts = topProducts,
                    lowStockProducts = lowStockList.take(4),
                    dailyTrendPoints = trendPoints
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun refreshStats() {
        // combine will re-trigger with new filter
    }
}
