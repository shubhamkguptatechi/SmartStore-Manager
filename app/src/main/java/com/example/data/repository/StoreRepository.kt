package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.dao.TopSellingProduct
import com.example.data.local.entity.Category
import com.example.data.local.entity.CashRegisterSession
import com.example.data.local.entity.Expense
import com.example.data.local.entity.Product
import com.example.data.local.entity.Purchase
import com.example.data.local.entity.PurchaseItem
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import com.example.data.local.entity.SalesReturn
import com.example.data.local.entity.SalesReturnItem
import com.example.data.local.entity.StockMovement
import com.example.data.local.entity.Supplier
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import java.util.Locale

data class CartItem(
    val product: Product,
    var quantity: Double = 1.0,
    var discountPaise: Long = 0
) {
    val unitPricePaise: Long get() = product.sellingPricePaise
    val grossTotalPaise: Long get() = (unitPricePaise * quantity).toLong()
    val netTotalPaise: Long get() = maxOf(0L, grossTotalPaise - discountPaise)
    val totalCostPaise: Long get() = (product.purchasePricePaise * quantity).toLong()
}

data class FinancialSummary(
    val totalSalesPaise: Long = 0,
    val totalCostPaise: Long = 0,
    val grossProfitPaise: Long = 0,
    val totalExpensesPaise: Long = 0,
    val netProfitPaise: Long = 0,
    val totalTransactions: Int = 0,
    val totalRefundsPaise: Long = 0,
    val cashSalesPaise: Long = 0,
    val upiSalesPaise: Long = 0,
    val cardSalesPaise: Long = 0
)

class StoreRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val categoryDao = database.categoryDao()
    private val supplierDao = database.supplierDao()
    private val purchaseDao = database.purchaseDao()
    private val saleDao = database.saleDao()
    private val salesReturnDao = database.salesReturnDao()
    private val expenseDao = database.expenseDao()
    private val stockMovementDao = database.stockMovementDao()
    private val cashRegisterDao = database.cashRegisterDao()

    // Products
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    val lowStockCount: Flow<Int> = productDao.getLowStockCount()
    val totalProductsCount: Flow<Int> = productDao.getTotalProductsCount()
    val totalInventoryValuePaise: Flow<Long?> = productDao.getTotalInventoryValuePaise()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)
    fun getProductById(id: Long): Flow<Product?> = productDao.getProductById(id)
    suspend fun getProductByBarcode(barcode: String): Product? = productDao.getProductByBarcode(barcode)
    suspend fun getProductByCode(code: String): Product? = productDao.getProductByCode(code)

    suspend fun insertProduct(product: Product): Long {
        val id = productDao.insert(product)
        if (product.currentStock > 0) {
            stockMovementDao.insert(
                StockMovement(
                    productId = id,
                    productName = product.name,
                    movementType = "INITIAL_STOCK",
                    quantityDelta = product.currentStock,
                    unit = product.unit,
                    reason = "Product Created with Stock"
                )
            )
        }
        return id
    }

    suspend fun updateProduct(product: Product) = productDao.update(product)
    suspend fun deleteProduct(product: Product) = productDao.delete(product)

    suspend fun insertCategory(category: Category) = categoryDao.insert(category)
    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    suspend fun adjustStock(productId: Long, delta: Double, reason: String, movementType: String = "MANUAL_ADJUSTMENT") {
        database.withTransaction {
            val product = productDao.getProductByIdSync(productId) ?: return@withTransaction
            productDao.adjustStock(productId, delta)
            stockMovementDao.insert(
                StockMovement(
                    productId = productId,
                    productName = product.name,
                    movementType = movementType,
                    quantityDelta = delta,
                    unit = product.unit,
                    reason = reason
                )
            )
        }
    }

    // POS Sales - Atomic Transaction
    suspend fun completeSale(
        cartItems: List<CartItem>,
        billDiscountPaise: Long,
        paymentMethod: String,
        cashPaidPaise: Long,
        upiPaidPaise: Long,
        cardPaidPaise: Long,
        allowNegativeStock: Boolean = false
    ): Result<Sale> = runCatching {
        database.withTransaction {
            require(cartItems.isNotEmpty()) { "Cart cannot be empty" }

            // Validate stock availability
            if (!allowNegativeStock) {
                for (item in cartItems) {
                    val dbProduct = productDao.getProductByIdSync(item.product.id)
                        ?: error("Product ${item.product.name} not found")
                    if (dbProduct.currentStock < item.quantity) {
                        error("Insufficient stock for ${item.product.name}. Available: ${dbProduct.currentStock} ${dbProduct.unit}")
                    }
                }
            }

            val subtotalPaise = cartItems.sumOf { it.grossTotalPaise }
            val itemDiscountPaise = cartItems.sumOf { it.discountPaise }
            val finalTotalPaise = maxOf(0L, subtotalPaise - itemDiscountPaise - billDiscountPaise)
            val totalCostPaise = cartItems.sumOf { it.totalCostPaise }

            val totalCount = saleDao.getTotalSalesCount()
            val receiptNumber = "SSM-${DateUtils.getTodayCodeDate()}-${String.format(Locale.ENGLISH, "%04d", totalCount + 1)}"

            val sale = Sale(
                receiptNumber = receiptNumber,
                saleDate = System.currentTimeMillis(),
                subtotalPaise = subtotalPaise,
                itemDiscountPaise = itemDiscountPaise,
                billDiscountPaise = billDiscountPaise,
                finalTotalPaise = finalTotalPaise,
                totalCostPaise = totalCostPaise,
                paymentMethod = paymentMethod,
                cashPaidPaise = cashPaidPaise,
                upiPaidPaise = upiPaidPaise,
                cardPaidPaise = cardPaidPaise
            )
            val saleId = saleDao.insertSale(sale)

            val saleItems = cartItems.map { cartItem ->
                SaleItem(
                    saleId = saleId,
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    quantity = cartItem.quantity,
                    unit = cartItem.product.unit,
                    unitPricePaise = cartItem.unitPricePaise,
                    costPriceSnapshotPaise = cartItem.product.purchasePricePaise, // Accurate historical cost snapshot!
                    discountPaise = cartItem.discountPaise,
                    totalPaise = cartItem.netTotalPaise
                )
            }
            saleDao.insertSaleItems(saleItems)

            // Reduce stock and record movements
            for (item in cartItems) {
                productDao.adjustStock(item.product.id, -item.quantity)
                stockMovementDao.insert(
                    StockMovement(
                        productId = item.product.id,
                        productName = item.product.name,
                        movementType = "SALE",
                        quantityDelta = -item.quantity,
                        unit = item.product.unit,
                        referenceId = receiptNumber,
                        reason = "POS Sale"
                    )
                )
            }

            sale.copy(id = saleId)
        }
    }

    val allSales: Flow<List<Sale>> = saleDao.getAllSales()
    fun getSaleById(id: Long): Flow<Sale?> = saleDao.getSaleById(id)
    suspend fun getSaleByIdSync(id: Long): Sale? = saleDao.getSaleByIdSync(id)
    suspend fun getSaleByReceiptNumber(receiptNumber: String): Sale? = saleDao.getSaleByReceiptNumber(receiptNumber)
    fun getSaleItems(saleId: Long): Flow<List<SaleItem>> = saleDao.getSaleItems(saleId)
    suspend fun getSaleItemsSync(saleId: Long): List<SaleItem> = saleDao.getSaleItemsSync(saleId)
    fun getSalesInRange(start: Long, end: Long): Flow<List<Sale>> = saleDao.getSalesInRange(start, end)
    fun getTopSellingProducts(limit: Int = 5): Flow<List<TopSellingProduct>> = saleDao.getTopSellingProducts(limit)

    // Purchases & Suppliers
    val allSuppliers: Flow<List<Supplier>> = supplierDao.getAllSuppliers()
    suspend fun insertSupplier(supplier: Supplier): Long = supplierDao.insert(supplier)
    suspend fun updateSupplier(supplier: Supplier) = supplierDao.update(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = supplierDao.delete(supplier)

    val allPurchases: Flow<List<Purchase>> = purchaseDao.getAllPurchases()
    fun getPurchaseItems(purchaseId: Long): Flow<List<PurchaseItem>> = purchaseDao.getPurchaseItems(purchaseId)
    suspend fun getPurchaseItemsSync(purchaseId: Long): List<PurchaseItem> = purchaseDao.getPurchaseItemsSync(purchaseId)

    suspend fun processPurchase(
        purchase: Purchase,
        items: List<PurchaseItem>
    ): Long = database.withTransaction {
        val purchaseId = purchaseDao.insertPurchase(purchase)
        val itemsWithId = items.map { it.copy(purchaseId = purchaseId) }
        purchaseDao.insertPurchaseItems(itemsWithId)

        // Automatically increase stock for products
        for (item in items) {
            productDao.adjustStock(item.productId, item.quantity)
            stockMovementDao.insert(
                StockMovement(
                    productId = item.productId,
                    productName = item.productName,
                    movementType = "PURCHASE",
                    quantityDelta = item.quantity,
                    unit = item.unit,
                    referenceId = purchase.invoiceNumber ?: "PUR-$purchaseId",
                    reason = "Purchase from ${purchase.supplierName}"
                )
            )
        }
        purchaseId
    }

    // Sales Returns
    val allReturns: Flow<List<SalesReturn>> = salesReturnDao.getAllReturns()
    fun getReturnItems(returnId: Long): Flow<List<SalesReturnItem>> = salesReturnDao.getReturnItems(returnId)

    suspend fun processSalesReturn(
        salesReturn: SalesReturn,
        returnItems: List<SalesReturnItem>
    ): Long = database.withTransaction {
        val returnId = salesReturnDao.insertReturn(salesReturn)
        val itemsWithId = returnItems.map { it.copy(returnId = returnId) }
        salesReturnDao.insertReturnItems(itemsWithId)

        // For each returned item: restore stock ONLY if resalable
        for (item in returnItems) {
            if (item.isResalable) {
                productDao.adjustStock(item.productId, item.returnQuantity)
                stockMovementDao.insert(
                    StockMovement(
                        productId = item.productId,
                        productName = item.productName,
                        movementType = "SALE_RETURN_RESTOCK",
                        quantityDelta = item.returnQuantity,
                        unit = item.unit,
                        referenceId = salesReturn.receiptNumber,
                        reason = "Returned resalable: ${salesReturn.reason}"
                    )
                )
            } else {
                stockMovementDao.insert(
                    StockMovement(
                        productId = item.productId,
                        productName = item.productName,
                        movementType = "SALE_RETURN_DAMAGED",
                        quantityDelta = 0.0, // Stock not restored
                        unit = item.unit,
                        referenceId = salesReturn.receiptNumber,
                        reason = "Returned damaged/scrap: ${salesReturn.reason}"
                    )
                )
            }
        }
        returnId
    }

    // Expenses
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    fun getExpensesInRange(start: Long, end: Long): Flow<List<Expense>> = expenseDao.getExpensesInRange(start, end)
    suspend fun insertExpense(expense: Expense): Long = expenseDao.insert(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)

    // Stock movements
    val allStockMovements: Flow<List<StockMovement>> = stockMovementDao.getAllMovements()
    fun getMovementsForProduct(productId: Long): Flow<List<StockMovement>> = stockMovementDao.getMovementsForProduct(productId)

    // Cash Register
    val activeCashSession: Flow<CashRegisterSession?> = cashRegisterDao.getActiveSession()
    val allCashSessions: Flow<List<CashRegisterSession>> = cashRegisterDao.getAllSessions()
    suspend fun openCashSession(openingPaise: Long, notes: String? = null): Long {
        val session = CashRegisterSession(
            sessionDate = DateUtils.getTodayCodeDate(),
            openedAt = System.currentTimeMillis(),
            openingCashPaise = openingPaise,
            notes = notes,
            isOpen = true
        )
        return cashRegisterDao.insertSession(session)
    }

    suspend fun closeCashSession(session: CashRegisterSession, actualClosingPaise: Long, notes: String? = null) {
        cashRegisterDao.updateSession(
            session.copy(
                closedAt = System.currentTimeMillis(),
                actualClosingCashPaise = actualClosingPaise,
                notes = if (notes != null) "${session.notes ?: ""}\n$notes" else session.notes,
                isOpen = false
            )
        )
    }
}
