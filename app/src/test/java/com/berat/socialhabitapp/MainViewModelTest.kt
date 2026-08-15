package com.berat.socialhabitapp

import app.cash.turbine.test
import com.berat.socialhabitapp.domain.model.AuthState
import com.berat.socialhabitapp.domain.usecase.GetAuthStateUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getAuthStateUseCase: GetAuthStateUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAuthStateUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `authState emits Initializing and then Authenticated when session exists`() = runTest {
        every { getAuthStateUseCase() } returns flowOf(
            AuthState.Authenticated(userId = "user-123", email = "test@example.com")
        )

        val viewModel = MainViewModel(getAuthStateUseCase)

        viewModel.authState.test {
            assertEquals(AuthState.Initializing, awaitItem())
            assertEquals(
                AuthState.Authenticated(userId = "user-123", email = "test@example.com"),
                awaitItem()
            )
        }
    }

    @Test
    fun `authState emits Initializing and then Unauthenticated when no session exists`() = runTest {
        every { getAuthStateUseCase() } returns flowOf(AuthState.Unauthenticated)

        val viewModel = MainViewModel(getAuthStateUseCase)

        viewModel.authState.test {
            assertEquals(AuthState.Initializing, awaitItem())
            assertEquals(AuthState.Unauthenticated, awaitItem())
        }
    }
}
