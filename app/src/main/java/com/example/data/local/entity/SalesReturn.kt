package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales_returns",
    indices = [Index("saleId"), Index("returnDate")]
)
data class SalesReturn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val receiptNumber: String,
    val returnDate: Long = System.currentTimeMillis(),
    val refundAmountPaise: Long,
    val paymentMethod: String = "CASH", // CASH, UPI, CARD
    val reason: String = "Customer return",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sales_return_items",
    foreignKeys = [
        ForeignKey(
            entity = SalesReturn::class,
            parentColumns = ["id"],
            childColumns = ["returnId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("returnId"), Index("productId")]
)
data class SalesReturnItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val returnId: Long,
    val saleItemId: Long,
    val productId: Long,
    val productName: String,
    val returnQuantity: Double,
    val unit: String,
    val refundPaise: Long,
    val isResalable: Boolean = true, // If true, adds back to product stock. If false, damaged.
    val costPriceSnapshotPaise: Long
)
