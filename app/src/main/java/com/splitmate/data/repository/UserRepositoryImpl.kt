package com.splitmate.data.repository

import com.splitmate.data.local.dao.UserDao
import com.splitmate.data.mapper.toDomain
import com.splitmate.data.mapper.toEntity
import com.splitmate.domain.model.User
import com.splitmate.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> {
        return userDao.getCurrentUser().map { it?.toDomain() }
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { users -> users.map { it.toDomain() } }
    }

    override suspend fun getUserById(id: String): User? {
        return userDao.getUserById(id)?.toDomain()
    }

    override suspend fun createUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
    }

    override suspend fun deleteUser(id: String) {
        userDao.deleteUser(id)
    }

    override suspend fun setCurrentUser(userId: String) {
        userDao.clearCurrentUser()
        userDao.setCurrentUser(userId)
    }

    override suspend fun searchUsers(query: String): List<User> {
        return userDao.searchUsers(query).map { it.toDomain() }
    }
}
