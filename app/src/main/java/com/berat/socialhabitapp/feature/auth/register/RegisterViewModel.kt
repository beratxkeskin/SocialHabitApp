package com.berat.socialhabitapp.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berat.socialhabitapp.domain.model.AuthResult
import com.berat.socialhabitapp.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, generalError = null) }
    }

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username, usernameError = null, generalError = null) }
    }

    fun onDisplayNameChanged(displayName: String) {
        _uiState.update { it.copy(displayName = displayName, displayNameError = null, generalError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, generalError = null) }
    }

    fun register() {
        val currentState = _uiState.value
        _uiState.update {
            it.copy(
                isLoading = true,
                emailError = null,
                usernameError = null,
                displayNameError = null,
                passwordError = null,
                generalError = null
            )
        }

        viewModelScope.launch {
            val result = registerUseCase(
                email = currentState.email,
                password = currentState.password,
                username = currentState.username,
                displayName = currentState.displayName
            )

            when (result) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                }

                is AuthResult.Failure.ValidationError -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            emailError = if (result.field == AuthResult.Failure.ValidationField.EMAIL) {
                                "Geçerli bir e-posta adresi giriniz"
                            } else null,
                            usernameError = if (result.field == AuthResult.Failure.ValidationField.USERNAME) {
                                "Kullanıcı adı 3-20 karakter olmalı ve yalnızca harf, rakam veya alt çizgi içermelidir"
                            } else null,
                            displayNameError = if (result.field == AuthResult.Failure.ValidationField.DISPLAY_NAME) {
                                "Görünen ad 2-50 karakter arasında olmalıdır"
                            } else null,
                            passwordError = if (result.field == AuthResult.Failure.ValidationField.PASSWORD) {
                                "Şifre en az 6 karakter olmalıdır"
                            } else null
                        )
                    }
                }

                is AuthResult.Failure.UserAlreadyExists -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = "Bu e-posta veya kullanıcı adı zaten kullanımda"
                        )
                    }
                }

                is AuthResult.Failure.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = "İnternet bağlantısı kurulamadı. Lütfen bağlantınızı kontrol edin."
                        )
                    }
                }

                is AuthResult.Failure.ServerError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = "Sunucu hatası oluştu. Lütfen daha sonra tekrar deneyin."
                        )
                    }
                }

                is AuthResult.Failure.Unknown -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = "Kayıt işlemi sırasında beklenmeyen bir hata oluştu."
                        )
                    }
                }
            }
        }
    }
}
