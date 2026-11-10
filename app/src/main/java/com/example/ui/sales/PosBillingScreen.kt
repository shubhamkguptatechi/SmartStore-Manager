package com.example.ui.sales

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.repository.CartItem
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.WarningAmber
import com.example.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosBillingScreen(
    viewModel: SalesViewModel,
    onNavigateToReceipt: (Long) -> Unit,
    onNavigateToHistory: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    var showBarcodeDialog by remember { mutableStateOf(false) }
    var showBillDiscountDialog by remember { mutableStateOf(false) }
    var showSplitDialog by remember { mutableStateOf(false) }
    var itemDiscountDialogProduct by remember { mutableStateOf<Product?>(null) }
    var fractionalQtyProduct by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SalesEvent.SaleCompleted -> {
                    onShowSnackbar("Sale completed successfully!")
                    onNavigateToReceipt(event.saleId)
                }
                is SalesEvent.ShowMessage -> {
                    onShowSnackbar(event.message)
                }
            }
        }
    }

    // Filter available products for quick add list
    val filteredProducts = remember(allProducts, uiState.searchQuery, uiState.selectedCategory) {
        var list = allProducts
        if (uiState.searchQuery.isNotBlank()) {
            val q = uiState.searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                        it.code.lowercase().contains(q) ||
                        (it.barcode?.lowercase()?.contains(q) == true)
            }
        }
        if (uiState.selectedCategory != "All") {
            list = list.filter { it.category == uiState.selectedCategory }
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pos_billing_screen")
    ) {
        // Top Action Bar & Search
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
                        placeholder = { Text("Search product name, code...") },
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
                            .testTag("pos_search_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showBarcodeDialog = true },
                        modifier = Modifier
                            .background(PrimaryGreen.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .testTag("barcode_scanner_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Barcode",
                            tint = PrimaryGreen
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .testTag("sales_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Sales History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
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

        // Main Content Area: Cart on top/split, product catalog picker
        Row(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
            // Left/Center: Active Cart List
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Cart (${uiState.totalItemsCount})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (uiState.cartItems.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("Clear", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                if (uiState.cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Cart is empty",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap products from the list to add",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.cartItems, key = { it.product.id }) { cartItem ->
                            CartItemRow(
                                item = cartItem,
                                onIncrease = { viewModel.addToCart(cartItem.product, 1.0) },
                                onDecrease = { viewModel.updateQuantity(cartItem.product.id, cartItem.quantity - 1.0) },
                                onCustomQty = { fractionalQtyProduct = cartItem.product },
                                onDiscount = { itemDiscountDialogProduct = cartItem.product },
                                onRemove = { viewModel.removeFromCart(cartItem.product.id) }
                            )
                        }
                    }
                }
            }

            // Right: Quick Products Picker (Catalog)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Catalog",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductPickerCard(
                            product = product,
                            onAdd = { viewModel.addToCart(product, 1.0) }
                        )
                    }
                }
            }
        }

        // Bottom Billing / Checkout Panel
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Payment Method Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pay by:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    listOf("CASH", "UPI", "CARD", "SPLIT").forEach { method ->
                        FilterChip(
                            selected = uiState.paymentMethod == method,
                            onClick = {
                                viewModel.setPaymentMethod(method)
                                if (method == "SPLIT") showSplitDialog = true
                            },
                            label = { Text(method) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Subtotal: ${CurrencyUtils.formatPaise(uiState.subtotalPaise)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (uiState.totalDiscountPaise > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Disc: -${CurrencyUtils.formatPaise(uiState.totalDiscountPaise)}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = WarningAmber)
                                )
                            }
                        }
                        Text(
                            text = CurrencyUtils.formatPaise(uiState.finalTotalPaise),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showBillDiscountDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Discount, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Discount")
                        }

                        Button(
                            onClick = { viewModel.completeCheckout() },
                            enabled = uiState.cartItems.isNotEmpty() && !uiState.isProcessing,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            modifier = Modifier.testTag("complete_sale_button")
                        ) {
                            if (uiState.isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Complete Sale")
                            }
                        }
                    }
                }
            }
        }
    }

    // Barcode Scanner / Manual Code Entry Dialog
    if (showBarcodeDialog) {
        var barcodeInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBarcodeDialog = false },
            title = { Text("Barcode / Product Code") },
            text = {
                Column {
                    Text("Scan or enter barcode/product code to add item directly:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = barcodeInput,
                        onValueChange = { barcodeInput = it },
                        placeholder = { Text("e.g. 890123456701 or BR-1KG") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("barcode_dialog_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (barcodeInput.isNotBlank()) {
                            viewModel.onBarcodeScanned(barcodeInput.trim())
                            showBarcodeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Add Product")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBarcodeDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Bill Discount Dialog
    if (showBillDiscountDialog) {
        var discountRupees by remember { mutableStateOf((uiState.billDiscountPaise / 100.0).toString()) }
        AlertDialog(
            onDismissRequest = { showBillDiscountDialog = false },
            title = { Text("Apply Bill Discount") },
            text = {
                Column {
                    Text("Enter flat discount amount in Rupees (₹):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = discountRupees,
                        onValueChange = { discountRupees = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        prefix = { Text("₹") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val paise = CurrencyUtils.parseRupeesInputToPaise(discountRupees)
                        viewModel.setBillDiscount(paise)
                        showBillDiscountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBillDiscountDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Fractional Quantity Input Dialog (e.g. 0.5 kg, 1.25 L)
    fractionalQtyProduct?.let { product ->
        val currentQty = uiState.cartItems.firstOrNull { it.product.id == product.id }?.quantity ?: 1.0
        var qtyInput by remember { mutableStateOf(currentQty.toString()) }

        AlertDialog(
            onDismissRequest = { fractionalQtyProduct = null },
            title = { Text("Enter Quantity (${product.unit})") },
            text = {
                Column {
                    Text("Product: ${product.name}")
                    Text("Current stock: ${product.currentStock} ${product.unit}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        suffix = { Text(product.unit) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = qtyInput.toDoubleOrNull() ?: 1.0
                        viewModel.updateQuantity(product.id, parsed)
                        fractionalQtyProduct = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { fractionalQtyProduct = null }) { Text("Cancel") }
            }
        )
    }

    // Item Discount Dialog
    itemDiscountDialogProduct?.let { product ->
        val currentItem = uiState.cartItems.firstOrNull { it.product.id == product.id }
        var discountRupees by remember { mutableStateOf(((currentItem?.discountPaise ?: 0L) / 100.0).toString()) }

        AlertDialog(
            onDismissRequest = { itemDiscountDialogProduct = null },
            title = { Text("Item Discount (${product.name})") },
            text = {
                Column {
                    Text("Enter discount on this item in Rupees (₹):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = discountRupees,
                        onValueChange = { discountRupees = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        prefix = { Text("₹") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val paise = CurrencyUtils.parseRupeesInputToPaise(discountRupees)
                        viewModel.setItemDiscount(product.id, paise)
                        itemDiscountDialogProduct = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemDiscountDialogProduct = null }) { Text("Cancel") }
            }
        )
    }

    // Split Payment Dialog
    if (showSplitDialog) {
        val total = uiState.finalTotalPaise
        var cashInput by remember { mutableStateOf((uiState.splitCashAmountPaise / 100.0).toString()) }
        var upiInput by remember { mutableStateOf((uiState.splitUpiAmountPaise / 100.0).toString()) }
        var cardInput by remember { mutableStateOf((uiState.splitCardAmountPaise / 100.0).toString()) }

        AlertDialog(
            onDismissRequest = { showSplitDialog = false },
            title = { Text("Split Payment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Total to pay: ${CurrencyUtils.formatPaise(total)}", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = cashInput,
                        onValueChange = { cashInput = it },
                        label = { Text("Cash Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = upiInput,
                        onValueChange = { upiInput = it },
                        label = { Text("UPI Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cardInput,
                        onValueChange = { cardInput = it },
                        label = { Text("Card Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cPaise = CurrencyUtils.parseRupeesInputToPaise(cashInput)
                        val uPaise = CurrencyUtils.parseRupeesInputToPaise(upiInput)
                        val cardPaise = CurrencyUtils.parseRupeesInputToPaise(cardInput)
                        viewModel.updateSplitAmounts(cPaise, uPaise, cardPaise)
                        showSplitDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSplitDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onCustomQty: () -> Unit,
    onDiscount: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.product.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "${CurrencyUtils.formatPaise(item.unitPricePaise)} / ${item.product.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quantity Stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = CurrencyUtils.formatQuantity(item.quantity, item.product.unit),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable { onCustomQty() }
                    )

                    IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                    }
                }

                // Total and Discount
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDiscount, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Discount,
                            contentDescription = "Item Discount",
                            tint = if (item.discountPaise > 0) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = CurrencyUtils.formatPaise(item.netTotalPaise),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductPickerCard(
    product: Product,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAdd() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = "${CurrencyUtils.formatPaise(product.sellingPricePaise)} • Stock: ${product.currentStock} ${product.unit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (product.currentStock <= product.minStockAlert) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
