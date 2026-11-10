package com.example.ui.sales

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StoreSettings
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import com.example.ui.theme.PrimaryGreen
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.ReceiptPrinter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptPreviewScreen(
    saleId: Long,
    viewModel: SalesViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var sale by remember { mutableStateOf<Sale?>(null) }
    var items by remember { mutableStateOf<List<SaleItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(if (settings.receiptFormat == "A4") 1 else 0) } // 0: 58mm, 1: A4

    LaunchedEffect(saleId) {
        val details = viewModel.getSaleDetails(saleId)
        sale = details.first
        items = details.second
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("receipt_preview_screen")
    ) {
        TopAppBar(
            title = { Text("Receipt Preview") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                sale?.let { s ->
                    IconButton(
                        onClick = {
                            ReceiptPrinter.printReceipt(
                                context = context,
                                sale = s,
                                items = items,
                                settings = settings,
                                isThermal58mm = (selectedTab == 0)
                            )
                        },
                        modifier = Modifier.testTag("print_receipt_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print Receipt", tint = PrimaryGreen)
                    }
                }
            }
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("58mm Thermal") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("A4 Invoice") }
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else if (sale == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sale record not found")
            }
        } else {
            val currentSale = sale!!
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (selectedTab == 0) {
                    // 58mm Thermal Receipt Card
                    Card(
                        modifier = Modifier
                            .width(320.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = settings.storeName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Black
                                ),
                                textAlign = TextAlign.Center
                            )
                            if (settings.storeAddress.isNotBlank()) {
                                Text(
                                    text = settings.storeAddress,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.DarkGray),
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (settings.storePhone.isNotBlank()) {
                                Text(
                                    text = "Tel: ${settings.storePhone}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.DarkGray),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "--------------------------------",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.Gray)
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Receipt: ${currentSale.receiptNumber}", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.Black))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Date: ${DateUtils.formatReceiptDateTime(currentSale.saleDate)}", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.Black))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pay Mode: ${currentSale.paymentMethod}", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.Black))
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "--------------------------------",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.Gray)
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // Itemized rows
                            items.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.productName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, color = Color.Black),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = CurrencyUtils.formatPaise(item.totalPaise, false),
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.Black)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "  ${CurrencyUtils.formatQuantity(item.quantity, item.unit)} @ ${CurrencyUtils.formatPaise(item.unitPricePaise, false)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = Color.DarkGray)
                                    )
                                    if (item.discountPaise > 0) {
                                        Text(
                                            text = "-${CurrencyUtils.formatPaise(item.discountPaise, false)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = Color.DarkGray)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Text(
                                text = "--------------------------------",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.Gray)
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.Black))
                                Text(CurrencyUtils.formatPaise(currentSale.subtotalPaise), style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.Black))
                            }

                            val totalDiscount = currentSale.itemDiscountPaise + currentSale.billDiscountPaise
                            if (totalDiscount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount:", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.Black))
                                    Text("-${CurrencyUtils.formatPaise(totalDiscount)}", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.Black))
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("FINAL TOTAL:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.Black))
                                Text(CurrencyUtils.formatPaise(currentSale.finalTotalPaise), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.Black))
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "================================",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.Gray)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = settings.receiptFooterMessage,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = Color.DarkGray),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // A4 Invoice Preview Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = settings.storeName,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.Black)
                                    )
                                    Text(settings.storeAddress, style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
                                    Text("Ph: ${settings.storePhone}", style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "TAX INVOICE",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                    )
                                    Text("Invoice #: ${currentSale.receiptNumber}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                                    Text("Date: ${DateUtils.formatReceiptDateTime(currentSale.saleDate)}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                                    Text("Payment: ${currentSale.paymentMethod}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9))
                                    .padding(vertical = 6.dp, horizontal = 4.dp)
                            ) {
                                Text("#", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.Black), modifier = Modifier.width(24.dp))
                                Text("Item", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.Black), modifier = Modifier.weight(1f))
                                Text("Qty", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.Black), modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                                Text("Rate", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.Black), modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                                Text("Total", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.Black), modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
                            }

                            // Items
                            items.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${idx + 1}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Black), modifier = Modifier.width(24.dp))
                                    Text(item.productName, style = MaterialTheme.typography.bodySmall.copy(color = Color.Black), modifier = Modifier.weight(1f))
                                    Text(CurrencyUtils.formatQuantity(item.quantity, item.unit), style = MaterialTheme.typography.bodySmall.copy(color = Color.Black), modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                                    Text(CurrencyUtils.formatPaise(item.unitPricePaise, false), style = MaterialTheme.typography.bodySmall.copy(color = Color.Black), modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                                    Text(CurrencyUtils.formatPaise(item.totalPaise, false), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Color.Black), modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
                                }
                                Divider(color = Color(0xFFF1F5F9))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Total calculation
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("Subtotal: ${CurrencyUtils.formatPaise(currentSale.subtotalPaise)}", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black))
                                val discount = currentSale.itemDiscountPaise + currentSale.billDiscountPaise
                                if (discount > 0) {
                                    Text("Discount: -${CurrencyUtils.formatPaise(discount)}", style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Final Total: ${CurrencyUtils.formatPaise(currentSale.finalTotalPaise)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = settings.receiptFooterMessage,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Print Button
                Button(
                    onClick = {
                        ReceiptPrinter.printReceipt(
                            context = context,
                            sale = currentSale,
                            items = items,
                            settings = settings,
                            isThermal58mm = (selectedTab == 0)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp)
                        .testTag("action_print_receipt_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedTab == 0) "Print 58mm Thermal" else "Print A4 Invoice")
                }
            }
        }
    }
}
