package com.berat.socialhabitapp.domain.usecase

import com.berat.socialhabitapp.domain.model.AuthResult
import com.berat.socialhabitapp.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        loginUseCase = LoginUseCase(authRepository)
    }

    @Test
    fun `invoke with valid credentials calls repository and returns success`() = runTest {
        coEvery {
            authRepository.login("berat@example.com", "password123")
        } returns AuthResult.Success(userId = "user-123", email = "berat@example.com")

        val result = loginUseCase("  berat@example.com  ", "password123")

        assertTrue(result is AuthResult.Success)
        val success = result as AuthResult.Success
        assertEquals("user-123", success.userId)
        assertEquals("berat@example.com", success.email)

        coVerify(exactly = 1) { authRepository.login("berat@example.com", "password123") }
    }

    @Test
    fun `invoke with invalid email returns validation error without calling repository`() = runTest {
        val result = loginUseCase("invalid-email", "password123")

        assertEquals(
            AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.EMAIL),
            result
        )
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `invoke with blank password returns validation error without calling repository`() = runTest {
        val result = loginUseCase("berat@example.com", "   ")

        assertEquals(
            AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.PASSWORD),
            result
        )
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `invoke propagates repository InvalidCredentials failure`() = runTest {
        coEvery {
            authRepository.login(any(), any())
        } returns AuthResult.Failure.InvalidCredentials

        val result = loginUseCase("berat@example.com", "wrongpassword")

        assertEquals(AuthResult.Failure.InvalidCredentials, result)
    }

    @Test
    fun `invoke propagates repository EmailNotConfirmed failure`() = runTest {
        coEvery {
            authRepository.login(any(), any())
        } returns AuthResult.Failure.EmailNotConfirmed

        val result = loginUseCase("berat@example.com", "password123")

        assertEquals(AuthResult.Failure.EmailNotConfirmed, result)
    }

    @Test
    fun `invoke propagates repository NetworkError failure`() = runTest {
        coEvery {
            authRepository.login(any(), any())
        } returns AuthResult.Failure.NetworkError

        val result = loginUseCase("berat@example.com", "password123")

        assertEquals(AuthResult.Failure.NetworkError, result)
    }
}
