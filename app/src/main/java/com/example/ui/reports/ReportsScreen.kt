package com.example.ui.reports

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.dashboard.DateFilter
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.WarningAmber
import com.example.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    val rep by viewModel.reportState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.messages.collect { msg ->
            onShowSnackbar(msg)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen")
    ) {
        TopAppBar(
            title = { Text("Reports & Business Intelligence") },
            actions = {
                Button(
                    onClick = { viewModel.exportReportCsv(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .testTag("export_csv_btn")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export CSV")
                }
            }
        )

        // Date Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DateFilter.values()) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { viewModel.setFilter(filter) },
                    label = { Text(filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profit & Loss Statement Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShowChart, contentDescription = null, tint = PrimaryGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Profit & Loss Statement (${selectedFilter.label})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Divider()

                    ReportRow("Gross Sales (${rep.totalTransactions} bills):", CurrencyUtils.formatPaise(rep.grossSalesPaise))
                    ReportRow("Less: Returns & Refunds:", "-${CurrencyUtils.formatPaise(rep.totalReturnsPaise)}", color = ErrorRed)
                    ReportRow("= Net Sales Revenue:", CurrencyUtils.formatPaise(rep.netSalesPaise), isBold = true)
                    ReportRow("Less: Cost of Goods Sold (COGS):", "-${CurrencyUtils.formatPaise(rep.cogsPaise)}", color = Color.DarkGray)

                    Divider()

                    ReportRow(
                        label = "= GROSS PROFIT:",
                        value = CurrencyUtils.formatPaise(rep.grossProfitPaise),
                        isBold = true,
                        color = SuccessEmerald,
                        fontSize = 16.sp
                    )
                    ReportRow("Less: Total Operating Expenses:", "-${CurrencyUtils.formatPaise(rep.operatingExpensesPaise)}", color = ErrorRed)

                    Divider()

                    ReportRow(
                        label = "= NET PROFIT:",
                        value = CurrencyUtils.formatPaise(rep.netProfitPaise),
                        isBold = true,
                        color = if (rep.netProfitPaise >= 0) PrimaryGreen else ErrorRed,
                        fontSize = 18.sp
                    )
                }
            }

            // Payment Methods Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Revenue by Payment Method",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    ReportRow("Cash Payments (Drawer):", CurrencyUtils.formatPaise(rep.cashSalesPaise))
                    ReportRow("UPI / QR Payments (Bank):", CurrencyUtils.formatPaise(rep.upiSalesPaise))
                    ReportRow("Card / POS Machine:", CurrencyUtils.formatPaise(rep.cardSalesPaise))
                }
            }

            // Operating Expenses Breakdown
            if (rep.expenseCategories.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Operating Expenses by Category",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        rep.expenseCategories.forEach { cat ->
                            ReportRow(cat.category, CurrencyUtils.formatPaise(cat.totalAmountPaise))
                        }
                    }
                }
            }

            // Top-Selling Products
            if (rep.topSelling.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Top Selling Products",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        rep.topSelling.forEachIndexed { idx, top ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${idx + 1}. ${top.productName}", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "${top.totalQty} sold • ${CurrencyUtils.formatPaise(top.totalRevenuePaise)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = PrimaryGreen)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontSize = fontSize
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
                color = color,
                fontSize = fontSize
            )
        )
    }
}
