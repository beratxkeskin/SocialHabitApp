package com.berat.socialhabitapp.domain.usecase

import com.berat.socialhabitapp.domain.model.LogoutResult
import com.berat.socialhabitapp.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): LogoutResult {
        return authRepository.logout()
    }
}
