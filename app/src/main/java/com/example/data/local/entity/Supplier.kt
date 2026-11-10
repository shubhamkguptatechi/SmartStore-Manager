package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
