package com.berat.socialhabitapp.domain.usecase

import com.berat.socialhabitapp.domain.model.AuthResult
import com.berat.socialhabitapp.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): AuthResult {
        val trimmedEmail = email.trim()
        if (!isValidEmail(trimmedEmail)) {
            return AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.EMAIL)
        }

        if (password.isBlank()) {
            return AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.PASSWORD)
        }

        return authRepository.login(
            email = trimmedEmail,
            password = password
        )
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank() || email.contains(" ")) return false
        val atIndex = email.indexOf('@')
        return atIndex > 0 && atIndex == email.lastIndexOf('@') && atIndex < email.length - 1
    }
}
