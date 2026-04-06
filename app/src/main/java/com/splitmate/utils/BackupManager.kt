package com.splitmate.utils

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.splitmate.data.local.SplitMateDatabase
import com.splitmate.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.*
import javax.inject.Inject
import javax.inject.Singleton

data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val users: List<UserEntity> = emptyList(),
    val groups: List<GroupEntity> = emptyList(),
    val groupMembers: List<GroupMemberEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val expenseSplits: List<ExpenseSplitEntity> = emptyList(),
    val settlements: List<SettlementEntity> = emptyList()
)

@Singleton
class BackupManager @Inject constructor(
    private val database: SplitMateDatabase,
    private val gson: Gson
) {
    suspend fun exportToJson(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userDao = database.userDao()
            val groupDao = database.groupDao()
            val expenseDao = database.expenseDao()
            val settlementDao = database.settlementDao()

            val backup = BackupData(
                version = 1,
                timestamp = System.currentTimeMillis(),
                users = userDao.getAllUsers().firstOrNull() ?: emptyList(),
                groups = groupDao.getAllGroups().firstOrNull() ?: emptyList(),
                groupMembers = groupDao.observeAllGroupMembers().firstOrNull() ?: emptyList(),
                expenses = expenseDao.getAllExpenses().firstOrNull() ?: emptyList(),
                expenseSplits = expenseDao.getAllExpenseSplits().firstOrNull() ?: emptyList(),
                settlements = settlementDao.getAllSettlements().firstOrNull() ?: emptyList()
            )

            val json = gson.toJson(backup)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray(Charsets.UTF_8))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromJson(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: throw IOException("Cannot read file")

            val backup = gson.fromJson(json, BackupData::class.java)

            // Import data
            backup.users.forEach { database.userDao().insertUser(it) }
            backup.groups.forEach { database.groupDao().insertGroup(it) }
            backup.groupMembers.forEach { database.groupDao().insertGroupMember(it) }
            backup.expenses.forEach { database.expenseDao().insertExpense(it) }
            backup.expenseSplits.forEach { database.expenseDao().insertExpenseSplits(listOf(it)) }
            backup.settlements.forEach { database.settlementDao().insertSettlement(it) }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
