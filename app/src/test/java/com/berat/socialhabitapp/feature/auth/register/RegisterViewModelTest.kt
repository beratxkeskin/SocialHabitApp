package com.berat.socialhabitapp.feature.auth.register

import app.cash.turbine.test
import com.berat.socialhabitapp.domain.model.AuthResult
import com.berat.socialhabitapp.domain.usecase.RegisterUseCase
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
class RegisterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var registerUseCase: RegisterUseCase
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        registerUseCase = mockk()
        viewModel = RegisterViewModel(registerUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is default empty and not loading`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.username)
        assertEquals("", state.displayName)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.generalError)
    }

    @Test
    fun `onEmailChanged updates email state and clears errors`() = runTest {
        viewModel.onEmailChanged("test@example.com")
        assertEquals("test@example.com", viewModel.uiState.value.email)
    }

    @Test
    fun `register success updates state to isSuccess true and isLoading false`() = runTest {
        coEvery {
            registerUseCase("test@example.com", "password123", "testuser", "Test User")
        } returns AuthResult.Success(userId = "user-123", email = "test@example.com")

        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onUsernameChanged("testuser")
        viewModel.onDisplayNameChanged("Test User")

        viewModel.uiState.test {
            assertEquals(
                RegisterUiState(
                    email = "test@example.com",
                    password = "password123",
                    username = "testuser",
                    displayName = "Test User"
                ),
                awaitItem()
            )

            viewModel.register()

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
    fun `register validation error updates field specific error`() = runTest {
        coEvery {
            registerUseCase(any(), any(), any(), any())
        } returns AuthResult.Failure.ValidationError(AuthResult.Failure.ValidationField.EMAIL)

        viewModel.onEmailChanged("invalid")
        viewModel.register()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNotNull(state.emailError)
        assertNull(state.generalError)
    }

    @Test
    fun `register UserAlreadyExists failure updates generalError`() = runTest {
        coEvery {
            registerUseCase(any(), any(), any(), any())
        } returns AuthResult.Failure.UserAlreadyExists

        viewModel.register()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("Bu e-posta veya kullanıcı adı zaten kullanımda", state.generalError)
    }

    @Test
    fun `register NetworkError failure updates generalError`() = runTest {
        coEvery {
            registerUseCase(any(), any(), any(), any())
        } returns AuthResult.Failure.NetworkError

        viewModel.register()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("İnternet bağlantısı kurulamadı. Lütfen bağlantınızı kontrol edin.", state.generalError)
    }
}
