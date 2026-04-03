package com.splitmate.domain.repository

import com.splitmate.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    fun getAllUsers(): Flow<List<User>>
    suspend fun getUserById(id: String): User?
    suspend fun createUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(id: String)
    suspend fun setCurrentUser(userId: String)
    suspend fun searchUsers(query: String): List<User>
}
