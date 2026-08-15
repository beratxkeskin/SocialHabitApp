package com.berat.socialhabitapp.domain.repository

import com.berat.socialhabitapp.domain.model.AuthResult
import com.berat.socialhabitapp.domain.model.AuthState
import com.berat.socialhabitapp.domain.model.LogoutResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(
        email: String,
        password: String,
        username: String,
        displayName: String,
        timezone: String
    ): AuthResult

    suspend fun login(
        email: String,
        password: String
    ): AuthResult

    suspend fun logout(): LogoutResult

    fun getAuthState(): Flow<AuthState>
}
