package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["barcode"]),
        Index(value = ["category"])
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String, // SKU / short product code
    val barcode: String? = null,
    val category: String,
    val brand: String? = null,
    val purchasePricePaise: Long, // in paise (e.g. 1000 = ₹10.00)
    val sellingPricePaise: Long,  // in paise (e.g. 1500 = ₹15.00)
    val currentStock: Double,     // supports fractional: 1.5, 0.25, etc.
    val unit: String,            // Piece, Packet, Bottle, Box, Kilogram, Gram, Litre, Millilitre, Dozen
    val minStockAlert: Double = 5.0,
    val expiryDate: Long? = null,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
