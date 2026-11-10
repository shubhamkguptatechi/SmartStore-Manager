package com.example.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StorePreferences
import com.example.data.local.StoreSettings
import com.example.data.local.entity.Category
import com.example.data.local.entity.Product
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import com.example.data.repository.CartItem
import com.example.data.repository.StoreRepository
import com.example.util.CurrencyUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class SalesEvent {
    data class SaleCompleted(val saleId: Long) : SalesEvent()
    data class ShowMessage(val message: String, val isError: Boolean = false) : SalesEvent()
}

data class PosUiState(
    val cartItems: List<CartItem> = emptyList(),
    val billDiscountPaise: Long = 0L,
    val paymentMethod: String = "CASH", // CASH, UPI, CARD, SPLIT
    val splitCashAmountPaise: Long = 0L,
    val splitUpiAmountPaise: Long = 0L,
    val splitCardAmountPaise: Long = 0L,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val isProcessing: Boolean = false,
    val scannedBarcode: String = ""
) {
    val subtotalPaise: Long get() = cartItems.sumOf { it.grossTotalPaise }
    val itemDiscountPaise: Long get() = cartItems.sumOf { it.discountPaise }
    val totalDiscountPaise: Long get() = itemDiscountPaise + billDiscountPaise
    val finalTotalPaise: Long get() = maxOf(0L, subtotalPaise - totalDiscountPaise)
    val totalItemsCount: Int get() = cartItems.size
}

class SalesViewModel(
    private val repository: StoreRepository,
    private val preferences: StorePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SalesEvent>()
    val events: SharedFlow<SalesEvent> = _events.asSharedFlow()

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSales: StateFlow<List<Sale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<StoreSettings> = preferences.storeSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StoreSettings())

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setPaymentMethod(method: String) {
        val total = _uiState.value.finalTotalPaise
        val current = _uiState.value
        _uiState.value = current.copy(
            paymentMethod = method,
            splitCashAmountPaise = if (method == "SPLIT") total / 2 else if (method == "CASH") total else 0L,
            splitUpiAmountPaise = if (method == "SPLIT") total - (total / 2) else if (method == "UPI") total else 0L,
            splitCardAmountPaise = if (method == "CARD") total else 0L
        )
    }

    fun updateSplitAmounts(cash: Long, upi: Long, card: Long) {
        _uiState.value = _uiState.value.copy(
            splitCashAmountPaise = cash,
            splitUpiAmountPaise = upi,
            splitCardAmountPaise = card
        )
    }

    fun addToCart(product: Product, quantity: Double = 1.0) {
        val currentCart = _uiState.value.cartItems.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.product.id == product.id }

        val allowNegative = settings.value.allowNegativeStock
        val currentQtyInCart = if (existingIndex >= 0) currentCart[existingIndex].quantity else 0.0
        val targetQty = currentQtyInCart + quantity

        if (!allowNegative && product.currentStock < targetQty) {
            viewModelScope.launch {
                _events.emit(SalesEvent.ShowMessage("Only ${product.currentStock} ${product.unit} available in stock!", isError = true))
            }
            return
        }

        if (existingIndex >= 0) {
            val item = currentCart[existingIndex]
            currentCart[existingIndex] = item.copy(quantity = targetQty)
        } else {
            currentCart.add(CartItem(product = product, quantity = quantity))
        }

        _uiState.value = _uiState.value.copy(cartItems = currentCart)
    }

    fun updateQuantity(productId: Long, newQuantity: Double) {
        if (newQuantity <= 0.0) {
            removeFromCart(productId)
            return
        }

        val currentCart = _uiState.value.cartItems.toMutableList()
        val index = currentCart.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = currentCart[index]
            val allowNegative = settings.value.allowNegativeStock
            if (!allowNegative && item.product.currentStock < newQuantity) {
                viewModelScope.launch {
                    _events.emit(SalesEvent.ShowMessage("Exceeds available stock (${item.product.currentStock} ${item.product.unit})", isError = true))
                }
                return
            }
            currentCart[index] = item.copy(quantity = newQuantity)
            _uiState.value = _uiState.value.copy(cartItems = currentCart)
        }
    }

    fun setItemDiscount(productId: Long, discountPaise: Long) {
        val currentCart = _uiState.value.cartItems.toMutableList()
        val index = currentCart.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = currentCart[index]
            currentCart[index] = item.copy(discountPaise = discountPaise)
            _uiState.value = _uiState.value.copy(cartItems = currentCart)
        }
    }

    fun setBillDiscount(discountPaise: Long) {
        _uiState.value = _uiState.value.copy(billDiscountPaise = discountPaise)
    }

    fun removeFromCart(productId: Long) {
        val currentCart = _uiState.value.cartItems.filter { it.product.id != productId }
        _uiState.value = _uiState.value.copy(cartItems = currentCart)
    }

    fun clearCart() {
        _uiState.value = _uiState.value.copy(
            cartItems = emptyList(),
            billDiscountPaise = 0L,
            searchQuery = ""
        )
    }

    fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            val product = repository.getProductByBarcode(barcode) ?: repository.getProductByCode(barcode)
            if (product != null) {
                addToCart(product, 1.0)
                _events.emit(SalesEvent.ShowMessage("Added ${product.name} to cart"))
            } else {
                _events.emit(SalesEvent.ShowMessage("No product found with code/barcode: $barcode", isError = true))
            }
        }
    }

    fun completeCheckout() {
        if (_uiState.value.isProcessing) return // Prevent duplicate taps
        if (_uiState.value.cartItems.isEmpty()) {
            viewModelScope.launch {
                _events.emit(SalesEvent.ShowMessage("Cart is empty", isError = true))
            }
            return
        }

        val state = _uiState.value
        val method = state.paymentMethod
        val finalTotal = state.finalTotalPaise

        var cash = 0L
        var upi = 0L
        var card = 0L

        when (method) {
            "CASH" -> cash = finalTotal
            "UPI" -> upi = finalTotal
            "CARD" -> card = finalTotal
            "SPLIT" -> {
                cash = state.splitCashAmountPaise
                upi = state.splitUpiAmountPaise
                card = state.splitCardAmountPaise
                val sumSplit = cash + upi + card
                if (sumSplit != finalTotal) {
                    viewModelScope.launch {
                        _events.emit(SalesEvent.ShowMessage("Split sum (${CurrencyUtils.formatPaise(sumSplit)}) must equal total (${CurrencyUtils.formatPaise(finalTotal)})", isError = true))
                    }
                    return
                }
            }
        }

        _uiState.value = _uiState.value.copy(isProcessing = true)

        viewModelScope.launch {
            val result = repository.completeSale(
                cartItems = state.cartItems,
                billDiscountPaise = state.billDiscountPaise,
                paymentMethod = method,
                cashPaidPaise = cash,
                upiPaidPaise = upi,
                cardPaidPaise = card,
                allowNegativeStock = settings.value.allowNegativeStock
            )

            result.onSuccess { sale ->
                _uiState.value = PosUiState() // reset cart
                _events.emit(SalesEvent.SaleCompleted(sale.id))
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(isProcessing = false)
                _events.emit(SalesEvent.ShowMessage(err.message ?: "Failed to complete sale", isError = true))
            }
        }
    }

    suspend fun getSaleDetails(saleId: Long): Pair<Sale?, List<SaleItem>> {
        val sale = repository.getSaleByIdSync(saleId)
        val items = if (sale != null) repository.getSaleItemsSync(saleId) else emptyList()
        return Pair(sale, items)
    }
}
