package com.example.ui.purchases

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.Product
import com.example.data.local.entity.Supplier
import com.example.ui.theme.PrimaryGreen
import com.example.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseScreen(
    viewModel: PurchaseViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val suppliers by viewModel.allSuppliers.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val draftItems by viewModel.draftItems.collectAsState()

    var selectedSupplier by remember { mutableStateOf<Supplier?>(suppliers.firstOrNull()) }
    var invoiceNumber by remember { mutableStateOf("") }
    var discountInput by remember { mutableStateOf("0") }
    var amountPaidInput by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var paymentStatus by remember { mutableStateOf("PAID") }
    var notes by remember { mutableStateOf("") }

    var supplierDropdownExpanded by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }

    val grossTotalPaise = draftItems.sumOf { it.totalPaise }
    val discountPaise = CurrencyUtils.parseRupeesInputToPaise(discountInput)
    val netTotalPaise = maxOf(0L, grossTotalPaise - discountPaise)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("add_purchase_screen")
    ) {
        TopAppBar(
            title = { Text("Record Purchase") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Supplier Picker
            ExposedDropdownMenuBox(
                expanded = supplierDropdownExpanded,
                onExpandedChange = { supplierDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedSupplier?.name ?: "Select Supplier",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Supplier *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = supplierDropdownExpanded,
                    onDismissRequest = { supplierDropdownExpanded = false }
                ) {
                    suppliers.forEach { supp ->
                        DropdownMenuItem(
                            text = { Text(supp.name) },
                            onClick = {
                                selectedSupplier = supp
                                supplierDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = invoiceNumber,
                onValueChange = { invoiceNumber = it },
                label = { Text("Supplier Invoice Number") },
                placeholder = { Text("e.g. INV-9843") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Items in Purchase
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Purchased Items (${draftItems.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                OutlinedButton(
                    onClick = { showAddItemDialog = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Product")
                }
            }

            if (draftItems.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No products added to this purchase yet. Tap 'Add Product' above.")
                    }
                }
            } else {
                draftItems.forEach { item ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.name, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${CurrencyUtils.formatQuantity(item.quantity, item.product.unit)} @ ${CurrencyUtils.formatPaise(item.purchaseRatePaise)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = CurrencyUtils.formatPaise(item.totalPaise),
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                                IconButton(onClick = { viewModel.removeDraftItem(item.product.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            // Totals and Payments
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Total:")
                        Text(CurrencyUtils.formatPaise(grossTotalPaise), fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = discountInput,
                        onValueChange = { discountInput = it },
                        label = { Text("Supplier Discount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Payable:", fontWeight = FontWeight.Bold)
                        Text(CurrencyUtils.formatPaise(netTotalPaise), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryGreen))
                    }

                    OutlinedTextField(
                        value = if (amountPaidInput.isEmpty()) (netTotalPaise / 100.0).toString() else amountPaidInput,
                        onValueChange = { amountPaidInput = it },
                        label = { Text("Amount Paid (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Payment Method & Status
                    Text("Payment Method:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("CASH", "UPI", "CARD").forEach { m ->
                            FilterChip(
                                selected = paymentMethod == m,
                                onClick = { paymentMethod = m },
                                label = { Text(m) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Text("Payment Status:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("PAID", "PARTIALLY_PAID", "UNPAID").forEach { s ->
                            FilterChip(
                                selected = paymentStatus == s,
                                onClick = { paymentStatus = s },
                                label = { Text(s.replace("_", " ")) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val supp = selectedSupplier
                    if (supp != null && draftItems.isNotEmpty()) {
                        val paidPaise = CurrencyUtils.parseRupeesInputToPaise(
                            if (amountPaidInput.isEmpty()) (netTotalPaise / 100.0).toString() else amountPaidInput
                        )
                        viewModel.createPurchase(
                            supplier = supp,
                            invoiceNumber = invoiceNumber.trim().ifBlank { null },
                            discountPaise = discountPaise,
                            amountPaidPaise = paidPaise,
                            paymentStatus = paymentStatus,
                            paymentMethod = paymentMethod,
                            notes = notes.trim().ifBlank { null },
                            onSuccess = onBack
                        )
                    }
                },
                enabled = selectedSupplier != null && draftItems.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_purchase_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete & Update Stock")
            }
        }
    }

    // Add Product to Purchase Dialog
    if (showAddItemDialog) {
        var selectedProduct by remember { mutableStateOf(allProducts.firstOrNull()) }
        var prodDropdownExpanded by remember { mutableStateOf(false) }
        var quantityInput by remember { mutableStateOf("10") }
        var rateInput by remember { mutableStateOf(if (selectedProduct != null) (selectedProduct!!.purchasePricePaise / 100.0).toString() else "0") }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("Add Inward Product") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = prodDropdownExpanded,
                        onExpandedChange = { prodDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedProduct?.name ?: "Select Product",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Product") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = prodDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = prodDropdownExpanded,
                            onDismissRequest = { prodDropdownExpanded = false }
                        ) {
                            allProducts.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text("${p.name} (${p.unit})") },
                                    onClick = {
                                        selectedProduct = p
                                        rateInput = (p.purchasePricePaise / 100.0).toString()
                                        prodDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        label = { Text("Quantity (${selectedProduct?.unit ?: ""})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = rateInput,
                        onValueChange = { rateInput = it },
                        label = { Text("Purchase Rate per Unit (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = selectedProduct
                        val qty = quantityInput.toDoubleOrNull() ?: 1.0
                        val ratePaise = CurrencyUtils.parseRupeesInputToPaise(rateInput)
                        if (p != null && qty > 0) {
                            viewModel.addDraftItem(p, qty, ratePaise)
                            showAddItemDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Add to Purchase")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) { Text("Cancel") }
            }
        )
    }
}
