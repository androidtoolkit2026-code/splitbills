package com.splitmate.domain.repository

import com.splitmate.domain.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getAllGroups(): Flow<List<Group>>
    fun getGroupById(id: String): Flow<Group?>
    fun getGroupsForUser(userId: String): Flow<List<Group>>
    suspend fun createGroup(group: Group)
    suspend fun updateGroup(group: Group)
    suspend fun deleteGroup(id: String)
    suspend fun addMemberToGroup(groupId: String, userId: String)
    suspend fun removeMemberFromGroup(groupId: String, userId: String)
    fun getGroupMembers(groupId: String): Flow<List<String>>
    suspend fun searchGroups(query: String): List<Group>
}
