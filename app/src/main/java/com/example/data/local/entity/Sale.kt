package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [
        Index("receiptNumber", unique = true),
        Index("saleDate")
    ]
)
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNumber: String,
    val saleDate: Long = System.currentTimeMillis(),
    val subtotalPaise: Long,
    val itemDiscountPaise: Long = 0,
    val billDiscountPaise: Long = 0,
    val finalTotalPaise: Long,
    val totalCostPaise: Long, // Snapshot of sum of (quantity * costPriceSnapshotPaise)
    val paymentMethod: String, // CASH, UPI, CARD, SPLIT
    val cashPaidPaise: Long = 0,
    val upiPaidPaise: Long = 0,
    val cardPaidPaise: Long = 0,
    val isReturned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("saleId"), Index("productId")]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unit: String,
    val unitPricePaise: Long,
    val costPriceSnapshotPaise: Long, // Snapshot of purchase cost at time of sale
    val discountPaise: Long = 0,
    val totalPaise: Long
)
