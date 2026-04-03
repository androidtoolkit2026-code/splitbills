package com.splitmate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "settlements",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["groupId"]),
        Index(value = ["payerId"]),
        Index(value = ["payeeId"])
    ]
)
data class SettlementEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val payerId: String,
    val payeeId: String,
    val amount: Double,
    val currency: String = "INR",
    val paymentMethod: String = "CASH",
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
