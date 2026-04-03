package com.splitmate.di

import android.content.Context
import androidx.room.Room
import com.splitmate.data.local.SplitMateDatabase
import com.splitmate.data.local.dao.ExpenseDao
import com.splitmate.data.local.dao.GroupDao
import com.splitmate.data.local.dao.SettlementDao
import com.splitmate.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SplitMateDatabase {
        return Room.databaseBuilder(
            context,
            SplitMateDatabase::class.java,
            SplitMateDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(db: SplitMateDatabase): UserDao = db.userDao()

    @Provides
    fun provideGroupDao(db: SplitMateDatabase): GroupDao = db.groupDao()

    @Provides
    fun provideExpenseDao(db: SplitMateDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideSettlementDao(db: SplitMateDatabase): SettlementDao = db.settlementDao()
}
