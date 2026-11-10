package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Purchase
import com.example.data.local.entity.PurchaseItem
import com.example.data.local.entity.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    fun getSupplierById(id: Long): Flow<Supplier?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: Supplier): Long

    @Update
    suspend fun update(supplier: Supplier)

    @Delete
    suspend fun delete(supplier: Supplier)
}

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC")
    fun getAllPurchases(): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getPurchaseById(id: Long): Purchase?

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY purchaseDate DESC")
    fun getPurchasesForSupplier(supplierId: Long): Flow<List<Purchase>>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    fun getPurchaseItems(purchaseId: Long): Flow<List<PurchaseItem>>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getPurchaseItemsSync(purchaseId: Long): List<PurchaseItem>

    @Query("SELECT * FROM purchases WHERE purchaseDate >= :startTime AND purchaseDate <= :endTime ORDER BY purchaseDate DESC")
    fun getPurchasesInRange(startTime: Long, endTime: Long): Flow<List<Purchase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItem>)

    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun deletePurchase(id: Long)
}
