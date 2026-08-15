package com.berat.socialhabitapp.feature.auth.login

import app.cash.turbine.test
import com.berat.socialhabitapp.domain.model.AuthResult
import com.berat.socialhabitapp.domain.usecase.LoginUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mockk()
        viewModel = LoginViewModel(loginUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is default empty and not loading`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.generalError)
    }

    @Test
    fun `onEmailChanged and onPasswordChanged update state`() = runTest {
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("pass123")

        val state = viewModel.uiState.value
        assertEquals("test@example.com", state.email)
        assertEquals("pass123", state.password)
    }

    @Test
    fun `login success updates state to isSuccess true and isLoading false`() = runTest {
        coEvery {
            loginUseCase("test@example.com", "password123")
        } returns AuthResult.Success(userId = "user-123", email = "test@example.com")

        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("password123")

        viewModel.uiState.test {
            assertEquals(
                LoginUiState(email = "test@example.com", password = "password123"),
                awaitItem()
            )

            viewModel.login()

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertTrue(successState.isSuccess)
            assertNull(successState.generalError)
        }
    }

    @Test
    fun `login validation error updates field specific error`() = runTest {
        coEvery {
            loginUseCase(any(), any())
        } returns AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.EMAIL)

        viewModel.onEmailChanged("invalid")
        viewModel.login()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNotNull(state.emailError)
        assertNull(state.generalError)
    }

    @Test
    fun `login InvalidCredentials failure updates generalError`() = runTest {
        coEvery {
            loginUseCase(any(), any())
        } returns AuthResult.Failure.InvalidCredentials

        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("E-posta veya şifre hatalı.", state.generalError)
    }

    @Test
    fun `login EmailNotConfirmed failure updates generalError with confirmation message`() = runTest {
        coEvery {
            loginUseCase(any(), any())
        } returns AuthResult.Failure.EmailNotConfirmed

        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertTrue(state.generalError?.contains("doğrulanmamış") == true)
    }

    @Test
    fun `login NetworkError failure updates generalError with network message`() = runTest {
        coEvery {
            loginUseCase(any(), any())
        } returns AuthResult.Failure.NetworkError

        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("İnternet bağlantısı kurulamadı. Lütfen bağlantınızı kontrol edin.", state.generalError)
    }
}
