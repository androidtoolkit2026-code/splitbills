package com.splitmate.domain.usecase.balance

import com.splitmate.domain.model.Balance
import com.splitmate.domain.model.DebtTransaction
import com.splitmate.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [SimplifyDebtsUseCase].
 *
 * The algorithm guarantees:
 *  - Minimum number of transactions (at most creditors + debtors − 1)
 *  - Every creditor is fully paid back
 *  - Every debtor fully pays their debt
 *  - No floating-point drift (amounts rounded to 2 decimal places)
 */
class SimplifyDebtsUseCaseTest {

    private val useCase = SimplifyDebtsUseCase()

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun user(id: String, name: String) = User(id = id, name = name)
    private fun bal(userId: String, userName: String, amount: Double) =
        Balance(userId = userId, userName = userName, amount = amount)

    /** Verify that the transactions fully settle every non-zero balance. */
    private fun assertFullySettled(
        balances: List<Balance>,
        transactions: List<DebtTransaction>
    ) {
        val net = balances.associate { it.userId to it.amount }.toMutableMap()
        for (t in transactions) {
            net[t.fromUserId] = (net[t.fromUserId] ?: 0.0) + t.amount
            net[t.toUserId]   = (net[t.toUserId]   ?: 0.0) - t.amount
        }
        net.forEach { (id, remaining) ->
            assertEquals("User $id should be fully settled", 0.0, remaining, 0.02)
        }
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    /**
     * Example from the requirements:
     *
     *  Alice  +₹300  (is owed)
     *  Bob    −₹100  (owes)
     *  Carol  −₹200  (owes)
     *
     * Expected minimum transactions (2):
     *  Bob  → Alice  ₹100
     *  Carol → Alice ₹200
     */
    @Test
    fun `three members - two transactions settle all debts`() {
        val alice = user("a", "Alice")
        val bob   = user("b", "Bob")
        val carol = user("c", "Carol")

        val balances = listOf(
            bal("a", "Alice", 300.0),
            bal("b", "Bob",  -100.0),
            bal("c", "Carol", -200.0)
        )

        val result = useCase(balances, listOf(alice, bob, carol))

        assertEquals(2, result.size)
        assertFullySettled(balances, result)
        // Largest debtor (Carol ₹200) is matched with the single creditor first
        assertEquals("Carol", result[0].fromUserName)
        assertEquals("Alice", result[0].toUserName)
        assertEquals(200.0,   result[0].amount, 0.01)
        assertEquals("Bob",   result[1].fromUserName)
        assertEquals("Alice", result[1].toUserName)
        assertEquals(100.0,   result[1].amount, 0.01)
    }

    /**
     * Cross-debt scenario requiring cascade matching:
     *
     *  A +₹60, B −₹30, C −₹20, D −₹10
     *
     * Optimal: 3 transactions (B→A ₹30, C→A ₹20, D→A ₹10)
     */
    @Test
    fun `one creditor multiple debtors - minimum transactions`() {
        val members = listOf(user("A","A"), user("B","B"), user("C","C"), user("D","D"))
        val balances = listOf(
            bal("A","A",  60.0),
            bal("B","B", -30.0),
            bal("C","C", -20.0),
            bal("D","D", -10.0)
        )

        val result = useCase(balances, members)

        assertEquals(3, result.size)
        assertFullySettled(balances, result)
    }

    /**
     * Multiple creditors and debtors of equal size:
     *
     *  A +₹50, B +₹50, C −₹50, D −₹50
     *
     * Optimal: 2 transactions (C→A ₹50, D→B ₹50 — or equivalent pairing)
     */
    @Test
    fun `symmetric debts - minimum transactions with multiple creditors and debtors`() {
        val members = listOf(user("A","A"), user("B","B"), user("C","C"), user("D","D"))
        val balances = listOf(
            bal("A","A",  50.0),
            bal("B","B",  50.0),
            bal("C","C", -50.0),
            bal("D","D", -50.0)
        )

        val result = useCase(balances, members)

        assertEquals(2, result.size)
        assertFullySettled(balances, result)
    }

    /**
     * Uneven split requiring one transaction to span two creditors:
     *
     *  A +₹70, B +₹30, C −₹100
     *
     * Optimal: 2 transactions
     *  C → A ₹70
     *  C → B ₹30
     */
    @Test
    fun `one debtor split across two creditors`() {
        val members = listOf(user("A","A"), user("B","B"), user("C","C"))
        val balances = listOf(
            bal("A","A",  70.0),
            bal("B","B",  30.0),
            bal("C","C",-100.0)
        )

        val result = useCase(balances, members)

        assertEquals(2, result.size)
        assertFullySettled(balances, result)
        assertEquals("C", result[0].fromUserName) // largest creditor matched first
        assertEquals("A", result[0].toUserName)
        assertEquals(70.0, result[0].amount, 0.01)
    }

    /** All balances are zero — no transactions needed. */
    @Test
    fun `all settled up - no transactions produced`() {
        val members = listOf(user("A","A"), user("B","B"))
        val balances = listOf(bal("A","A", 0.0), bal("B","B", 0.0))

        val result = useCase(balances, members)

        assertTrue(result.isEmpty())
    }

    /** Floating-point amounts are rounded to 2 decimal places. */
    @Test
    fun `floating point amounts are rounded correctly`() {
        val members = listOf(user("A","A"), user("B","B"), user("C","C"))
        // 100 / 3 splits create rounding residuals
        val balances = listOf(
            bal("A","A",  66.67),
            bal("B","B", -33.33),
            bal("C","C", -33.34)
        )

        val result = useCase(balances, members)

        assertFullySettled(balances, result)
        result.forEach { t ->
            val rounded = Math.round(t.amount * 100.0) / 100.0
            assertEquals("Amount should be rounded to 2 dp", rounded, t.amount, 0.001)
        }
    }

    /** Names are resolved from the member list, not from the balance object. */
    @Test
    fun `user names are resolved from member list`() {
        val members = listOf(user("x","Xavier"), user("y","Yvonne"))
        val balances = listOf(bal("x","X", 50.0), bal("y","Y", -50.0))

        val result = useCase(balances, members)

        assertEquals(1, result.size)
        assertEquals("Yvonne",  result[0].fromUserName)
        assertEquals("Xavier",  result[0].toUserName)
    }

    /**
     * Complex 5-person group — verifies correctness at scale.
     *
     *  A +₹120, B −₹80, C +₹40, D −₹30, E −₹50
     *  Net sum = 0 ✓
     */
    @Test
    fun `complex five-person group is fully settled`() {
        val members = listOf(
            user("A","A"), user("B","B"), user("C","C"),
            user("D","D"), user("E","E")
        )
        val balances = listOf(
            bal("A","A",  120.0),
            bal("B","B",  -80.0),
            bal("C","C",   40.0),
            bal("D","D",  -30.0),
            bal("E","E",  -50.0)
        )

        val result = useCase(balances, members)

        // At most creditors(2) + debtors(3) − 1 = 4 transactions
        assertTrue("Should use at most 4 transactions", result.size <= 4)
        assertFullySettled(balances, result)
        result.forEach { t ->
            assertTrue("Amount must be positive", t.amount > 0)
        }
    }
}
