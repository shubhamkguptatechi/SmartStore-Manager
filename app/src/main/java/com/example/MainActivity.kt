package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.data.local.AppDatabase
import com.example.data.local.BackupManager
import com.example.data.local.StorePreferences
import com.example.data.repository.StoreRepository
import com.example.ui.cashregister.CashRegisterViewModel
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.expenses.ExpenseViewModel
import com.example.ui.more.SettingsViewModel
import com.example.ui.navigation.AppNavHost
import com.example.ui.products.ProductViewModel
import com.example.ui.purchases.PurchaseViewModel
import com.example.ui.reports.ReportsViewModel
import com.example.ui.returns.SalesReturnViewModel
import com.example.ui.sales.SalesViewModel
import com.example.ui.theme.SmartStoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val preferences = StorePreferences(applicationContext)
        val backupManager = BackupManager(applicationContext, database)
        val repository = StoreRepository(database)

        setContent {
            val settings by preferences.storeSettings.collectAsState(initial = com.example.data.local.StoreSettings())

            val systemInDark = isSystemInDarkTheme()
            val isDarkTheme = when (settings.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> systemInDark
            }

            val dashboardViewModel = remember { DashboardViewModel(repository) }
            val salesViewModel = remember { SalesViewModel(repository, preferences) }
            val productViewModel = remember { ProductViewModel(repository) }
            val purchaseViewModel = remember { PurchaseViewModel(repository) }
            val expenseViewModel = remember { ExpenseViewModel(repository) }
            val cashRegisterViewModel = remember { CashRegisterViewModel(repository) }
            val salesReturnViewModel = remember { SalesReturnViewModel(repository) }
            val reportsViewModel = remember { ReportsViewModel(repository) }
            val settingsViewModel = remember { SettingsViewModel(preferences, backupManager) }

            SmartStoreTheme(darkTheme = isDarkTheme) {
                AppNavHost(
                    dashboardViewModel = dashboardViewModel,
                    salesViewModel = salesViewModel,
                    productViewModel = productViewModel,
                    purchaseViewModel = purchaseViewModel,
                    expenseViewModel = expenseViewModel,
                    cashRegisterViewModel = cashRegisterViewModel,
                    salesReturnViewModel = salesReturnViewModel,
                    reportsViewModel = reportsViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
