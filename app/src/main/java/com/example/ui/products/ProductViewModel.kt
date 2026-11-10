package com.example.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Category
import com.example.data.local.entity.Product
import com.example.data.local.entity.StockMovement
import com.example.data.repository.StoreRepository
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

enum class ProductSortOption(val label: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    PRICE_LOW_HIGH("Price (Low-High)"),
    PRICE_HIGH_LOW("Price (High-Low)"),
    STOCK_LOW_HIGH("Stock (Lowest first)"),
    STOCK_HIGH_LOW("Stock (Highest first)")
}

data class ProductListUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val sortOption: ProductSortOption = ProductSortOption.NAME_ASC,
    val filterLowStockOnly: Boolean = false,
    val products: List<Product> = emptyList(),
    val totalCount: Int = 0,
    val lowStockCount: Int = 0
)

class ProductViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")
    private val _sortOption = MutableStateFlow(ProductSortOption.NAME_ASC)
    private val _filterLowStockOnly = MutableStateFlow(false)

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<ProductListUiState> = combine(
        repository.allProducts,
        _searchQuery,
        _selectedCategory,
        _sortOption,
        _filterLowStockOnly
    ) { products, query, category, sort, lowStockOnly ->
        var filtered = products

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(q) ||
                        it.code.lowercase().contains(q) ||
                        (it.barcode?.lowercase()?.contains(q) == true)
            }
        }

        if (category != "All") {
            filtered = filtered.filter { it.category == category }
        }

        if (lowStockOnly) {
            filtered = filtered.filter { it.currentStock <= it.minStockAlert }
        }

        val sorted = when (sort) {
            ProductSortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            ProductSortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            ProductSortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.sellingPricePaise }
            ProductSortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.sellingPricePaise }
            ProductSortOption.STOCK_LOW_HIGH -> filtered.sortedBy { it.currentStock }
            ProductSortOption.STOCK_HIGH_LOW -> filtered.sortedByDescending { it.currentStock }
        }

        val lowStockTotal = products.count { it.currentStock <= it.minStockAlert }

        ProductListUiState(
            searchQuery = query,
            selectedCategory = category,
            sortOption = sort,
            filterLowStockOnly = lowStockOnly,
            products = sorted,
            totalCount = products.size,
            lowStockCount = lowStockTotal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductListUiState())

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSelectedCategory(cat: String) { _selectedCategory.value = cat }
    fun setSortOption(sort: ProductSortOption) { _sortOption.value = sort }
    fun toggleLowStockFilter() { _filterLowStockOnly.value = !_filterLowStockOnly.value }

    fun saveProduct(product: Product, isNew: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                if (isNew) {
                    repository.insertProduct(product)
                    _messages.emit("Product '${product.name}' added successfully")
                } else {
                    repository.updateProduct(product)
                    _messages.emit("Product '${product.name}' updated")
                }
                onDone()
            } catch (e: Exception) {
                _messages.emit("Error saving product: ${e.message}")
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _messages.emit("Product '${product.name}' deleted")
        }
    }

    fun adjustStock(productId: Long, delta: Double, reason: String) {
        viewModelScope.launch {
            repository.adjustStock(productId, delta, reason)
            _messages.emit("Stock adjusted successfully")
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertCategory(Category(name = name.trim()))
                _messages.emit("Category '$name' added")
            }
        }
    }

    fun getStockMovements(productId: Long): StateFlow<List<StockMovement>> {
        return (if (productId > 0) repository.getMovementsForProduct(productId) else repository.allStockMovements)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}
