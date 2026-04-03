package com.splitmate.domain.model

import java.util.UUID

enum class SplitType {
    EQUAL, EXACT, PERCENTAGE, SHARES
}

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val title: String,
    val amount: Double,
    val currency: String = "INR",
    val paidById: String,
    val splitType: SplitType = SplitType.EQUAL,
    val notes: String = "",
    val receiptUri: String? = null,
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val splits: List<ExpenseSplit> = emptyList()
)

data class ExpenseSplit(
    val id: String = UUID.randomUUID().toString(),
    val expenseId: String,
    val userId: String,
    val amount: Double,
    val percentage: Double = 0.0,
    val shares: Int = 1,
    val userName: String = ""
)
