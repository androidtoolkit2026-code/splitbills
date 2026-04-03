package com.splitmate.domain.usecase.expense

import com.splitmate.domain.model.Expense
import com.splitmate.domain.model.ExpenseSplit
import com.splitmate.domain.model.SplitType
import com.splitmate.domain.repository.ExpenseRepository
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense, splits: List<ExpenseSplit>): Result<Unit> {
        return try {
            require(expense.title.isNotBlank()) { "Title cannot be empty" }
            require(expense.amount > 0) { "Amount must be greater than zero" }
            require(expense.paidById.isNotBlank()) { "Please select who paid" }
            require(splits.isNotEmpty()) { "Must have at least one person to split with" }
            require(splits.all { it.amount >= 0 }) { "Split amounts cannot be negative" }

            val splitSum = splits.sumOf { it.amount }
            val tolerance = 0.02
            require(kotlin.math.abs(splitSum - expense.amount) < tolerance) {
                "Split total (${String.format("%.2f", splitSum)}) must equal expense amount (${String.format("%.2f", expense.amount)})"
            }

            expenseRepository.createExpense(expense, splits)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class CalculateSplitsUseCase @Inject constructor() {
    operator fun invoke(
        amount: Double,
        memberIds: List<String>,
        splitType: SplitType,
        exactAmounts: Map<String, Double> = emptyMap(),
        percentages: Map<String, Double> = emptyMap(),
        shares: Map<String, Int> = emptyMap(),
        expenseId: String = ""
    ): List<ExpenseSplit> {
        if (memberIds.isEmpty()) return emptyList()

        return when (splitType) {
            SplitType.EQUAL -> {
                val baseAmount = Math.floor(amount / memberIds.size * 100.0) / 100.0
                val remainder = Math.round((amount - baseAmount * memberIds.size) * 100.0) / 100.0
                memberIds.mapIndexed { index, userId ->
                    val splitAmount = if (index == memberIds.lastIndex)
                        Math.round((baseAmount + remainder) * 100.0) / 100.0
                    else
                        baseAmount
                    ExpenseSplit(
                        expenseId = expenseId,
                        userId = userId,
                        amount = splitAmount,
                        shares = 1
                    )
                }
            }

            SplitType.EXACT -> {
                memberIds.map { userId ->
                    ExpenseSplit(
                        expenseId = expenseId,
                        userId = userId,
                        amount = Math.round((exactAmounts[userId] ?: 0.0) * 100.0) / 100.0
                    )
                }
            }

            SplitType.PERCENTAGE -> {
                val baseAmounts = memberIds.map { userId ->
                    val pct = percentages[userId] ?: 0.0
                    Math.floor(amount * pct / 100.0 * 100.0) / 100.0
                }
                val assigned = baseAmounts.sum()
                val remainder = Math.round((amount - assigned) * 100.0) / 100.0
                memberIds.mapIndexed { index, userId ->
                    val pct = percentages[userId] ?: 0.0
                    val splitAmount = if (index == memberIds.lastIndex)
                        Math.round((baseAmounts[index] + remainder) * 100.0) / 100.0
                    else
                        baseAmounts[index]
                    ExpenseSplit(
                        expenseId = expenseId,
                        userId = userId,
                        amount = splitAmount,
                        percentage = pct
                    )
                }
            }

            SplitType.SHARES -> {
                val totalShares = memberIds.sumOf { shares[it] ?: 1 }.coerceAtLeast(1)
                val baseAmounts = memberIds.map { userId ->
                    val userShares = (shares[userId] ?: 1).coerceAtLeast(1)
                    Math.floor(amount * userShares.toDouble() / totalShares * 100.0) / 100.0
                }
                val assigned = baseAmounts.sum()
                val remainder = Math.round((amount - assigned) * 100.0) / 100.0
                memberIds.mapIndexed { index, userId ->
                    val userShares = (shares[userId] ?: 1).coerceAtLeast(1)
                    val splitAmount = if (index == memberIds.lastIndex)
                        Math.round((baseAmounts[index] + remainder) * 100.0) / 100.0
                    else
                        baseAmounts[index]
                    ExpenseSplit(
                        expenseId = expenseId,
                        userId = userId,
                        amount = splitAmount,
                        shares = userShares
                    )
                }
            }
        }
    }
}

/** Returns validation result for live UI feedback without saving */
class ValidateExpenseSplitsUseCase @Inject constructor() {
    data class ValidationResult(
        val isValid: Boolean,
        val message: String,
        val remainingAmount: Double = 0.0,
        val remainingPercentage: Double = 0.0
    )

    operator fun invoke(
        totalAmount: Double,
        selectedMembers: List<String>,
        splitType: SplitType,
        exactAmounts: Map<String, Double>,
        percentages: Map<String, Double>
    ): ValidationResult {
        if (selectedMembers.isEmpty()) return ValidationResult(false, "Select at least one member")
        if (totalAmount <= 0) return ValidationResult(false, "Enter a valid amount")

        return when (splitType) {
            SplitType.EQUAL -> ValidationResult(true, "Each pays ${String.format("%.2f", totalAmount / selectedMembers.size)}")

            SplitType.EXACT -> {
                val assigned = selectedMembers.sumOf { exactAmounts[it] ?: 0.0 }
                val remaining = Math.round((totalAmount - assigned) * 100.0) / 100.0
                when {
                    remaining > 0.01 -> ValidationResult(false, "₹${String.format("%.2f", remaining)} remaining to assign", remainingAmount = remaining)
                    remaining < -0.01 -> ValidationResult(false, "Over by ₹${String.format("%.2f", -remaining)}", remainingAmount = remaining)
                    else -> ValidationResult(true, "All ₹${String.format("%.2f", totalAmount)} assigned")
                }
            }

            SplitType.PERCENTAGE -> {
                val assigned = selectedMembers.sumOf { percentages[it] ?: 0.0 }
                val remaining = Math.round((100.0 - assigned) * 100.0) / 100.0
                when {
                    remaining > 0.01 -> ValidationResult(false, "${String.format("%.1f", remaining)}% remaining to assign", remainingPercentage = remaining)
                    remaining < -0.01 -> ValidationResult(false, "Over by ${String.format("%.1f", -remaining)}%", remainingPercentage = remaining)
                    else -> ValidationResult(true, "100% assigned")
                }
            }

            SplitType.SHARES -> {
                ValidationResult(true, "Divided among ${selectedMembers.size} member${if (selectedMembers.size != 1) "s" else ""}")
            }
        }
    }
}
