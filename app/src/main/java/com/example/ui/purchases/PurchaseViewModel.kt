package com.example.ui.purchases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Product
import com.example.data.local.entity.Purchase
import com.example.data.local.entity.PurchaseItem
import com.example.data.local.entity.Supplier
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

data class PurchaseDraftItem(
    val product: Product,
    val quantity: Double,
    val purchaseRatePaise: Long
) {
    val totalPaise: Long get() = (purchaseRatePaise * quantity).toLong()
}

class PurchaseViewModel(private val repository: StoreRepository) : ViewModel() {

    val allPurchases: StateFlow<List<Purchase>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSuppliers: StateFlow<List<Supplier>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    // Add Purchase draft state
    private val _draftItems = MutableStateFlow<List<PurchaseDraftItem>>(emptyList())
    val draftItems: StateFlow<List<PurchaseDraftItem>> = _draftItems.asStateFlow()

    fun addDraftItem(product: Product, quantity: Double, purchaseRatePaise: Long) {
        val current = _draftItems.value.toMutableList()
        current.removeAll { it.product.id == product.id }
        current.add(PurchaseDraftItem(product, quantity, purchaseRatePaise))
        _draftItems.value = current
    }

    fun removeDraftItem(productId: Long) {
        _draftItems.value = _draftItems.value.filter { it.product.id != productId }
    }

    fun clearDraft() {
        _draftItems.value = emptyList()
    }

    fun createPurchase(
        supplier: Supplier,
        invoiceNumber: String?,
        discountPaise: Long,
        amountPaidPaise: Long,
        paymentStatus: String,
        paymentMethod: String,
        notes: String?,
        onSuccess: () -> Unit
    ) {
        val items = _draftItems.value
        if (items.isEmpty()) {
            viewModelScope.launch { _messages.emit("Please add at least one product") }
            return
        }

        viewModelScope.launch {
            try {
                val totalAmountPaise = items.sumOf { it.totalPaise } - discountPaise
                val purchase = Purchase(
                    supplierId = supplier.id,
                    supplierName = supplier.name,
                    invoiceNumber = invoiceNumber,
                    totalAmountPaise = maxOf(0L, totalAmountPaise),
                    discountPaise = discountPaise,
                    amountPaidPaise = amountPaidPaise,
                    paymentStatus = paymentStatus,
                    paymentMethod = paymentMethod,
                    notes = notes
                )

                val purchaseItems = items.map { item ->
                    PurchaseItem(
                        purchaseId = 0L,
                        productId = item.product.id,
                        productName = item.product.name,
                        quantity = item.quantity,
                        unit = item.product.unit,
                        purchaseRatePaise = item.purchaseRatePaise,
                        totalPaise = item.totalPaise
                    )
                }

                repository.processPurchase(purchase, purchaseItems)
                clearDraft()
                _messages.emit("Purchase recorded & stock updated successfully!")
                onSuccess()
            } catch (e: Exception) {
                _messages.emit("Error recording purchase: ${e.message}")
            }
        }
    }

    fun saveSupplier(supplier: Supplier, isNew: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            if (isNew) {
                repository.insertSupplier(supplier)
                _messages.emit("Supplier '${supplier.name}' added")
            } else {
                repository.updateSupplier(supplier)
                _messages.emit("Supplier '${supplier.name}' updated")
            }
            onDone()
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
            _messages.emit("Supplier deleted")
        }
    }
}
