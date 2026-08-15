package com.berat.socialhabitapp.domain.usecase

import com.berat.socialhabitapp.domain.model.AuthState
import com.berat.socialhabitapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthState> {
        return authRepository.getAuthState()
    }
}
