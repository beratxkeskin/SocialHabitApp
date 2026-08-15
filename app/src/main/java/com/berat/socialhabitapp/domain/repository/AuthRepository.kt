package com.berat.socialhabitapp.domain.repository

import com.berat.socialhabitapp.domain.model.AuthResult

interface AuthRepository {
    suspend fun register(
        email: String,
        password: String,
        username: String,
        displayName: String,
        timezone: String
    ): AuthResult
}
