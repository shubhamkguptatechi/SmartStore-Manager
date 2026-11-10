package com.example.data.local

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
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
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class BackupValidationInfo(
    val version: Int,
    val appName: String,
    val exportedAt: Long,
    val productCount: Int,
    val saleCount: Int,
    val purchaseCount: Int,
    val expenseCount: Int
)

class BackupManager(
    private val context: Context,
    private val database: AppDatabase
) {

    suspend fun generateBackupJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "SmartStore Manager")
        root.put("exportedAt", System.currentTimeMillis())

        // Products
        val products = database.productDao().getAllProducts().first()
        val prodArray = JSONArray()
        products.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("code", p.code)
            obj.put("barcode", p.barcode ?: "")
            obj.put("category", p.category)
            obj.put("brand", p.brand ?: "")
            obj.put("purchasePricePaise", p.purchasePricePaise)
            obj.put("sellingPricePaise", p.sellingPricePaise)
            obj.put("currentStock", p.currentStock)
            obj.put("unit", p.unit)
            obj.put("minStockAlert", p.minStockAlert)
            obj.put("createdAt", p.createdAt)
            prodArray.put(obj)
        }
        root.put("products", prodArray)

        // Categories
        val categories = database.categoryDao().getAllCategories().first()
        val catArray = JSONArray()
        categories.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            catArray.put(obj)
        }
        root.put("categories", catArray)

        // Suppliers
        val suppliers = database.supplierDao().getAllSuppliers().first()
        val suppArray = JSONArray()
        suppliers.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("phone", s.phone ?: "")
            obj.put("address", s.address ?: "")
            obj.put("notes", s.notes ?: "")
            suppArray.put(obj)
        }
        root.put("suppliers", suppArray)

        // Purchases
        val purchases = database.purchaseDao().getAllPurchases().first()
        val purArray = JSONArray()
        purchases.forEach { pur ->
            val obj = JSONObject()
            obj.put("id", pur.id)
            obj.put("supplierId", pur.supplierId)
            obj.put("supplierName", pur.supplierName)
            obj.put("purchaseDate", pur.purchaseDate)
            obj.put("invoiceNumber", pur.invoiceNumber ?: "")
            obj.put("totalAmountPaise", pur.totalAmountPaise)
            obj.put("discountPaise", pur.discountPaise)
            obj.put("amountPaidPaise", pur.amountPaidPaise)
            obj.put("paymentStatus", pur.paymentStatus)
            obj.put("paymentMethod", pur.paymentMethod)
            purArray.put(obj)
        }
        root.put("purchases", purArray)

        // Sales
        val sales = database.saleDao().getAllSales().first()
        val salesArray = JSONArray()
        sales.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("receiptNumber", s.receiptNumber)
            obj.put("saleDate", s.saleDate)
            obj.put("subtotalPaise", s.subtotalPaise)
            obj.put("itemDiscountPaise", s.itemDiscountPaise)
            obj.put("billDiscountPaise", s.billDiscountPaise)
            obj.put("finalTotalPaise", s.finalTotalPaise)
            obj.put("totalCostPaise", s.totalCostPaise)
            obj.put("paymentMethod", s.paymentMethod)
            obj.put("cashPaidPaise", s.cashPaidPaise)
            obj.put("upiPaidPaise", s.upiPaidPaise)
            obj.put("cardPaidPaise", s.cardPaidPaise)
            salesArray.put(obj)
        }
        root.put("sales", salesArray)

        // Expenses
        val expenses = database.expenseDao().getAllExpenses().first()
        val expArray = JSONArray()
        expenses.forEach { exp ->
            val obj = JSONObject()
            obj.put("id", exp.id)
            obj.put("category", exp.category)
            obj.put("amountPaise", exp.amountPaise)
            obj.put("expenseDate", exp.expenseDate)
            obj.put("paymentMethod", exp.paymentMethod)
            obj.put("notes", exp.notes)
            expArray.put(obj)
        }
        root.put("expenses", expArray)

        // Stock Movements
        val movements = database.stockMovementDao().getAllMovements().first()
        val movArray = JSONArray()
        movements.forEach { m ->
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("productId", m.productId)
            obj.put("productName", m.productName)
            obj.put("movementType", m.movementType)
            obj.put("quantityDelta", m.quantityDelta)
            obj.put("unit", m.unit)
            obj.put("referenceId", m.referenceId ?: "")
            obj.put("reason", m.reason)
            obj.put("timestamp", m.timestamp)
            movArray.put(obj)
        }
        root.put("movements", movArray)

        return root.toString(2)
    }

    suspend fun exportToUri(uri: Uri): Result<Unit> = runCatching {
        val json = generateBackupJson()
        context.contentResolver.openOutputStream(uri)?.use { out ->
            OutputStreamWriter(out).use { writer ->
                writer.write(json)
            }
        } ?: error("Unable to open output stream")
    }

    fun validateBackupJson(jsonString: String): Result<BackupValidationInfo> = runCatching {
        val root = JSONObject(jsonString)
        val version = root.getInt("version")
        val appName = root.getString("appName")
        val exportedAt = root.getLong("exportedAt")

        val products = root.getJSONArray("products").length()
        val sales = root.getJSONArray("sales").length()
        val purchases = root.getJSONArray("purchases").length()
        val expenses = root.getJSONArray("expenses").length()

        BackupValidationInfo(
            version = version,
            appName = appName,
            exportedAt = exportedAt,
            productCount = products,
            saleCount = sales,
            purchaseCount = purchases,
            expenseCount = expenses
        )
    }

    suspend fun readJsonFromUri(uri: Uri): Result<String> = runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input)).use { reader ->
                reader.readText()
            }
        } ?: error("Unable to open input stream")
    }

    suspend fun restoreFromJson(jsonString: String): Result<Unit> = runCatching {
        val root = JSONObject(jsonString)
        require(root.getInt("version") == 1) { "Unsupported backup version" }

        database.withTransaction {
            // Restore categories
            if (root.has("categories")) {
                val catArr = root.getJSONArray("categories")
                val cats = mutableListOf<Category>()
                for (i in 0 until catArr.length()) {
                    val obj = catArr.getJSONObject(i)
                    cats.add(
                        Category(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name")
                        )
                    )
                }
                database.categoryDao().insertAll(cats)
            }

            // Restore products
            if (root.has("products")) {
                val prodArr = root.getJSONArray("products")
                val prods = mutableListOf<Product>()
                for (i in 0 until prodArr.length()) {
                    val obj = prodArr.getJSONObject(i)
                    prods.add(
                        Product(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            code = obj.getString("code"),
                            barcode = obj.optString("barcode").ifBlank { null },
                            category = obj.getString("category"),
                            brand = obj.optString("brand").ifBlank { null },
                            purchasePricePaise = obj.getLong("purchasePricePaise"),
                            sellingPricePaise = obj.getLong("sellingPricePaise"),
                            currentStock = obj.getDouble("currentStock"),
                            unit = obj.getString("unit"),
                            minStockAlert = obj.optDouble("minStockAlert", 5.0),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                database.productDao().insertAll(prods)
            }

            // Restore suppliers
            if (root.has("suppliers")) {
                val suppArr = root.getJSONArray("suppliers")
                for (i in 0 until suppArr.length()) {
                    val obj = suppArr.getJSONObject(i)
                    database.supplierDao().insert(
                        Supplier(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            phone = obj.optString("phone").ifBlank { null },
                            address = obj.optString("address").ifBlank { null },
                            notes = obj.optString("notes").ifBlank { null }
                        )
                    )
                }
            }

            // Restore sales
            if (root.has("sales")) {
                val salesArr = root.getJSONArray("sales")
                for (i in 0 until salesArr.length()) {
                    val obj = salesArr.getJSONObject(i)
                    database.saleDao().insertSale(
                        Sale(
                            id = obj.optLong("id", 0),
                            receiptNumber = obj.getString("receiptNumber"),
                            saleDate = obj.getLong("saleDate"),
                            subtotalPaise = obj.getLong("subtotalPaise"),
                            itemDiscountPaise = obj.optLong("itemDiscountPaise", 0),
                            billDiscountPaise = obj.optLong("billDiscountPaise", 0),
                            finalTotalPaise = obj.getLong("finalTotalPaise"),
                            totalCostPaise = obj.optLong("totalCostPaise", 0),
                            paymentMethod = obj.getString("paymentMethod"),
                            cashPaidPaise = obj.optLong("cashPaidPaise", 0),
                            upiPaidPaise = obj.optLong("upiPaidPaise", 0),
                            cardPaidPaise = obj.optLong("cardPaidPaise", 0)
                        )
                    )
                }
            }

            // Restore expenses
            if (root.has("expenses")) {
                val expArr = root.getJSONArray("expenses")
                for (i in 0 until expArr.length()) {
                    val obj = expArr.getJSONObject(i)
                    database.expenseDao().insert(
                        Expense(
                            id = obj.optLong("id", 0),
                            category = obj.getString("category"),
                            amountPaise = obj.getLong("amountPaise"),
                            expenseDate = obj.getLong("expenseDate"),
                            paymentMethod = obj.optString("paymentMethod", "CASH"),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            }
        }
    }
}
