package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CashRegisterSession
import com.example.data.local.entity.Expense
import com.example.data.local.entity.StockMovement
import kotlinx.coroutines.flow.Flow

data class CategoryExpense(
    val category: String,
    val totalAmountPaise: Long
)

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE expenseDate >= :startTime AND expenseDate <= :endTime ORDER BY expenseDate DESC")
    fun getExpensesInRange(startTime: Long, endTime: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amountPaise) FROM expenses WHERE expenseDate >= :startTime AND expenseDate <= :endTime")
    fun getTotalExpensesInRange(startTime: Long, endTime: Long): Flow<Long?>

    @Query("SELECT SUM(amountPaise) FROM expenses WHERE paymentMethod = 'CASH' AND expenseDate >= :startTime AND expenseDate <= :endTime")
    fun getCashExpensesInRange(startTime: Long, endTime: Long): Flow<Long?>

    @Query("""
        SELECT category, SUM(amountPaise) as totalAmountPaise
        FROM expenses
        WHERE expenseDate >= :startTime AND expenseDate <= :endTime
        GROUP BY category
    """)
    fun getExpensesByCategoryInRange(startTime: Long, endTime: Long): Flow<List<CategoryExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<StockMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movement: StockMovement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movements: List<StockMovement>)
}

@Dao
interface CashRegisterDao {
    @Query("SELECT * FROM cash_register_sessions WHERE isOpen = 1 ORDER BY openedAt DESC LIMIT 1")
    fun getActiveSession(): Flow<CashRegisterSession?>

    @Query("SELECT * FROM cash_register_sessions WHERE isOpen = 1 ORDER BY openedAt DESC LIMIT 1")
    suspend fun getActiveSessionSync(): CashRegisterSession?

    @Query("SELECT * FROM cash_register_sessions ORDER BY openedAt DESC")
    fun getAllSessions(): Flow<List<CashRegisterSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CashRegisterSession): Long

    @Update
    suspend fun updateSession(session: CashRegisterSession)
}
