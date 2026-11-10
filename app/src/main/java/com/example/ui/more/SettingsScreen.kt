package com.example.ui.more

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
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    BackHandler { onBack() }

    val settings by viewModel.settings.collectAsState()

    var storeName by remember { mutableStateOf(settings.storeName) }
    var storeAddress by remember { mutableStateOf(settings.storeAddress) }
    var storePhone by remember { mutableStateOf(settings.storePhone) }
    var allowNegativeStock by remember { mutableStateOf(settings.allowNegativeStock) }
    var lowStockThreshold by remember { mutableStateOf(settings.defaultLowStockThreshold.toString()) }
    var themeMode by remember { mutableStateOf(settings.themeMode) }

    LaunchedEffect(settings) {
        storeName = settings.storeName
        storeAddress = settings.storeAddress
        storePhone = settings.storePhone
        allowNegativeStock = settings.allowNegativeStock
        lowStockThreshold = settings.defaultLowStockThreshold.toString()
        themeMode = settings.themeMode
    }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { msg ->
            onShowSnackbar(msg)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_settings_screen")
    ) {
        TopAppBar(
            title = { Text("App Settings") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        val threshold = lowStockThreshold.toDoubleOrNull() ?: 5.0
                        viewModel.updateSettings(
                            settings.copy(
                                storeName = storeName.trim(),
                                storeAddress = storeAddress.trim(),
                                storePhone = storePhone.trim(),
                                allowNegativeStock = allowNegativeStock,
                                defaultLowStockThreshold = threshold,
                                themeMode = themeMode
                            )
                        )
                    },
                    modifier = Modifier.testTag("save_settings_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save Settings", tint = PrimaryGreen)
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
            // Store Profile Card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Store Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("Store Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("store_name_input")
                    )

                    OutlinedTextField(
                        value = storeAddress,
                        onValueChange = { storeAddress = it },
                        label = { Text("Store Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = storePhone,
                        onValueChange = { storePhone = it },
                        label = { Text("Store Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Theme Selection Card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Theme & Appearance", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Choose application color theme:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = {
                                    themeMode = mode
                                    viewModel.setThemeMode(mode)
                                },
                                label = { Text(mode.lowercase().replaceFirstChar { it.uppercase() }) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Inventory & Billing Preferences
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Inventory & Billing Rules", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Allow Negative Stock", fontWeight = FontWeight.SemiBold)
                            Text("If enabled, POS billing won't block sales when stock reaches zero", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = allowNegativeStock,
                            onCheckedChange = { allowNegativeStock = it }
                        )
                    }

                    Divider()

                    OutlinedTextField(
                        value = lowStockThreshold,
                        onValueChange = { lowStockThreshold = it },
                        label = { Text("Default Low Stock Warning Threshold") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // App Info Card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("SmartStore Manager", fontWeight = FontWeight.Bold)
                    Text("Version 1.0.0 (Completely Offline Production Build)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Currency: Indian Rupees (₹)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("All business data is strictly stored locally on this device.", style = MaterialTheme.typography.labelSmall, color = PrimaryGreen)
                }
            }

            Button(
                onClick = {
                    val threshold = lowStockThreshold.toDoubleOrNull() ?: 5.0
                    viewModel.updateSettings(
                        settings.copy(
                            storeName = storeName.trim(),
                            storeAddress = storeAddress.trim(),
                            storePhone = storePhone.trim(),
                            allowNegativeStock = allowNegativeStock,
                            defaultLowStockThreshold = threshold,
                            themeMode = themeMode
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save All Settings")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    BackHandler { onBack() }

    val settings by viewModel.settings.collectAsState()

    var receiptFormat by remember { mutableStateOf(settings.receiptFormat) }
    var footerMessage by remember { mutableStateOf(settings.receiptFooterMessage) }

    LaunchedEffect(settings) {
        receiptFormat = settings.receiptFormat
        footerMessage = settings.receiptFooterMessage
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("receipt_settings_screen")
    ) {
        TopAppBar(
            title = { Text("Receipt & Printer Settings") },
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
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Default Receipt Layout", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = receiptFormat == "THERMAL_58MM",
                            onClick = { receiptFormat = "THERMAL_58MM" },
                            label = { Text("58mm Thermal (POS)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = receiptFormat == "A4",
                            onClick = { receiptFormat = "A4" },
                            label = { Text("A4 Invoice (Full Page)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Text(
                        text = if (receiptFormat == "THERMAL_58MM") "Optimized for thermal roll printers (58mm width). Prints compact itemized slip."
                        else "Optimized for laser/inkjet A4 printers. Generates formal retail tax invoice table.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Receipt Footer Message", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    OutlinedTextField(
                        value = footerMessage,
                        onValueChange = { footerMessage = it },
                        label = { Text("Greeting / Thank You note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.updateSettings(
                        settings.copy(
                            receiptFormat = receiptFormat,
                            receiptFooterMessage = footerMessage.trim()
                        )
                    )
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Receipt Preferences")
            }
        }
    }
}
