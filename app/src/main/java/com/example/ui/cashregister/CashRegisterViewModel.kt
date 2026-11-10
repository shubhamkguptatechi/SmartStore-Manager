package com.example.ui.cashregister

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CashRegisterSession
import com.example.data.repository.StoreRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CashSummary(
    val openingCashPaise: Long = 0L,
    val cashSalesPaise: Long = 0L,
    val cashRefundsPaise: Long = 0L,
    val cashExpensesPaise: Long = 0L,
    val expectedClosingCashPaise: Long = 0L,
    val nonCashSalesUpiPaise: Long = 0L,
    val nonCashSalesCardPaise: Long = 0L
)

data class CashRegisterUiState(
    val activeSession: CashRegisterSession? = null,
    val summary: CashSummary = CashSummary(),
    val pastSessions: List<CashRegisterSession> = emptyList()
)

class CashRegisterViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val uiState: StateFlow<CashRegisterUiState> = combine(
        repository.activeCashSession,
        repository.allSales,
        repository.allExpenses,
        repository.allReturns,
        repository.allCashSessions
    ) { activeSession, sales, expenses, returns, sessions ->
        if (activeSession == null) {
            CashRegisterUiState(activeSession = null, pastSessions = sessions.filter { !it.isOpen })
        } else {
            val sessionStart = activeSession.openedAt
            val sessionEnd = System.currentTimeMillis()

            val sessionSales = sales.filter { it.saleDate in sessionStart..sessionEnd }
            val sessionExpenses = expenses.filter { it.expenseDate in sessionStart..sessionEnd && it.paymentMethod == "CASH" }
            val sessionReturns = returns.filter { it.returnDate in sessionStart..sessionEnd && it.paymentMethod == "CASH" }

            val cashSales = sessionSales.filter { it.paymentMethod == "CASH" }.sumOf { it.finalTotalPaise } +
                    sessionSales.filter { it.paymentMethod == "SPLIT" }.sumOf { it.cashPaidPaise }
            val upiSales = sessionSales.filter { it.paymentMethod == "UPI" }.sumOf { it.finalTotalPaise } +
                    sessionSales.filter { it.paymentMethod == "SPLIT" }.sumOf { it.upiPaidPaise }
            val cardSales = sessionSales.filter { it.paymentMethod == "CARD" }.sumOf { it.finalTotalPaise } +
                    sessionSales.filter { it.paymentMethod == "SPLIT" }.sumOf { it.cardPaidPaise }

            val cashExpenses = sessionExpenses.sumOf { it.amountPaise }
            val cashRefunds = sessionReturns.sumOf { it.refundAmountPaise }

            val opening = activeSession.openingCashPaise
            val expectedClosing = opening + cashSales - cashRefunds - cashExpenses

            val summary = CashSummary(
                openingCashPaise = opening,
                cashSalesPaise = cashSales,
                cashRefundsPaise = cashRefunds,
                cashExpensesPaise = cashExpenses,
                expectedClosingCashPaise = expectedClosing,
                nonCashSalesUpiPaise = upiSales,
                nonCashSalesCardPaise = cardSales
            )

            CashRegisterUiState(
                activeSession = activeSession,
                summary = summary,
                pastSessions = sessions.filter { !it.isOpen }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CashRegisterUiState())

    fun openRegister(openingPaise: Long, notes: String?) {
        viewModelScope.launch {
            repository.openCashSession(openingPaise, notes)
            _messages.emit("Cash register session opened successfully!")
        }
    }

    fun closeRegister(session: CashRegisterSession, actualClosingPaise: Long, notes: String?) {
        viewModelScope.launch {
            repository.closeCashSession(session, actualClosingPaise, notes)
            _messages.emit("Cash register session closed successfully!")
        }
    }
}
