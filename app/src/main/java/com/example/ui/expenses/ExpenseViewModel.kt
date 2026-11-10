package com.example.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Expense
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val DEFAULT_EXPENSE_CATEGORIES = listOf(
    "Rent",
    "Electricity",
    "Salary",
    "Transport",
    "Packaging",
    "Maintenance",
    "Other"
)

data class ExpensesUiState(
    val selectedCategory: String = "All",
    val expenses: List<Expense> = emptyList(),
    val totalExpensesPaise: Long = 0L
)

class ExpenseViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val uiState: StateFlow<ExpensesUiState> = combine(
        repository.allExpenses,
        _selectedCategory
    ) { all, category ->
        val filtered = if (category == "All") all else all.filter { it.category == category }
        val total = filtered.sumOf { it.amountPaise }
        ExpensesUiState(
            selectedCategory = category,
            expenses = filtered,
            totalExpensesPaise = total
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExpensesUiState())

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun saveExpense(expense: Expense, isNew: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            if (isNew) {
                repository.insertExpense(expense)
                _messages.emit("Expense of ₹${expense.amountPaise / 100.0} recorded")
            } else {
                repository.updateExpense(expense)
                _messages.emit("Expense updated")
            }
            onDone()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _messages.emit("Expense deleted")
        }
    }
}
