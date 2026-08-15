package com.berat.socialhabitapp.domain.usecase

import com.berat.socialhabitapp.core.util.TimezoneProvider
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

class RegisterUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var timezoneProvider: TimezoneProvider
    private lateinit var registerUseCase: RegisterUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        timezoneProvider = object : TimezoneProvider {
            override fun getTimezone(): String = "Europe/Istanbul"
        }
        registerUseCase = RegisterUseCase(authRepository, timezoneProvider)
    }

    @Test
    fun `invoke with valid inputs registers user successfully with canonical username and timezone`() = runTest {
        coEvery {
            authRepository.register(
                email = "berat@example.com",
                password = "password123",
                username = "berat_dev",
                displayName = "Berat Keskin",
                timezone = "Europe/Istanbul"
            )
        } returns AuthResult.Success(userId = "uuid-123", email = "berat@example.com")

        val result = registerUseCase(
            email = "  berat@example.com  ",
            password = "password123",
            username = "  Berat_Dev  ",
            displayName = "  Berat Keskin  "
        )

        assertTrue(result is AuthResult.Success)
        val success = result as AuthResult.Success
        assertEquals("uuid-123", success.userId)
        assertEquals("berat@example.com", success.email)

        coVerify(exactly = 1) {
            authRepository.register(
                email = "berat@example.com",
                password = "password123",
                username = "berat_dev",
                displayName = "Berat Keskin",
                timezone = "Europe/Istanbul"
            )
        }
    }

    @Test
    fun `invoke with invalid email returns validation error without calling repository`() = runTest {
        val result = registerUseCase(
            email = "invalid-email",
            password = "password123",
            username = "berat_dev",
            displayName = "Berat"
        )

        assertEquals(
            AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.EMAIL),
            result
        )
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke with short username returns validation error`() = runTest {
        val result = registerUseCase(
            email = "berat@example.com",
            password = "password123",
            username = "ab",
            displayName = "Berat"
        )

        assertEquals(
            AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.USERNAME),
            result
        )
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke with username containing invalid characters returns validation error`() = runTest {
        val result = registerUseCase(
            email = "berat@example.com",
            password = "password123",
            username = "berat.dev!",
            displayName = "Berat"
        )

        assertEquals(
            AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.USERNAME),
            result
        )
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke with short display name returns validation error`() = runTest {
        val result = registerUseCase(
            email = "berat@example.com",
            password = "password123",
            username = "berat_dev",
            displayName = "A"
        )

        assertEquals(
            AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.DISPLAY_NAME),
            result
        )
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke with short password returns validation error`() = runTest {
        val result = registerUseCase(
            email = "berat@example.com",
            password = "12345",
            username = "berat_dev",
            displayName = "Berat Keskin"
        )

        assertEquals(
            AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.PASSWORD),
            result
        )
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke propagates repository failure when user already exists`() = runTest {
        coEvery {
            authRepository.register(any(), any(), any(), any(), any())
        } returns AuthResult.Failure.UserAlreadyExists

        val result = registerUseCase(
            email = "berat@example.com",
            password = "password123",
            username = "berat_dev",
            displayName = "Berat Keskin"
        )

        assertEquals(AuthResult.Failure.UserAlreadyExists, result)
    }
}
