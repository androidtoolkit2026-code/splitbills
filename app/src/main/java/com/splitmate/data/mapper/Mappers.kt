package com.splitmate.data.mapper

import com.splitmate.data.local.entity.*
import com.splitmate.domain.model.*

// User mappers
fun UserEntity.toDomain() = User(
    id = id,
    name = name,
    email = email,
    phone = phone,
    avatarUrl = avatarUrl,
    defaultCurrency = defaultCurrency,
    firebaseUid = firebaseUid,
    createdAt = createdAt
)

fun User.toEntity(isCurrentUser: Boolean = false) = UserEntity(
    id = id,
    name = name,
    email = email,
    phone = phone,
    avatarUrl = avatarUrl,
    defaultCurrency = defaultCurrency,
    firebaseUid = firebaseUid,
    isCurrentUser = isCurrentUser,
    createdAt = createdAt
)

// Group mappers
fun GroupEntity.toDomain(members: List<User> = emptyList(), totalExpenses: Double = 0.0) = Group(
    id = id,
    name = name,
    description = description,
    type = try { GroupType.valueOf(type) } catch (e: Exception) { GroupType.OTHER },
    iconName = iconName,
    currency = currency,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt,
    members = members,
    totalExpenses = totalExpenses
)

fun Group.toEntity() = GroupEntity(
    id = id,
    name = name,
    description = description,
    type = type.name,
    iconName = iconName,
    currency = currency,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Expense mappers
fun ExpenseEntity.toDomain(splits: List<ExpenseSplit> = emptyList()) = Expense(
    id = id,
    groupId = groupId,
    title = title,
    amount = amount,
    currency = currency,
    paidById = paidById,
    splitType = try { SplitType.valueOf(splitType) } catch (e: Exception) { SplitType.EQUAL },
    notes = notes,
    receiptUri = receiptUri,
    date = date,
    createdAt = createdAt,
    updatedAt = updatedAt,
    splits = splits
)

fun Expense.toEntity() = ExpenseEntity(
    id = id,
    groupId = groupId,
    title = title,
    amount = amount,
    currency = currency,
    paidById = paidById,
    splitType = splitType.name,
    notes = notes,
    receiptUri = receiptUri,
    date = date,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ExpenseSplit mappers
fun ExpenseSplitEntity.toDomain(userName: String = "") = ExpenseSplit(
    id = id,
    expenseId = expenseId,
    userId = userId,
    amount = amount,
    percentage = percentage,
    shares = shares,
    userName = userName
)

fun ExpenseSplit.toEntity() = ExpenseSplitEntity(
    id = id,
    expenseId = expenseId,
    userId = userId,
    amount = amount,
    percentage = percentage,
    shares = shares
)

// Settlement mappers
fun SettlementEntity.toDomain(payerName: String = "", payeeName: String = "") = Settlement(
    id = id,
    groupId = groupId,
    payerId = payerId,
    payeeId = payeeId,
    amount = amount,
    currency = currency,
    paymentMethod = try { PaymentMethod.valueOf(paymentMethod) } catch (e: Exception) { PaymentMethod.CASH },
    notes = notes,
    date = date,
    createdAt = createdAt,
    payerName = payerName,
    payeeName = payeeName
)

fun Settlement.toEntity() = SettlementEntity(
    id = id,
    groupId = groupId,
    payerId = payerId,
    payeeId = payeeId,
    amount = amount,
    currency = currency,
    paymentMethod = paymentMethod.name,
    notes = notes,
    date = date,
    createdAt = createdAt
)
