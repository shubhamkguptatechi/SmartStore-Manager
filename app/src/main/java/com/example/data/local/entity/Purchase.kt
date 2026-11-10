package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    indices = [Index("supplierId"), Index("purchaseDate")]
)
data class Purchase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supplierId: Long,
    val supplierName: String,
    val purchaseDate: Long = System.currentTimeMillis(),
    val invoiceNumber: String? = null,
    val totalAmountPaise: Long,
    val discountPaise: Long = 0,
    val amountPaidPaise: Long,
    val paymentStatus: String, // PAID, PARTIALLY_PAID, UNPAID
    val paymentMethod: String = "CASH", // CASH, UPI, CARD
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = Purchase::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("purchaseId"), Index("productId")]
)
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unit: String,
    val purchaseRatePaise: Long,
    val totalPaise: Long
)
