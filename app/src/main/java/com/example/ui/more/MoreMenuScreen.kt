package com.example.ui.more

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuScreen(
    onNavigateToPurchases: () -> Unit,
    onNavigateToSuppliers: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToCashRegister: () -> Unit,
    onNavigateToReturns: () -> Unit,
    onNavigateToMovements: () -> Unit,
    onNavigateToReceiptSettings: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    onNavigateToAppSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("more_menu_screen")
    ) {
        TopAppBar(
            title = { Text("More Features & Management") }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SectionHeader("Operations & Inventory")
            }

            item {
                MoreMenuItem(
                    title = "Purchases",
                    subtitle = "Inward stock from suppliers, record purchase invoices",
                    icon = Icons.Default.ShoppingBag,
                    iconColor = Color(0xFF7C3AED),
                    onClick = onNavigateToPurchases
                )
            }

            item {
                MoreMenuItem(
                    title = "Suppliers Directory",
                    subtitle = "Manage wholesale distributors, contacts, and balances",
                    icon = Icons.Default.People,
                    iconColor = Color(0xFF0284C7),
                    onClick = onNavigateToSuppliers
                )
            }

            item {
                MoreMenuItem(
                    title = "Store Expenses",
                    subtitle = "Rent, electricity, salaries, packaging, and operating bills",
                    icon = Icons.Default.Payments,
                    iconColor = Color(0xFFEA580C),
                    onClick = onNavigateToExpenses
                )
            }

            item {
                MoreMenuItem(
                    title = "Cash Register",
                    subtitle = "Opening cash, sales, drawer reconciliation, and cash variances",
                    icon = Icons.Default.PointOfSale,
                    iconColor = PrimaryGreen,
                    onClick = onNavigateToCashRegister
                )
            }

            item {
                MoreMenuItem(
                    title = "Sales Returns & Refunds",
                    subtitle = "Customer returns, refunds, damaged vs resalable restock",
                    icon = Icons.Default.AssignmentReturn,
                    iconColor = Color(0xFFE11D48),
                    onClick = onNavigateToReturns
                )
            }

            item {
                MoreMenuItem(
                    title = "Stock Movement History",
                    subtitle = "Complete audit trail of sales, purchases, and adjustments",
                    icon = Icons.Default.History,
                    iconColor = Color(0xFF475569),
                    onClick = onNavigateToMovements
                )
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader("Settings & Data Management")
            }

            item {
                MoreMenuItem(
                    title = "Receipt Settings",
                    subtitle = "58mm Thermal & A4 format, store header, footer message",
                    icon = Icons.Default.Receipt,
                    iconColor = Color(0xFF0D9488),
                    onClick = onNavigateToReceiptSettings
                )
            }

            item {
                MoreMenuItem(
                    title = "Backup & Restore",
                    subtitle = "Full offline JSON export, local storage backup & restore",
                    icon = Icons.Default.Backup,
                    iconColor = Color(0xFF2563EB),
                    onClick = onNavigateToBackupRestore
                )
            }

            item {
                MoreMenuItem(
                    title = "App Settings",
                    subtitle = "Store profile, Light / Dark theme, stock thresholds",
                    icon = Icons.Default.Settings,
                    iconColor = Color(0xFF4B5563),
                    onClick = onNavigateToAppSettings
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        ),
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    )
}

@Composable
private fun MoreMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
