package com.example.ui.returns

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.WarningAmber
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReturnScreen(
    viewModel: SalesReturnViewModel,
    onBack: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    BackHandler { onBack() }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSale by viewModel.selectedSale.collectAsState()
    val saleItems by viewModel.saleItems.collectAsState()
    val refundMethod by viewModel.refundMethod.collectAsState()
    val returnReason by viewModel.returnReason.collectAsState()
    val pastReturns by viewModel.allReturns.collectAsState()

    var searchInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { msg ->
            onShowSnackbar(msg)
        }
    }

    val totalRefundPaise = saleItems.sumOf { it.refundAmountPaise }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sales_return_screen")
    ) {
        TopAppBar(
            title = { Text("Customer Sales Returns & Refunds") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Receipt Search Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "1. Find Original Sale Receipt",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchInput,
                            onValueChange = { searchInput = it },
                            label = { Text("Receipt Number") },
                            placeholder = { Text("e.g. SSM-20261109-0001") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("return_search_input")
                        )

                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                        Button(
                            onClick = {
                                if (searchInput.isNotBlank()) {
                                    viewModel.searchSale(searchInput.trim())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                            Text("Find")
                        }
                    }
                }
            }

            // If sale found: display items with quantity stepper and resalable switch
            selectedSale?.let { sale ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Receipt: ${sale.receiptNumber}", fontWeight = FontWeight.Bold)
                                Text(
                                    "Billed: ${DateUtils.formatDateTime(sale.saleDate)} • Mode: ${sale.paymentMethod}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                CurrencyUtils.formatPaise(sale.finalTotalPaise),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryGreen)
                            )
                        }

                        Divider()

                        Text("2. Select Items to Return:", fontWeight = FontWeight.Bold)

                        saleItems.forEach { draft ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(draft.saleItem.productName, fontWeight = FontWeight.Bold)
                                            Text(
                                                "Sold: ${CurrencyUtils.formatQuantity(draft.saleItem.quantity, draft.saleItem.unit)} @ ${CurrencyUtils.formatPaise(draft.saleItem.unitPricePaise)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Return Qty Controls
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Button(
                                                onClick = {
                                                    viewModel.updateReturnQuantity(draft.saleItem.id, draft.returnQuantity - 1.0)
                                                },
                                                contentPadding = PaddingValues(0.dp),
                                                modifier = Modifier.size(32.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                                            ) {
                                                Text("-", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                            }

                                            Text(
                                                text = CurrencyUtils.formatQuantity(draft.returnQuantity, draft.saleItem.unit),
                                                modifier = Modifier.padding(horizontal = 8.dp),
                                                fontWeight = FontWeight.Bold
                                            )

                                            Button(
                                                onClick = {
                                                    viewModel.updateReturnQuantity(draft.saleItem.id, draft.returnQuantity + 1.0)
                                                },
                                                contentPadding = PaddingValues(0.dp),
                                                modifier = Modifier.size(32.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                            ) {
                                                Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    if (draft.returnQuantity > 0.0) {
                                        Divider()
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = if (draft.isResalable) "Resalable (Return to stock)" else "Damaged (Scrap - Do NOT restock)",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (draft.isResalable) PrimaryGreen else ErrorRed
                                                    )
                                                )
                                                Text(
                                                    text = "Refund: ${CurrencyUtils.formatPaise(draft.refundAmountPaise)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            Switch(
                                                checked = draft.isResalable,
                                                onCheckedChange = { viewModel.toggleResalable(draft.saleItem.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Divider()

                        // Refund Settings
                        Text("3. Refund Options:", fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Refund Amount:", fontWeight = FontWeight.Bold)
                            Text(
                                text = CurrencyUtils.formatPaise(totalRefundPaise),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ErrorRed)
                            )
                        }

                        Text("Refund Payment Method:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("CASH", "UPI", "CARD").forEach { m ->
                                FilterChip(
                                    selected = refundMethod == m,
                                    onClick = { viewModel.setRefundMethod(m) },
                                    label = { Text(m) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = returnReason,
                            onValueChange = { viewModel.setReturnReason(it) },
                            label = { Text("Reason for Return") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                viewModel.processReturn {
                                    searchInput = ""
                                }
                            },
                            enabled = totalRefundPaise > 0L,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("process_return_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AssignmentReturn, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text("Process Return & Issue Refund")
                        }
                    }
                }
            }

            // Return History Section
            if (pastReturns.isNotEmpty()) {
                Text("Return Records History", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                pastReturns.take(5).forEach { ret ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Orig Receipt: ${ret.receiptNumber}", fontWeight = FontWeight.Bold)
                                Text(
                                    "${DateUtils.formatDateTime(ret.returnDate)} • Refund via ${ret.paymentMethod}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Reason: ${ret.reason}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "-${CurrencyUtils.formatPaise(ret.refundAmountPaise)}",
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                        }
                    }
                }
            }
        }
    }
}
