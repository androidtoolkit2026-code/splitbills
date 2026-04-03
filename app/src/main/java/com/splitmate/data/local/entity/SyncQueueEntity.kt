package com.splitmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val entityType: String, // USER, GROUP, EXPENSE, SETTLEMENT
    val entityId: String,
    val action: String, // CREATE, UPDATE, DELETE
    val data: String, // JSON serialized data
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)
