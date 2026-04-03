package com.splitmate.di

import com.splitmate.data.repository.ExpenseRepositoryImpl
import com.splitmate.data.repository.GroupRepositoryImpl
import com.splitmate.data.repository.SettlementRepositoryImpl
import com.splitmate.data.repository.UserRepositoryImpl
import com.splitmate.domain.repository.ExpenseRepository
import com.splitmate.domain.repository.GroupRepository
import com.splitmate.domain.repository.SettlementRepository
import com.splitmate.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(impl: GroupRepositoryImpl): GroupRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindSettlementRepository(impl: SettlementRepositoryImpl): SettlementRepository
}
