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

/** Backward-compatible alias kept for existing call sites. */
typealias DebtSimplification = DebtTransaction

data class GroupBalance(
    val groupId: String,
    val groupName: String,
    val balances: List<Balance>,
    val simplifiedDebts: List<DebtSimplification>,
    val totalExpenses: Double
)

data class DashboardSummary(
    val totalOwed: Double,
    val totalOwedToYou: Double,
    val netBalance: Double,
    val groupSummaries: List<GroupBalance>,
    val recentActivities: List<ActivityItem>
)

data class ActivityItem(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val groupId: String,
    val groupName: String,
    val date: Long
)

enum class ActivityType {
    EXPENSE_ADDED, EXPENSE_UPDATED, EXPENSE_DELETED,
    SETTLEMENT, GROUP_CREATED, MEMBER_ADDED
}
