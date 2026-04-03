package com.splitmate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expense_splits",
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["expenseId"]),
        Index(value = ["userId"])
    ]
)
data class ExpenseSplitEntity(
    @PrimaryKey val id: String,
    val expenseId: String,
    val userId: String,
    val amount: Double,
    val percentage: Double = 0.0,
    val shares: Int = 1
)
