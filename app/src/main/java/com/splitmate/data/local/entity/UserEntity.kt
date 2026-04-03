package com.splitmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String = "",
    val defaultCurrency: String = "INR",
    val firebaseUid: String? = null,
    val isCurrentUser: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
