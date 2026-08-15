package com.berat.socialhabitapp.domain.usecase

import com.berat.socialhabitapp.core.util.TimezoneProvider
import com.berat.socialhabitapp.domain.model.AuthResult
import com.berat.socialhabitapp.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val timezoneProvider: TimezoneProvider
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        username: String,
        displayName: String
    ): AuthResult {
        val trimmedEmail = email.trim()
        if (!isValidEmail(trimmedEmail)) {
            return AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.EMAIL)
        }

        val trimmedUsername = username.trim()
        if (!isValidUsername(trimmedUsername)) {
            return AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.USERNAME)
        }

        val trimmedDisplayName = displayName.trim()
        if (trimmedDisplayName.length !in 2..50) {
            return AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.DISPLAY_NAME)
        }

        if (password.length < 6) {
            return AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.PASSWORD)
        }

        val canonicalUsername = trimmedUsername.lowercase()
        val timezone = timezoneProvider.getTimezone()

        return authRepository.register(
            email = trimmedEmail,
            password = password,
            username = canonicalUsername,
            displayName = trimmedDisplayName,
            timezone = timezone
        )
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank() || email.contains(" ")) return false
        val atIndex = email.indexOf('@')
        return atIndex > 0 && atIndex == email.lastIndexOf('@') && atIndex < email.length - 1
    }

    private fun isValidUsername(username: String): Boolean {
        return username.matches(Regex("^[a-zA-Z0-9_]{3,20}$"))
    }
}
