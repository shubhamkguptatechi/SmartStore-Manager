package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index("category"),
        Index("expenseDate")
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // Rent, Electricity, Salary, Transport, Packaging, Maintenance, Other
    val amountPaise: Long,
    val expenseDate: Long = System.currentTimeMillis(),
    val paymentMethod: String = "CASH", // CASH, UPI, CARD
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cash_register_sessions",
    indices = [Index("sessionDate")]
)
data class CashRegisterSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionDate: String, // e.g. "2026-11-09"
    val openedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val openingCashPaise: Long = 0,
    val actualClosingCashPaise: Long? = null,
    val notes: String? = null,
    val isOpen: Boolean = true
)
