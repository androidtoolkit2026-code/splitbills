package com.splitmate.domain.model

import java.util.UUID

enum class PaymentMethod {
    CASH, UPI, BANK
}

data class Settlement(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val payerId: String,
    val payeeId: String,
    val amount: Double,
    val currency: String = "INR",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val payerName: String = "",
    val payeeName: String = ""
)
