package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Category
import com.example.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): Flow<Product?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductByIdSync(id: Long): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE code = :code LIMIT 1")
    suspend fun getProductByCode(code: String): Product?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE currentStock <= minStockAlert ORDER BY currentStock ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT COUNT(*) FROM products WHERE currentStock <= minStockAlert")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products")
    fun getTotalProductsCount(): Flow<Int>

    @Query("SELECT SUM(currentStock * purchasePricePaise) FROM products")
    fun getTotalInventoryValuePaise(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("UPDATE products SET currentStock = currentStock + :delta WHERE id = :productId")
    suspend fun adjustStock(productId: Long, delta: Double)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>)

    @Delete
    suspend fun delete(category: Category)
}
