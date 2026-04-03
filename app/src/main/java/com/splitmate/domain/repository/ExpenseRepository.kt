package com.splitmate.domain.repository

import com.splitmate.domain.model.Expense
import com.splitmate.domain.model.ExpenseSplit
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getExpensesForGroup(groupId: String): Flow<List<Expense>>
    fun getExpenseById(id: String): Flow<Expense?>
    fun getAllExpenses(): Flow<List<Expense>>
    suspend fun createExpense(expense: Expense, splits: List<ExpenseSplit>)
    suspend fun updateExpense(expense: Expense, splits: List<ExpenseSplit>)
    suspend fun deleteExpense(id: String)
    fun getExpenseSplits(expenseId: String): Flow<List<ExpenseSplit>>
    suspend fun searchExpenses(query: String): List<Expense>
    fun getExpensesForUser(userId: String): Flow<List<Expense>>
}
