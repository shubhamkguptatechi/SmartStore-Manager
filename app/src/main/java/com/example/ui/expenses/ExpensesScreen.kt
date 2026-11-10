package com.example.ui.expenses

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.Expense
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryGreen
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsState()
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    var isNewExpense by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { msg ->
            onShowSnackbar(msg)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("expenses_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Store Expenses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )

            // Category Filter Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                items(DEFAULT_EXPENSE_CATEGORIES) { cat ->
                    FilterChip(
                        selected = uiState.selectedCategory == cat,
                        onClick = { viewModel.setSelectedCategory(cat) },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Expenses (${uiState.selectedCategory}):", fontWeight = FontWeight.Bold)
                    Text(
                        text = CurrencyUtils.formatPaise(uiState.totalExpensesPaise),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ErrorRed)
                    )
                }
            }

            if (uiState.expenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Text("No expenses logged under this category")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.expenses, key = { it.id }) { expense ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(expense.category, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        text = "${DateUtils.formatDate(expense.expenseDate)} • Via ${expense.paymentMethod}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (expense.notes.isNotBlank()) {
                                        Text(
                                            text = expense.notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = CurrencyUtils.formatPaise(expense.amountPaise),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ErrorRed)
                                    )
                                    IconButton(onClick = {
                                        expenseToEdit = expense
                                        isNewExpense = false
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { viewModel.deleteExpense(expense) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                expenseToEdit = Expense(category = "Rent", amountPaise = 0L)
                isNewExpense = true
            },
            containerColor = PrimaryGreen,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_expense_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Expense")
        }
    }

    // Add / Edit Expense Dialog
    expenseToEdit?.let { expense ->
        var category by remember { mutableStateOf(expense.category) }
        var amountInput by remember { mutableStateOf(if (expense.amountPaise > 0) (expense.amountPaise / 100.0).toString() else "") }
        var paymentMethod by remember { mutableStateOf(expense.paymentMethod) }
        var notes by remember { mutableStateOf(expense.notes) }
        var catDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { expenseToEdit = null },
            title = { Text(if (isNewExpense) "Add Expense" else "Edit Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = catDropdownExpanded,
                        onExpandedChange = { catDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = catDropdownExpanded,
                            onDismissRequest = { catDropdownExpanded = false }
                        ) {
                            DEFAULT_EXPENSE_CATEGORIES.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        catDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount (₹) *") },
                        prefix = { Text("₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Payment Method selector
                    Text("Payment Method:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Description / Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val paise = CurrencyUtils.parseRupeesInputToPaise(amountInput)
                        if (paise > 0) {
                            val updated = expense.copy(
                                category = category,
                                amountPaise = paise,
                                paymentMethod = paymentMethod,
                                notes = notes.trim()
                            )
                            viewModel.saveExpense(updated, isNewExpense) {
                                expenseToEdit = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToEdit = null }) { Text("Cancel") }
            }
        )
    }
}
