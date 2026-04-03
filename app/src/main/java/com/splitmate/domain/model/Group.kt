package com.splitmate.domain.model

import java.util.UUID

enum class GroupType {
    TRIP, HOME, OFFICE, EVENT, OTHER
}

data class Group(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val type: GroupType = GroupType.OTHER,
    val iconName: String = "group",
    val currency: String = "INR",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val members: List<User> = emptyList(),
    val totalExpenses: Double = 0.0
)
