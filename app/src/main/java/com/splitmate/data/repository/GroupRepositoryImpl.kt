package com.splitmate.data.repository

import com.splitmate.data.local.dao.ExpenseDao
import com.splitmate.data.local.dao.GroupDao
import com.splitmate.data.local.dao.UserDao
import com.splitmate.data.mapper.toDomain
import com.splitmate.data.mapper.toEntity
import com.splitmate.domain.model.Group
import com.splitmate.domain.repository.GroupRepository
import com.splitmate.data.local.entity.GroupMemberEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val userDao: UserDao,
    private val expenseDao: ExpenseDao
) : GroupRepository {

    override fun getAllGroups(): Flow<List<Group>> {
        return combine(
            groupDao.getAllGroups(),
            groupDao.observeAllGroupMembers()
        ) { groups, _ ->
            groups.map { groupEntity ->
                val memberIds = groupDao.getGroupMemberIdsSync(groupEntity.id)
                val members = userDao.getUsersByIds(memberIds).map { it.toDomain() }
                val total = expenseDao.getTotalExpensesForGroup(groupEntity.id) ?: 0.0
                groupEntity.toDomain(members, total)
            }
        }
    }

    override fun getGroupById(id: String): Flow<Group?> {
        return combine(
            groupDao.getGroupById(id),
            groupDao.getGroupMemberIds(id)
        ) { groupEntity, memberIds ->
            groupEntity?.let {
                val members = userDao.getUsersByIds(memberIds).map { u -> u.toDomain() }
                val total = expenseDao.getTotalExpensesForGroup(it.id) ?: 0.0
                it.toDomain(members, total)
            }
        }
    }

    override fun getGroupsForUser(userId: String): Flow<List<Group>> {
        return combine(
            groupDao.getGroupsForUser(userId),
            groupDao.observeAllGroupMembers()
        ) { groups, _ ->
            groups.map { groupEntity ->
                val memberIds = groupDao.getGroupMemberIdsSync(groupEntity.id)
                val members = userDao.getUsersByIds(memberIds).map { it.toDomain() }
                val total = expenseDao.getTotalExpensesForGroup(groupEntity.id) ?: 0.0
                groupEntity.toDomain(members, total)
            }
        }
    }

    override suspend fun createGroup(group: Group) {
        groupDao.insertGroup(group.toEntity())
        // Add members
        group.members.forEach { member ->
            groupDao.insertGroupMember(
                GroupMemberEntity(groupId = group.id, userId = member.id)
            )
        }
    }

    override suspend fun updateGroup(group: Group) {
        groupDao.updateGroup(group.toEntity())
    }

    override suspend fun deleteGroup(id: String) {
        groupDao.deleteGroup(id)
    }

    override suspend fun addMemberToGroup(groupId: String, userId: String) {
        groupDao.insertGroupMember(GroupMemberEntity(groupId = groupId, userId = userId))
    }

    override suspend fun removeMemberFromGroup(groupId: String, userId: String) {
        groupDao.removeGroupMember(groupId, userId)
    }

    override fun getGroupMembers(groupId: String): Flow<List<String>> {
        return groupDao.getGroupMemberIds(groupId)
    }

    override suspend fun searchGroups(query: String): List<Group> {
        return groupDao.searchGroups(query).map { it.toDomain() }
    }
}
