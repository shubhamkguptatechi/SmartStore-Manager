package com.example.ui.products

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.Product
import com.example.ui.theme.PrimaryGreen
import com.example.util.CurrencyUtils

val SUPPORTED_UNITS = listOf(
    "Piece",
    "Packet",
    "Bottle",
    "Box",
    "Kilogram",
    "Gram",
    "Litre",
    "Millilitre",
    "Dozen"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: Long,
    viewModel: ProductViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val categories by viewModel.categories.collectAsState()
    val allProducts by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()?.name ?: "General") }
    var selectedUnit by remember { mutableStateOf(SUPPORTED_UNITS.first()) }
    var purchasePriceInput by remember { mutableStateOf("") }
    var sellingPriceInput by remember { mutableStateOf("") }
    var stockInput by remember { mutableStateOf("10") }
    var minStockInput by remember { mutableStateOf("5") }

    var isLoaded by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(productId, allProducts.products) {
        if (!isLoaded && productId > 0) {
            val existing = allProducts.products.firstOrNull { it.id == productId }
            if (existing != null) {
                name = existing.name
                code = existing.code
                barcode = existing.barcode ?: ""
                brand = existing.brand ?: ""
                selectedCategory = existing.category
                selectedUnit = existing.unit
                purchasePriceInput = (existing.purchasePricePaise / 100.0).toString()
                sellingPriceInput = (existing.sellingPricePaise / 100.0).toString()
                stockInput = existing.currentStock.toString()
                minStockInput = existing.minStockAlert.toString()
                isLoaded = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("add_edit_product_screen")
    ) {
        TopAppBar(
            title = { Text(if (productId > 0) "Edit Product" else "New Product") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        if (name.isNotBlank() && code.isNotBlank()) {
                            val purchasePaise = CurrencyUtils.parseRupeesInputToPaise(purchasePriceInput)
                            val sellingPaise = CurrencyUtils.parseRupeesInputToPaise(sellingPriceInput)
                            val stock = stockInput.toDoubleOrNull() ?: 0.0
                            val minStock = minStockInput.toDoubleOrNull() ?: 5.0

                            val prod = Product(
                                id = if (productId > 0) productId else 0L,
                                name = name.trim(),
                                code = code.trim(),
                                barcode = barcode.trim().ifBlank { null },
                                category = selectedCategory,
                                brand = brand.trim().ifBlank { null },
                                purchasePricePaise = purchasePaise,
                                sellingPricePaise = sellingPaise,
                                currentStock = stock,
                                unit = selectedUnit,
                                minStockAlert = minStock
                            )
                            viewModel.saveProduct(prod, isNew = (productId <= 0), onDone = onBack)
                        }
                    },
                    modifier = Modifier.testTag("save_product_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save Product", tint = PrimaryGreen)
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name *") },
                placeholder = { Text("e.g. Basmati Rice 1kg") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("product_name_input")
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Product Code / SKU *") },
                    placeholder = { Text("e.g. BR-1KG") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("product_code_input")
                )
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode (Optional)") },
                    placeholder = { Text("e.g. 890123456701") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Brand / Manufacturer (Optional)") },
                placeholder = { Text("e.g. Fortune, Amul, Tata") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Category & Add Category Button
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat.name
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = { showAddCategoryDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add New Category", tint = PrimaryGreen)
                }
            }

            // Unit Selector
            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedUnit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Measurement Unit *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false }
                ) {
                    SUPPORTED_UNITS.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = {
                                selectedUnit = unit
                                unitExpanded = false
                            }
                        )
                    }
                }
            }

            // Pricing Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = purchasePriceInput,
                    onValueChange = { purchasePriceInput = it },
                    label = { Text("Cost Price (₹) *") },
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("product_cost_input")
                )
                OutlinedTextField(
                    value = sellingPriceInput,
                    onValueChange = { sellingPriceInput = it },
                    label = { Text("Selling Price (₹) *") },
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("product_selling_input")
                )
            }

            // Stock & Alert
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = stockInput,
                    onValueChange = { stockInput = it },
                    label = { Text("Current Stock *") },
                    suffix = { Text(selectedUnit) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("product_stock_input")
                )
                OutlinedTextField(
                    value = minStockInput,
                    onValueChange = { minStockInput = it },
                    label = { Text("Low Stock Alert *") },
                    suffix = { Text(selectedUnit) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && code.isNotBlank()) {
                        val purchasePaise = CurrencyUtils.parseRupeesInputToPaise(purchasePriceInput)
                        val sellingPaise = CurrencyUtils.parseRupeesInputToPaise(sellingPriceInput)
                        val stock = stockInput.toDoubleOrNull() ?: 0.0
                        val minStock = minStockInput.toDoubleOrNull() ?: 5.0

                        val prod = Product(
                            id = if (productId > 0) productId else 0L,
                            name = name.trim(),
                            code = code.trim(),
                            barcode = barcode.trim().ifBlank { null },
                            category = selectedCategory,
                            brand = brand.trim().ifBlank { null },
                            purchasePricePaise = purchasePaise,
                            sellingPricePaise = sellingPaise,
                            currentStock = stock,
                            unit = selectedUnit,
                            minStockAlert = minStock
                        )
                        viewModel.saveProduct(prod, isNew = (productId <= 0), onDone = onBack)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (productId > 0) "Update Product" else "Save Product")
            }
        }
    }

    if (showAddCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Category") },
            text = {
                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCatName.isNotBlank()) {
                            viewModel.addCategory(newCatName.trim())
                            selectedCategory = newCatName.trim()
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) { Text("Cancel") }
            }
        )
    }
}
