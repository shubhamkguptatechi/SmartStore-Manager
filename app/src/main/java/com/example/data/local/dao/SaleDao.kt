package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import com.example.data.local.entity.SalesReturn
import com.example.data.local.entity.SalesReturnItem
import kotlinx.coroutines.flow.Flow

data class TopSellingProduct(
    val productId: Long,
    val productName: String,
    val totalQty: Double,
    val totalRevenuePaise: Long
)

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY saleDate DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    fun getSaleById(id: Long): Flow<Sale?>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleByIdSync(id: Long): Sale?

    @Query("SELECT * FROM sales WHERE receiptNumber = :receiptNumber LIMIT 1")
    suspend fun getSaleByReceiptNumber(receiptNumber: String): Sale?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getSaleItems(saleId: Long): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItemsSync(saleId: Long): List<SaleItem>

    @Query("SELECT * FROM sales WHERE saleDate >= :startTime AND saleDate <= :endTime ORDER BY saleDate DESC")
    fun getSalesInRange(startTime: Long, endTime: Long): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE saleDate >= :startTime AND saleDate <= :endTime ORDER BY saleDate DESC")
    suspend fun getSalesInRangeSync(startTime: Long, endTime: Long): List<Sale>

    @Query("SELECT COUNT(*) FROM sales WHERE saleDate >= :startTime AND saleDate <= :endTime")
    fun getSalesCountInRange(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT SUM(finalTotalPaise) FROM sales WHERE saleDate >= :startTime AND saleDate <= :endTime")
    fun getTotalRevenueInRange(startTime: Long, endTime: Long): Flow<Long?>

    @Query("SELECT SUM(totalCostPaise) FROM sales WHERE saleDate >= :startTime AND saleDate <= :endTime")
    fun getTotalCostInRange(startTime: Long, endTime: Long): Flow<Long?>

    @Query("""
        SELECT productId, productName, SUM(quantity) as totalQty, SUM(totalPaise) as totalRevenuePaise
        FROM sale_items
        GROUP BY productId, productName
        ORDER BY totalQty DESC
        LIMIT :limit
    """)
    fun getTopSellingProducts(limit: Int = 5): Flow<List<TopSellingProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Update
    suspend fun updateSale(sale: Sale)

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun getTotalSalesCount(): Int
}

@Dao
interface SalesReturnDao {
    @Query("SELECT * FROM sales_returns ORDER BY returnDate DESC")
    fun getAllReturns(): Flow<List<SalesReturn>>

    @Query("SELECT * FROM sales_returns WHERE id = :id")
    suspend fun getReturnById(id: Long): SalesReturn?

    @Query("SELECT * FROM sales_return_items WHERE returnId = :returnId")
    fun getReturnItems(returnId: Long): Flow<List<SalesReturnItem>>

    @Query("SELECT * FROM sales_return_items WHERE returnId = :returnId")
    suspend fun getReturnItemsSync(returnId: Long): List<SalesReturnItem>

    @Query("SELECT * FROM sales_returns WHERE returnDate >= :startTime AND returnDate <= :endTime")
    fun getReturnsInRange(startTime: Long, endTime: Long): Flow<List<SalesReturn>>

    @Query("SELECT SUM(refundAmountPaise) FROM sales_returns WHERE returnDate >= :startTime AND returnDate <= :endTime")
    fun getTotalRefundsInRange(startTime: Long, endTime: Long): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturn(salesReturn: SalesReturn): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturnItems(items: List<SalesReturnItem>)
}
