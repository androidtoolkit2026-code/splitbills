package com.splitmate.data.local.dao

import androidx.room.*
import com.splitmate.data.local.entity.ExpenseEntity
import com.splitmate.data.local.entity.ExpenseSplitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY date DESC")
    fun getExpensesForGroup(groupId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getExpenseById(id: String): Flow<ExpenseEntity?>

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseSplits(splits: List<ExpenseSplitEntity>)

    @Query("DELETE FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun deleteExpenseSplits(expenseId: String)

    @Query("SELECT * FROM expense_splits WHERE expenseId = :expenseId")
    fun getExpenseSplits(expenseId: String): Flow<List<ExpenseSplitEntity>>

    @Query("SELECT * FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun getExpenseSplitsSync(expenseId: String): List<ExpenseSplitEntity>

    @Query("SELECT * FROM expenses WHERE title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%'")
    suspend fun searchExpenses(query: String): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE id IN (SELECT DISTINCT expenseId FROM expense_splits WHERE userId = :userId) OR paidById = :userId ORDER BY date DESC")
    fun getExpensesForUser(userId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expenses WHERE groupId = :groupId")
    suspend fun getTotalExpensesForGroup(groupId: String): Double?

    @Transaction
    suspend fun insertExpenseWithSplits(expense: ExpenseEntity, splits: List<ExpenseSplitEntity>) {
        insertExpense(expense)
        insertExpenseSplits(splits)
    }

    @Transaction
    suspend fun updateExpenseWithSplits(expense: ExpenseEntity, splits: List<ExpenseSplitEntity>) {
        updateExpense(expense)
        deleteExpenseSplits(expense.id)
        insertExpenseSplits(splits)
    }
}
