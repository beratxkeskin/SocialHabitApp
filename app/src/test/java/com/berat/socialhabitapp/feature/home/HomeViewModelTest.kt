package com.berat.socialhabitapp.feature.home

import app.cash.turbine.test
import com.berat.socialhabitapp.domain.model.LogoutResult
import com.berat.socialhabitapp.domain.usecase.LogoutUseCase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        logoutUseCase = mockk()
        viewModel = HomeViewModel(logoutUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is default not logging out without error`() = runTest {
        val state = viewModel.uiState.value
        assertFalse(state.isLoggingOut)
        assertNull(state.logoutError)
    }

    @Test
    fun `logout success updates state to isLoggingOut false`() = runTest {
        coEvery { logoutUseCase() } returns LogoutResult.Success

        viewModel.uiState.test {
            assertEquals(HomeUiState(), awaitItem())

            viewModel.logout()

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoggingOut)

            testDispatcher.scheduler.advanceUntilIdle()

            val idleState = awaitItem()
            assertFalse(idleState.isLoggingOut)
            assertNull(idleState.logoutError)
        }
    }

    @Test
    fun `logout failure updates state with logoutError`() = runTest {
        coEvery { logoutUseCase() } returns LogoutResult.Failure.NetworkError

        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoggingOut)
        assertEquals("İnternet bağlantısı kurulamadı.", state.logoutError)
    }
}
