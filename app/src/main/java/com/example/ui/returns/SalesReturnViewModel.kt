package com.example.ui.returns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import com.example.data.local.entity.SalesReturn
import com.example.data.local.entity.SalesReturnItem
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReturnItemDraft(
    val saleItem: SaleItem,
    var returnQuantity: Double = 0.0,
    var isResalable: Boolean = true
) {
    val refundAmountPaise: Long get() = (saleItem.unitPricePaise * returnQuantity).toLong()
}

class SalesReturnViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSale = MutableStateFlow<Sale?>(null)
    val selectedSale: StateFlow<Sale?> = _selectedSale.asStateFlow()

    private val _saleItems = MutableStateFlow<List<ReturnItemDraft>>(emptyList())
    val saleItems: StateFlow<List<ReturnItemDraft>> = _saleItems.asStateFlow()

    private val _refundMethod = MutableStateFlow("CASH")
    val refundMethod: StateFlow<String> = _refundMethod.asStateFlow()

    private val _returnReason = MutableStateFlow("Customer return")
    val returnReason: StateFlow<String> = _returnReason.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val allReturns: StateFlow<List<SalesReturn>> = repository.allReturns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun setRefundMethod(m: String) {
        _refundMethod.value = m
    }

    fun setReturnReason(r: String) {
        _returnReason.value = r
    }

    fun searchSale(query: String) {
        viewModelScope.launch {
            val sale = repository.getSaleByReceiptNumber(query.trim())
            if (sale != null) {
                _selectedSale.value = sale
                val items = repository.getSaleItemsSync(sale.id)
                _saleItems.value = items.map { ReturnItemDraft(it, returnQuantity = 0.0, isResalable = true) }
            } else {
                _messages.emit("No sale found with receipt number: $query")
            }
        }
    }

    fun updateReturnQuantity(saleItemId: Long, qty: Double) {
        val current = _saleItems.value.map { item ->
            if (item.saleItem.id == saleItemId) {
                val clamped = qty.coerceIn(0.0, item.saleItem.quantity)
                item.copy(returnQuantity = clamped)
            } else item
        }
        _saleItems.value = current
    }

    fun toggleResalable(saleItemId: Long) {
        val current = _saleItems.value.map { item ->
            if (item.saleItem.id == saleItemId) {
                item.copy(isResalable = !item.isResalable)
            } else item
        }
        _saleItems.value = current
    }

    fun processReturn(onSuccess: () -> Unit) {
        val sale = _selectedSale.value ?: return
        val activeDrafts = _saleItems.value.filter { it.returnQuantity > 0.0 }
        if (activeDrafts.isEmpty()) {
            viewModelScope.launch { _messages.emit("Please select at least one item and quantity to return") }
            return
        }

        viewModelScope.launch {
            try {
                val totalRefund = activeDrafts.sumOf { it.refundAmountPaise }
                val salesReturn = SalesReturn(
                    saleId = sale.id,
                    receiptNumber = sale.receiptNumber,
                    refundAmountPaise = totalRefund,
                    paymentMethod = _refundMethod.value,
                    reason = _returnReason.value
                )

                val returnItems = activeDrafts.map { draft ->
                    SalesReturnItem(
                        returnId = 0L,
                        saleItemId = draft.saleItem.id,
                        productId = draft.saleItem.productId,
                        productName = draft.saleItem.productName,
                        returnQuantity = draft.returnQuantity,
                        unit = draft.saleItem.unit,
                        refundPaise = draft.refundAmountPaise,
                        isResalable = draft.isResalable,
                        costPriceSnapshotPaise = draft.saleItem.costPriceSnapshotPaise
                    )
                }

                repository.processSalesReturn(salesReturn, returnItems)
                _selectedSale.value = null
                _saleItems.value = emptyList()
                _messages.emit("Return processed & refund of ₹${totalRefund / 100.0} recorded!")
                onSuccess()
            } catch (e: Exception) {
                _messages.emit("Error processing return: ${e.message}")
            }
        }
    }
}
