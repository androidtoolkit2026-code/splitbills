package com.splitmate.domain.model

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String = "",
    val defaultCurrency: String = "INR",
    val firebaseUid: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
