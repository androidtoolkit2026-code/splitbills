package com.splitmate.domain.repository

import com.splitmate.domain.model.Settlement
import kotlinx.coroutines.flow.Flow

interface SettlementRepository {
    fun getSettlementsForGroup(groupId: String): Flow<List<Settlement>>
    fun getAllSettlements(): Flow<List<Settlement>>
    suspend fun createSettlement(settlement: Settlement)
    suspend fun deleteSettlement(id: String)
    fun getSettlementsBetween(userId1: String, userId2: String): Flow<List<Settlement>>
}
