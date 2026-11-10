package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.cashregister.CashRegisterScreen
import com.example.ui.cashregister.CashRegisterViewModel
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.expenses.ExpensesScreen
import com.example.ui.expenses.ExpenseViewModel
import com.example.ui.more.BackupRestoreScreen
import com.example.ui.more.MoreMenuScreen
import com.example.ui.more.ReceiptSettingsScreen
import com.example.ui.more.SettingsScreen
import com.example.ui.more.SettingsViewModel
import com.example.ui.products.AddEditProductScreen
import com.example.ui.products.ProductListScreen
import com.example.ui.products.ProductViewModel
import com.example.ui.products.StockMovementScreen
import com.example.ui.purchases.AddPurchaseScreen
import com.example.ui.purchases.PurchaseViewModel
import com.example.ui.purchases.PurchasesScreen
import com.example.ui.purchases.SuppliersScreen
import com.example.ui.reports.ReportsScreen
import com.example.ui.reports.ReportsViewModel
import com.example.ui.returns.SalesReturnScreen
import com.example.ui.returns.SalesReturnViewModel
import com.example.ui.sales.PosBillingScreen
import com.example.ui.sales.ReceiptPreviewScreen
import com.example.ui.sales.SalesHistoryScreen
import com.example.ui.sales.SalesViewModel
import com.example.ui.theme.PrimaryGreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    dashboardViewModel: DashboardViewModel,
    salesViewModel: SalesViewModel,
    productViewModel: ProductViewModel,
    purchaseViewModel: PurchaseViewModel,
    expenseViewModel: ExpenseViewModel,
    cashRegisterViewModel: CashRegisterViewModel,
    salesReturnViewModel: SalesReturnViewModel,
    reportsViewModel: ReportsViewModel,
    settingsViewModel: SettingsViewModel
) {
    var currentTab by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val backStack = remember { mutableStateListOf<Screen>() }

    val currentScreen = backStack.lastOrNull() ?: currentTab
    val isTopLevel = backStack.isEmpty()

    val settings by settingsViewModel.settings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val showSnackbar: (String) -> Unit = { message ->
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    fun navigateTo(screen: Screen) {
        backStack.add(screen)
    }

    fun navigateBack() {
        if (backStack.isNotEmpty()) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    if (!isTopLevel) {
        BackHandler {
            navigateBack()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentTab == Screen.Dashboard,
                        onClick = { currentTab = Screen.Dashboard },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentTab == Screen.Sales,
                        onClick = { currentTab = Screen.Sales },
                        icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Sales") },
                        label = { Text("Sales") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_sales")
                    )

                    NavigationBarItem(
                        selected = currentTab == Screen.Products,
                        onClick = { currentTab = Screen.Products },
                        icon = { Icon(Icons.Default.Inventory2, contentDescription = "Products") },
                        label = { Text("Products") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_products")
                    )

                    NavigationBarItem(
                        selected = currentTab == Screen.Reports,
                        onClick = { currentTab = Screen.Reports },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Reports") },
                        label = { Text("Reports") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_reports")
                    )

                    NavigationBarItem(
                        selected = currentTab == Screen.More,
                        onClick = { currentTab = Screen.More },
                        icon = { Icon(Icons.Default.Menu, contentDescription = "More") },
                        label = { Text("More") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_more")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
                when (screen) {
                    is Screen.Dashboard -> DashboardScreen(
                        viewModel = dashboardViewModel,
                        settings = settings,
                        onNavigateToPos = { currentTab = Screen.Sales },
                        onNavigateToAddProduct = { navigateTo(Screen.AddEditProduct(-1L)) },
                        onNavigateToAddPurchase = { navigateTo(Screen.AddPurchase) },
                        onNavigateToAddExpense = { navigateTo(Screen.Expenses) },
                        onNavigateToInventory = { currentTab = Screen.Products },
                        onNavigateToReceipt = { saleId -> navigateTo(Screen.ReceiptPreview(saleId)) }
                    )

                    is Screen.Sales -> PosBillingScreen(
                        viewModel = salesViewModel,
                        onNavigateToReceipt = { saleId -> navigateTo(Screen.ReceiptPreview(saleId)) },
                        onNavigateToHistory = { navigateTo(Screen.SalesHistory) },
                        onShowSnackbar = showSnackbar
                    )

                    is Screen.Products -> ProductListScreen(
                        viewModel = productViewModel,
                        onNavigateToAddEdit = { id -> navigateTo(Screen.AddEditProduct(id)) },
                        onNavigateToMovements = { id -> navigateTo(Screen.StockMovements(id)) },
                        onShowSnackbar = showSnackbar
                    )

                    is Screen.Reports -> ReportsScreen(
                        viewModel = reportsViewModel,
                        onShowSnackbar = showSnackbar
                    )

                    is Screen.More -> MoreMenuScreen(
                        onNavigateToPurchases = { navigateTo(Screen.Purchases) },
                        onNavigateToSuppliers = { navigateTo(Screen.Suppliers) },
                        onNavigateToExpenses = { navigateTo(Screen.Expenses) },
                        onNavigateToCashRegister = { navigateTo(Screen.CashRegister) },
                        onNavigateToReturns = { navigateTo(Screen.SalesReturns) },
                        onNavigateToMovements = { navigateTo(Screen.StockMovements(-1L)) },
                        onNavigateToReceiptSettings = { navigateTo(Screen.ReceiptSettings) },
                        onNavigateToBackupRestore = { navigateTo(Screen.BackupRestore) },
                        onNavigateToAppSettings = { navigateTo(Screen.AppSettings) }
                    )

                    is Screen.ReceiptPreview -> ReceiptPreviewScreen(
                        saleId = screen.saleId,
                        viewModel = salesViewModel,
                        onBack = { navigateBack() }
                    )

                    is Screen.SalesHistory -> SalesHistoryScreen(
                        viewModel = salesViewModel,
                        onNavigateToReceipt = { saleId -> navigateTo(Screen.ReceiptPreview(saleId)) },
                        onBack = { navigateBack() }
                    )

                    is Screen.AddEditProduct -> AddEditProductScreen(
                        productId = screen.productId,
                        viewModel = productViewModel,
                        onBack = { navigateBack() }
                    )

                    is Screen.StockMovements -> StockMovementScreen(
                        productId = screen.productId,
                        viewModel = productViewModel,
                        onBack = { navigateBack() }
                    )

                    is Screen.Purchases -> PurchasesScreen(
                        viewModel = purchaseViewModel,
                        onNavigateToAddPurchase = { navigateTo(Screen.AddPurchase) },
                        onNavigateToSuppliers = { navigateTo(Screen.Suppliers) },
                        onBack = { navigateBack() },
                        onShowSnackbar = showSnackbar
                    )

                    is Screen.AddPurchase -> AddPurchaseScreen(
                        viewModel = purchaseViewModel,
                        onBack = { navigateBack() }
                    )

                    is Screen.Suppliers -> SuppliersScreen(
                        viewModel = purchaseViewModel,
                        onBack = { navigateBack() }
                    )

                    is Screen.Expenses -> ExpensesScreen(
                        viewModel = expenseViewModel,
                        onBack = { navigateBack() },
                        onShowSnackbar = showSnackbar
                    )

                    is Screen.CashRegister -> CashRegisterScreen(
                        viewModel = cashRegisterViewModel,
                        onBack = { navigateBack() },
                        onShowSnackbar = showSnackbar
                    )

                    is Screen.SalesReturns -> SalesReturnScreen(
                        viewModel = salesReturnViewModel,
                        onBack = { navigateBack() },
                        onShowSnackbar = showSnackbar
                    )

                    is Screen.ReceiptSettings -> ReceiptSettingsScreen(
                        viewModel = settingsViewModel,
                        onBack = { navigateBack() },
                        onShowSnackbar = showSnackbar
                    )

                    is Screen.BackupRestore -> BackupRestoreScreen(
                        viewModel = settingsViewModel,
                        onBack = { navigateBack() },
                        onShowSnackbar = showSnackbar
                    )

                    is Screen.AppSettings -> SettingsScreen(
                        viewModel = settingsViewModel,
                        onBack = { navigateBack() },
                        onShowSnackbar = showSnackbar
                    )
                }
            }
        }
    }
}
