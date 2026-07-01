package com.example.data.repository

import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, password: String): Flow<Result<User>>
    fun register(email: String, username: String, password: String): Flow<Result<User>>
    fun logout(): Flow<Result<Unit>>
    fun getCurrentUser(): User?
    fun observeCurrentUser(): Flow<User?>
}
