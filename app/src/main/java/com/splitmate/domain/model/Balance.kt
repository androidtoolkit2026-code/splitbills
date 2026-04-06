package com.splitmate.domain.model

data class Balance(
    val userId: String,
    val userName: String,
    val amount: Double // positive = owed TO this user, negative = this user OWES
)

/**
 * Represents a single minimized debt payment between two users.
 * Produced by [com.splitmate.domain.usecase.balance.SimplifyDebtsUseCase].
 *
 * [fromUserId] owes [amount] to [toUserId].
 */
data class DebtTransaction(
    val fromUserId: String,
    val fromUserName: String,
    val toUserId: String,
    val toUserName: String,
    val amount: Double
)

data class GroupBalance(
    val groupId: String,
    val groupName: String,
    val balances: List<Balance>,
    val simplifiedDebts: List<DebtTransaction>,
    val totalExpenses: Double
)
