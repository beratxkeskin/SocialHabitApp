package com.berat.socialhabitapp.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berat.socialhabitapp.domain.model.LogoutResult
import com.berat.socialhabitapp.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoggingOut: Boolean = false,
    val logoutError: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun logout() {
        _uiState.update { it.copy(isLoggingOut = true, logoutError = null) }
        viewModelScope.launch {
            val result = logoutUseCase()
            when (result) {
                is LogoutResult.Success -> {
                    _uiState.update { it.copy(isLoggingOut = false) }
                }
                is LogoutResult.Failure.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            logoutError = "İnternet bağlantısı kurulamadı."
                        )
                    }
                }
                is LogoutResult.Failure.ServerError -> {
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            logoutError = "Sunucu hatası oluştu."
                        )
                    }
                }
                is LogoutResult.Failure.Unknown -> {
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            logoutError = "Çıkış yapılırken bir hata oluştu."
                        )
                    }
                }
            }
        }
    }
}
