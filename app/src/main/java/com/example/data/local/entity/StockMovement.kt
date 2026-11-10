package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_movements",
    indices = [
        Index("productId"),
        Index("timestamp")
    ]
)
data class StockMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val movementType: String, // SALE, PURCHASE, SALE_RETURN_RESTOCK, SALE_RETURN_DAMAGED, MANUAL_ADJUSTMENT
    val quantityDelta: Double, // Negative for sale, positive for purchase/restock
    val unit: String,
    val referenceId: String? = null, // Receipt #, Invoice #, etc.
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
