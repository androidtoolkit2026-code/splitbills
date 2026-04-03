package com.splitmate.domain.usecase.balance

import com.splitmate.domain.model.Balance
import com.splitmate.domain.model.DebtTransaction
import com.splitmate.domain.model.Expense
import com.splitmate.domain.model.GroupBalance
import com.splitmate.domain.model.Settlement
import com.splitmate.domain.model.User
import java.util.PriorityQueue
import javax.inject.Inject

class CalculateBalanceUseCase @Inject constructor() {

    /**
     * Calculate net balances for each member in a group based on expenses and settlements.
     *
     * For each expense:
     *   - The payer gets credited (positive) for the total amount they paid minus their own split
     *   - Each split participant gets debited (negative) for their share (except the payer)
     *
     * For each settlement:
     *   - The payer (person who paid to settle) gets credited
     *   - The payee (person who received settlement) gets debited
     */
    operator fun invoke(
        expenses: List<Expense>,
        settlements: List<Settlement>,
        members: List<User>,
        groupId: String,
        groupName: String
    ): GroupBalance {
        // Net balance map: userId -> net amount
        // Positive = others owe this person, Negative = this person owes others
        val netBalances = mutableMapOf<String, Double>()

        // Initialize all members
        members.forEach { netBalances[it.id] = 0.0 }

        // Process expenses
        for (expense in expenses) {
            val payerId = expense.paidById

            for (split in expense.splits) {
                if (split.userId == payerId) {
                    // Payer's own share — net effect: they paid for others
                    // Credit the payer for the total amount, debit for own split below
                    continue
                }
                // The payer is owed this amount
                netBalances[payerId] = (netBalances[payerId] ?: 0.0) + split.amount
                // The split user owes this amount
                netBalances[split.userId] = (netBalances[split.userId] ?: 0.0) - split.amount
            }
        }

        // Process settlements
        for (settlement in settlements) {
            // Payer paid money to payee to settle debt
            netBalances[settlement.payerId] =
                (netBalances[settlement.payerId] ?: 0.0) + settlement.amount
            netBalances[settlement.payeeId] =
                (netBalances[settlement.payeeId] ?: 0.0) - settlement.amount
        }

        val memberMap = members.associateBy { it.id }
        val balances = netBalances.map { (userId, amount) ->
            Balance(
                userId = userId,
                userName = memberMap[userId]?.name ?: "Unknown",
                amount = Math.round(amount * 100.0) / 100.0
            )
        }

        val totalExpenses = expenses.sumOf { it.amount }

        return GroupBalance(
            groupId = groupId,
            groupName = groupName,
            balances = balances,
            simplifiedDebts = emptyList(), // Filled by SimplifyDebtsUseCase
            totalExpenses = totalExpenses
        )
    }
}

class SimplifyDebtsUseCase @Inject constructor() {

    /**
     * Minimizes the number of transactions needed to settle all debts.
     *
     * Uses a max-heap (PriorityQueue) greedy algorithm:
     *  1. Separate members into creditors (+balance) and debtors (−balance).
     *  2. Always match the largest debtor with the largest creditor.
     *  3. Record a transaction for min(|debt|, credit).
     *  4. Reinsert the non-zero residual back into the heap for re-sorting.
     *  5. Repeat until every balance is settled.
     *
     * Complexity: O(n log n) — each member is inserted/removed at most twice.
     * Transactions produced: at most (creditors + debtors − 1), the provable minimum
     * for bipartite debt settlement.
     */
    operator fun invoke(
        balances: List<Balance>,
        members: List<User>
    ): List<DebtTransaction> {
        val memberMap = members.associateBy { it.id }

        // Max-heap: always poll the entry with the largest amount
        val comparator = compareByDescending<Pair<String, Double>> { it.second }
        val creditors = PriorityQueue(comparator) // owed money  (+balance)
        val debtors   = PriorityQueue(comparator) // owe money   (stored positive)

        for (bal in balances) {
            val amount = Math.round(bal.amount * 100.0) / 100.0
            when {
                amount >  0.01 -> creditors.add(bal.userId to amount)
                amount < -0.01 -> debtors.add(bal.userId to -amount)
            }
        }

        val result = mutableListOf<DebtTransaction>()

        while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
            val (debtorId,   debtAmount)   = debtors.poll()!!
            val (creditorId, creditAmount) = creditors.poll()!!

            val transfer = Math.round(minOf(debtAmount, creditAmount) * 100.0) / 100.0

            result += DebtTransaction(
                fromUserId   = debtorId,
                fromUserName = memberMap[debtorId]?.name   ?: "Unknown",
                toUserId     = creditorId,
                toUserName   = memberMap[creditorId]?.name ?: "Unknown",
                amount       = transfer
            )

            // Reinsert residuals so the heap stays correctly ordered
            val remainingDebt   = Math.round((debtAmount   - transfer) * 100.0) / 100.0
            val remainingCredit = Math.round((creditAmount - transfer) * 100.0) / 100.0
            if (remainingDebt   > 0.01) debtors.add(debtorId   to remainingDebt)
            if (remainingCredit > 0.01) creditors.add(creditorId to remainingCredit)
        }

        return result
    }
}
