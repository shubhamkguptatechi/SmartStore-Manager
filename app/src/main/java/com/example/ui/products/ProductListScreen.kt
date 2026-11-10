package com.example.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.Product
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.WarningAmber
import com.example.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    onNavigateToAddEdit: (Long) -> Unit,
    onNavigateToMovements: (Long) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var productToAdjust by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { msg ->
            onShowSnackbar(msg)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("product_list_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search & Filter Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search products, code, barcode...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_search_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .testTag("product_sort_button")
                            ) {
                                Icon(Icons.Default.FilterList, contentDescription = "Sort Products")
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                ProductSortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.label) },
                                        onClick = {
                                            viewModel.setSortOption(option)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { onNavigateToMovements(-1L) },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .testTag("stock_movements_button")
                        ) {
                            Icon(Icons.Default.History, contentDescription = "Stock Movement History")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Categories & Low Stock Filter
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.filterLowStockOnly,
                                onClick = { viewModel.toggleLowStockFilter() },
                                label = { Text("Low Stock (${uiState.lowStockCount})") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (uiState.filterLowStockOnly) Color.White else WarningAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WarningAmber,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        item {
                            FilterChip(
                                selected = uiState.selectedCategory == "All",
                                onClick = { viewModel.setSelectedCategory("All") },
                                label = { Text("All") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        items(categories) { cat ->
                            FilterChip(
                                selected = uiState.selectedCategory == cat.name,
                                onClick = { viewModel.setSelectedCategory(cat.name) },
                                label = { Text(cat.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Products Count Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.products.size} Products shown",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sorted by: ${uiState.sortOption.label}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Product Cards List
            if (uiState.products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No products found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button below to add your first product",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.products, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            onEdit = { onNavigateToAddEdit(product.id) },
                            onAdjustStock = { productToAdjust = product },
                            onDelete = { productToDelete = product },
                            onViewHistory = { onNavigateToMovements(product.id) }
                        )
                    }
                }
            }
        }

        // FAB to Add Product
        FloatingActionButton(
            onClick = { onNavigateToAddEdit(-1L) },
            containerColor = PrimaryGreen,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_product_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Product")
        }
    }

    // Stock Adjustment Dialog
    productToAdjust?.let { product ->
        var deltaInput by remember { mutableStateOf("") }
        var isAddition by remember { mutableStateOf(true) }
        var reasonInput by remember { mutableStateOf("Physical count correction") }

        AlertDialog(
            onDismissRequest = { productToAdjust = null },
            title = { Text("Adjust Stock: ${product.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current Stock: ${CurrencyUtils.formatQuantity(product.currentStock, product.unit)}", fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isAddition,
                            onClick = { isAddition = true },
                            label = { Text("+ Add Stock") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = !isAddition,
                            onClick = { isAddition = false },
                            label = { Text("- Deduct / Damage") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ErrorRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    OutlinedTextField(
                        value = deltaInput,
                        onValueChange = { deltaInput = it },
                        label = { Text("Quantity (${product.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        label = { Text("Reason for adjustment") },
                        placeholder = { Text("e.g. Expired, Damage, Count correction") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = deltaInput.toDoubleOrNull() ?: 0.0
                        if (qty > 0) {
                            val signedQty = if (isAddition) qty else -qty
                            viewModel.adjustStock(product.id, signedQty, reasonInput)
                            productToAdjust = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Save Adjustment")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToAdjust = null }) { Text("Cancel") }
            }
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to permanently delete '${product.name}'? Existing sales history will remain preserved.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(product)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProductItemCard(
    product: Product,
    onEdit: () -> Unit,
    onAdjustStock: () -> Unit,
    onDelete: () -> Unit,
    onViewHistory: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isLowStock = product.currentStock <= product.minStockAlert

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Code: ${product.code}${if (product.barcode != null) " • Barcode: ${product.barcode}" else ""} • ${product.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit Product") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Adjust Stock") },
                            leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onAdjustStock()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Movement History") },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onViewHistory()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Product", color = ErrorRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Stock Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sell: ${CurrencyUtils.formatPaise(product.sellingPricePaise)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    )
                    Text(
                        text = "Cost: ${CurrencyUtils.formatPaise(product.purchasePricePaise)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stock Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isLowStock) WarningAmber.copy(alpha = 0.15f) else PrimaryGreen.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLowStock) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "Stock: ${CurrencyUtils.formatQuantity(product.currentStock, product.unit)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isLowStock) WarningAmber else PrimaryGreen
                            )
                        )
                    }
                }
            }
        }
    }
}
