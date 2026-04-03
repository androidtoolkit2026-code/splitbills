package com.splitmate.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.splitmate.data.local.dao.ExpenseDao
import com.splitmate.data.local.dao.GroupDao
import com.splitmate.data.local.dao.SettlementDao
import com.splitmate.data.local.dao.UserDao
import com.splitmate.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        ExpenseEntity::class,
        ExpenseSplitEntity::class,
        SettlementEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SplitMateDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun settlementDao(): SettlementDao

    companion object {
        const val DATABASE_NAME = "splitmate.db"
    }
}
