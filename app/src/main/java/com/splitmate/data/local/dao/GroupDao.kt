package com.splitmate.data.local.dao

import androidx.room.*
import com.splitmate.data.local.entity.GroupEntity
import com.splitmate.data.local.entity.GroupMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Query("SELECT * FROM `groups` ORDER BY updatedAt DESC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM `groups` WHERE id = :id")
    fun getGroupById(id: String): Flow<GroupEntity?>

    @Query("SELECT * FROM `groups` WHERE id IN (SELECT groupId FROM group_members WHERE userId = :userId) ORDER BY updatedAt DESC")
    fun getGroupsForUser(userId: String): Flow<List<GroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Query("DELETE FROM `groups` WHERE id = :id")
    suspend fun deleteGroup(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(member: GroupMemberEntity)

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun removeGroupMember(groupId: String, userId: String)

    @Query("SELECT userId FROM group_members WHERE groupId = :groupId")
    fun getGroupMemberIds(groupId: String): Flow<List<String>>

    @Query("SELECT userId FROM group_members WHERE groupId = :groupId")
    suspend fun getGroupMemberIdsSync(groupId: String): List<String>

    @Query("SELECT * FROM `groups` WHERE name LIKE '%' || :query || '%'")
    suspend fun searchGroups(query: String): List<GroupEntity>

    @Query("SELECT COUNT(*) FROM group_members WHERE groupId = :groupId")
    suspend fun getMemberCount(groupId: String): Int

    @Query("SELECT * FROM group_members")
    fun observeAllGroupMembers(): Flow<List<GroupMemberEntity>>
}
