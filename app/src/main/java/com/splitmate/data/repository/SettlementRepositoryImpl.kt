package com.splitmate.data.repository

import com.splitmate.data.local.dao.SettlementDao
import com.splitmate.data.local.dao.UserDao
import com.splitmate.data.mapper.toDomain
import com.splitmate.data.mapper.toEntity
import com.splitmate.domain.model.Settlement
import com.splitmate.domain.repository.SettlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettlementRepositoryImpl @Inject constructor(
    private val settlementDao: SettlementDao,
    private val userDao: UserDao
) : SettlementRepository {

    override fun getSettlementsForGroup(groupId: String): Flow<List<Settlement>> {
        return settlementDao.getSettlementsForGroup(groupId).map { settlements ->
            settlements.map { entity ->
                val payer = userDao.getUserById(entity.payerId)
                val payee = userDao.getUserById(entity.payeeId)
                entity.toDomain(payer?.name ?: "Unknown", payee?.name ?: "Unknown")
            }
        }
    }

    override fun getAllSettlements(): Flow<List<Settlement>> {
        return settlementDao.getAllSettlements().map { settlements ->
            settlements.map { entity ->
                val payer = userDao.getUserById(entity.payerId)
                val payee = userDao.getUserById(entity.payeeId)
                entity.toDomain(payer?.name ?: "Unknown", payee?.name ?: "Unknown")
            }
        }
    }

    override suspend fun createSettlement(settlement: Settlement) {
        settlementDao.insertSettlement(settlement.toEntity())
    }

    override suspend fun deleteSettlement(id: String) {
        settlementDao.deleteSettlement(id)
    }

    override fun getSettlementsBetween(userId1: String, userId2: String): Flow<List<Settlement>> {
        return settlementDao.getSettlementsBetween(userId1, userId2).map { settlements ->
            settlements.map { entity ->
                val payer = userDao.getUserById(entity.payerId)
                val payee = userDao.getUserById(entity.payeeId)
                entity.toDomain(payer?.name ?: "Unknown", payee?.name ?: "Unknown")
            }
        }
    }
}
