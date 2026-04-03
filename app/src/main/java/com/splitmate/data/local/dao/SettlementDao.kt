package com.splitmate.data.local.dao

import androidx.room.*
import com.splitmate.data.local.entity.SettlementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettlementDao {

    @Query("SELECT * FROM settlements WHERE groupId = :groupId ORDER BY date DESC")
    fun getSettlementsForGroup(groupId: String): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM settlements ORDER BY date DESC")
    fun getAllSettlements(): Flow<List<SettlementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: SettlementEntity)

    @Query("DELETE FROM settlements WHERE id = :id")
    suspend fun deleteSettlement(id: String)

    @Query("""
        SELECT * FROM settlements 
        WHERE (payerId = :userId1 AND payeeId = :userId2) 
           OR (payerId = :userId2 AND payeeId = :userId1) 
        ORDER BY date DESC
    """)
    fun getSettlementsBetween(userId1: String, userId2: String): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM settlements WHERE groupId = :groupId")
    suspend fun getSettlementsForGroupSync(groupId: String): List<SettlementEntity>
}
