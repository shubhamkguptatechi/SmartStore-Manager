package com.example.ui.navigation

sealed class Screen(val title: String) {
    // Top-Level Bottom Nav Tabs
    object Dashboard : Screen("Dashboard")
    object Sales : Screen("POS Billing")
    object Products : Screen("Products & Stock")
    object Reports : Screen("Reports")
    object More : Screen("More")

    // Sub-screens
    data class ReceiptPreview(val saleId: Long) : Screen("Receipt Preview")
    object SalesHistory : Screen("Sales History")
    data class AddEditProduct(val productId: Long = -1L) : Screen("Product Details")
    data class StockMovements(val productId: Long = -1L) : Screen("Stock Movements")
    object Purchases : Screen("Purchases")
    object AddPurchase : Screen("New Purchase")
    object Suppliers : Screen("Suppliers")
    object Expenses : Screen("Expenses")
    object CashRegister : Screen("Cash Register")
    object SalesReturns : Screen("Sales Returns")
    object ReceiptSettings : Screen("Receipt Settings")
    object BackupRestore : Screen("Backup & Restore")
    object AppSettings : Screen("App Settings")
}
