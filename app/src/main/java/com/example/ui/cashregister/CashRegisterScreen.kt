package com.example.ui.cashregister

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.WarningAmber
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashRegisterScreen(
    viewModel: CashRegisterViewModel,
    onBack: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsState()
    val activeSession = uiState.activeSession
    val summary = uiState.summary

    var openingCashInput by remember { mutableStateOf("1000") }
    var openingNotes by remember { mutableStateOf("") }

    var countedCashInput by remember { mutableStateOf("") }
    var closingNotes by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { msg ->
            onShowSnackbar(msg)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("cash_register_screen")
    ) {
        TopAppBar(
            title = { Text("Cash Drawer & Register") },
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
            if (activeSession == null) {
                // Open Register Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(
                                text = "Open New Cash Drawer Session",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = "Count physical cash in register drawer to start today's session:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = openingCashInput,
                            onValueChange = { openingCashInput = it },
                            label = { Text("Opening Cash Float (₹) *") },
                            prefix = { Text("₹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("opening_cash_input")
                        )

                        OutlinedTextField(
                            value = openingNotes,
                            onValueChange = { openingNotes = it },
                            label = { Text("Notes (Optional)") },
                            placeholder = { Text("e.g. 5x100, 10x50 notes") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val paise = CurrencyUtils.parseRupeesInputToPaise(openingCashInput)
                                viewModel.openRegister(paise, openingNotes.ifBlank { null })
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("open_register_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Open Register Drawer")
                        }
                    }
                }
            } else {
                // Active Register Session Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PointOfSale, contentDescription = null, tint = PrimaryGreen)
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = "Register Session: Active",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Surface(shape = RoundedCornerShape(8.dp), color = PrimaryGreen.copy(alpha = 0.15f)) {
                                Text(
                                    text = "OPEN",
                                    color = PrimaryGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        Text(
                            text = "Opened: ${DateUtils.formatDateTime(activeSession.openedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        // Financial breakdown for physical cash
                        CashFlowRow("Opening Cash:", CurrencyUtils.formatPaise(summary.openingCashPaise))
                        CashFlowRow("+ Cash Sales:", CurrencyUtils.formatPaise(summary.cashSalesPaise), color = PrimaryGreen)
                        CashFlowRow("- Cash Refunds:", CurrencyUtils.formatPaise(summary.cashRefundsPaise), color = ErrorRed)
                        CashFlowRow("- Cash Expenses:", CurrencyUtils.formatPaise(summary.cashExpensesPaise), color = ErrorRed)

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Expected Drawer Cash:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = CurrencyUtils.formatPaise(summary.expectedClosingCashPaise),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryGreen)
                            )
                        }

                        // Digital Transactions Note
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Digital Non-Cash Transactions (Direct to Bank):",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "UPI: ${CurrencyUtils.formatPaise(summary.nonCashSalesUpiPaise)} • Card: ${CurrencyUtils.formatPaise(summary.nonCashSalesCardPaise)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 6.dp))

                        // Close Session Form
                        Text(
                            text = "Close Drawer & Count Cash",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        OutlinedTextField(
                            value = countedCashInput,
                            onValueChange = { countedCashInput = it },
                            label = { Text("Actual Counted Cash in Drawer (₹) *") },
                            prefix = { Text("₹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("counted_cash_input")
                        )

                        val countedPaise = CurrencyUtils.parseRupeesInputToPaise(countedCashInput)
                        if (countedCashInput.isNotBlank()) {
                            val diff = countedPaise - summary.expectedClosingCashPaise
                            val isMatch = diff == 0L
                            val isOver = diff > 0L
                            Text(
                                text = if (isMatch) "Exact match! No variance."
                                else if (isOver) "Cash Over by ${CurrencyUtils.formatPaise(diff)}"
                                else "Cash Short by ${CurrencyUtils.formatPaise(Math.abs(diff))}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMatch) PrimaryGreen else if (isOver) WarningAmber else ErrorRed
                                )
                            )
                        }

                        OutlinedTextField(
                            value = closingNotes,
                            onValueChange = { closingNotes = it },
                            label = { Text("Closing Notes / Reason for variance") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                viewModel.closeRegister(activeSession, countedPaise, closingNotes.ifBlank { null })
                            },
                            enabled = countedCashInput.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("close_register_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text("Reconcile & Close Register")
                        }
                    }
                }
            }

            // Past Closed Sessions
            if (uiState.pastSessions.isNotEmpty()) {
                Text(
                    text = "Past Reconciled Sessions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                uiState.pastSessions.take(5).forEach { session ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = DateUtils.formatDate(session.openedAt),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Closed at ${DateUtils.formatTime(session.closedAt ?: session.openedAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Opening: ${CurrencyUtils.formatPaise(session.openingCashPaise)}", style = MaterialTheme.typography.bodySmall)
                                Text("Counted: ${CurrencyUtils.formatPaise(session.actualClosingCashPaise ?: 0L)}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CashFlowRow(label: String, value: String, color: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = color))
    }
}
