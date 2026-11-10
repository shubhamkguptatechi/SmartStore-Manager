package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.CashRegisterDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.PurchaseDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.dao.SalesReturnDao
import com.example.data.local.dao.StockMovementDao
import com.example.data.local.dao.SupplierDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Category::class,
        Supplier::class,
        Purchase::class,
        PurchaseItem::class,
        Sale::class,
        SaleItem::class,
        SalesReturn::class,
        SalesReturnItem::class,
        StockMovement::class,
        Expense::class,
        CashRegisterSession::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun saleDao(): SaleDao
    abstract fun salesReturnDao(): SalesReturnDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun cashRegisterDao(): CashRegisterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartstore_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            // Seed Categories
            val categories = listOf(
                Category(name = "Grains & Pulses"),
                Category(name = "Dairy & Bakery"),
                Category(name = "Beverages"),
                Category(name = "Snacks & Biscuits"),
                Category(name = "Spices & Oils"),
                Category(name = "Personal Care"),
                Category(name = "Household & Cleaning"),
                Category(name = "General")
            )
            database.categoryDao().insertAll(categories)

            // Seed Supplier
            val supplierId = database.supplierDao().insert(
                Supplier(
                    name = "Metro Wholesale Distributors",
                    phone = "+91 98765 43210",
                    address = "Sector 18 APMC Yard",
                    notes = "Primary FMCG supplier"
                )
            )

            // Seed Initial Products
            val products = listOf(
                Product(
                    name = "Basmati Rice (1 kg)",
                    code = "BR-1KG",
                    barcode = "890123456701",
                    category = "Grains & Pulses",
                    brand = "India Gate",
                    purchasePricePaise = 8500,
                    sellingPricePaise = 11500,
                    currentStock = 25.0,
                    unit = "Kilogram",
                    minStockAlert = 5.0
                ),
                Product(
                    name = "Whole Wheat Atta (5 kg)",
                    code = "WWA-5KG",
                    barcode = "890123456702",
                    category = "Grains & Pulses",
                    brand = "Aashirvaad",
                    purchasePricePaise = 21000,
                    sellingPricePaise = 26500,
                    currentStock = 14.0,
                    unit = "Packet",
                    minStockAlert = 4.0
                ),
                Product(
                    name = "Sunflower Cooking Oil (1 L)",
                    code = "SCO-1L",
                    barcode = "890123456703",
                    category = "Spices & Oils",
                    brand = "Fortune",
                    purchasePricePaise = 12500,
                    sellingPricePaise = 15500,
                    currentStock = 20.0,
                    unit = "Litre",
                    minStockAlert = 5.0
                ),
                Product(
                    name = "Fresh Toned Milk (500 ml)",
                    code = "FTM-500",
                    barcode = "890123456704",
                    category = "Dairy & Bakery",
                    brand = "Amul",
                    purchasePricePaise = 2600,
                    sellingPricePaise = 3000,
                    currentStock = 18.0,
                    unit = "Packet",
                    minStockAlert = 4.0
                ),
                Product(
                    name = "Whole Wheat Bread (400 g)",
                    code = "WWB-400",
                    barcode = "890123456705",
                    category = "Dairy & Bakery",
                    brand = "Britannia",
                    purchasePricePaise = 3200,
                    sellingPricePaise = 4500,
                    currentStock = 10.0,
                    unit = "Packet",
                    minStockAlert = 3.0
                ),
                Product(
                    name = "Tata Iodized Salt (1 kg)",
                    code = "TIS-1KG",
                    barcode = "890123456706",
                    category = "Spices & Oils",
                    brand = "Tata",
                    purchasePricePaise = 2200,
                    sellingPricePaise = 2800,
                    currentStock = 35.0,
                    unit = "Packet",
                    minStockAlert = 8.0
                ),
                Product(
                    name = "Premium Tea Powder (250 g)",
                    code = "PTP-250",
                    barcode = "890123456707",
                    category = "Beverages",
                    brand = "Taj Mahal",
                    purchasePricePaise = 11000,
                    sellingPricePaise = 14500,
                    currentStock = 16.0,
                    unit = "Packet",
                    minStockAlert = 4.0
                ),
                Product(
                    name = "Good Day Butter Cookies (200 g)",
                    code = "GDC-200",
                    barcode = "890123456708",
                    category = "Snacks & Biscuits",
                    brand = "Britannia",
                    purchasePricePaise = 2800,
                    sellingPricePaise = 3500,
                    currentStock = 28.0,
                    unit = "Packet",
                    minStockAlert = 6.0
                ),
                Product(
                    name = "Toor Dal Premium (1 kg)",
                    code = "TDP-1KG",
                    barcode = "890123456709",
                    category = "Grains & Pulses",
                    brand = "Organic Tattva",
                    purchasePricePaise = 14500,
                    sellingPricePaise = 18000,
                    currentStock = 3.0, // Low stock indicator!
                    unit = "Kilogram",
                    minStockAlert = 6.0
                ),
                Product(
                    name = "Herbal Bathing Soap (125 g)",
                    code = "HBS-125",
                    barcode = "890123456710",
                    category = "Personal Care",
                    brand = "Medimix",
                    purchasePricePaise = 3600,
                    sellingPricePaise = 4800,
                    currentStock = 22.0,
                    unit = "Piece",
                    minStockAlert = 5.0
                )
            )
            database.productDao().insertAll(products)

            // Seed stock movements for initial inventory
            products.forEach { p ->
                database.stockMovementDao().insert(
                    StockMovement(
                        productId = p.id,
                        productName = p.name,
                        movementType = "INITIAL_STOCK",
                        quantityDelta = p.currentStock,
                        unit = p.unit,
                        reason = "Store Opening Balance"
                    )
                )
            }
        }
    }
}
