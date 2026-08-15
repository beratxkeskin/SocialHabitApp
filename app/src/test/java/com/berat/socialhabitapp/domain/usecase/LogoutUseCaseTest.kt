package com.berat.socialhabitapp.domain.usecase

import com.berat.socialhabitapp.domain.model.LogoutResult
import com.berat.socialhabitapp.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LogoutUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var logoutUseCase: LogoutUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        logoutUseCase = LogoutUseCase(authRepository)
    }

    @Test
    fun `invoke calls repository logout and returns success`() = runTest {
        coEvery { authRepository.logout() } returns LogoutResult.Success

        val result = logoutUseCase()

        assertEquals(LogoutResult.Success, result)
        coVerify(exactly = 1) { authRepository.logout() }
    }

    @Test
    fun `invoke calls repository logout and returns failure on network error`() = runTest {
        coEvery { authRepository.logout() } returns LogoutResult.Failure.NetworkError

        val result = logoutUseCase()

        assertEquals(LogoutResult.Failure.NetworkError, result)
        coVerify(exactly = 1) { authRepository.logout() }
    }
}
