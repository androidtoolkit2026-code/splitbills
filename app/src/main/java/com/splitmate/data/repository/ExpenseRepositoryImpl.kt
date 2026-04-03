package com.splitmate.data.repository

import com.splitmate.data.local.dao.ExpenseDao
import com.splitmate.data.local.dao.UserDao
import com.splitmate.data.mapper.toDomain
import com.splitmate.data.mapper.toEntity
import com.splitmate.domain.model.Expense
import com.splitmate.domain.model.ExpenseSplit
import com.splitmate.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val userDao: UserDao
) : ExpenseRepository {

    override fun getExpensesForGroup(groupId: String): Flow<List<Expense>> {
        return expenseDao.getExpensesForGroup(groupId).map { expenses ->
            expenses.map { entity ->
                val splits = expenseDao.getExpenseSplitsSync(entity.id).map { splitEntity ->
                    val user = userDao.getUserById(splitEntity.userId)
                    splitEntity.toDomain(user?.name ?: "Unknown")
                }
                entity.toDomain(splits)
            }
        }
    }

    override fun getExpenseById(id: String): Flow<Expense?> {
        return expenseDao.getExpenseById(id).map { entity ->
            entity?.let {
                val splits = expenseDao.getExpenseSplitsSync(it.id).map { splitEntity ->
                    val user = userDao.getUserById(splitEntity.userId)
                    splitEntity.toDomain(user?.name ?: "Unknown")
                }
                it.toDomain(splits)
            }
        }
    }

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses().map { expenses ->
            expenses.map { entity ->
                val splits = expenseDao.getExpenseSplitsSync(entity.id).map { splitEntity ->
                    val user = userDao.getUserById(splitEntity.userId)
                    splitEntity.toDomain(user?.name ?: "Unknown")
                }
                entity.toDomain(splits)
            }
        }
    }

    override suspend fun createExpense(expense: Expense, splits: List<ExpenseSplit>) {
        expenseDao.insertExpenseWithSplits(
            expense.toEntity(),
            splits.map { it.toEntity() }
        )
    }

    override suspend fun updateExpense(expense: Expense, splits: List<ExpenseSplit>) {
        expenseDao.updateExpenseWithSplits(
            expense.toEntity(),
            splits.map { it.toEntity() }
        )
    }

    override suspend fun deleteExpense(id: String) {
        expenseDao.deleteExpense(id)
    }

    override fun getExpenseSplits(expenseId: String): Flow<List<ExpenseSplit>> {
        return expenseDao.getExpenseSplits(expenseId).map { splits ->
            splits.map { splitEntity ->
                val user = userDao.getUserById(splitEntity.userId)
                splitEntity.toDomain(user?.name ?: "Unknown")
            }
        }
    }

    override suspend fun searchExpenses(query: String): List<Expense> {
        return expenseDao.searchExpenses(query).map { it.toDomain() }
    }

    override fun getExpensesForUser(userId: String): Flow<List<Expense>> {
        return expenseDao.getExpensesForUser(userId).map { expenses ->
            expenses.map { entity ->
                val splits = expenseDao.getExpenseSplitsSync(entity.id).map { splitEntity ->
                    val user = userDao.getUserById(splitEntity.userId)
                    splitEntity.toDomain(user?.name ?: "Unknown")
                }
                entity.toDomain(splits)
            }
        }
    }
}
