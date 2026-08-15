package com.berat.socialhabitapp.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berat.socialhabitapp.domain.model.AuthResult
import com.berat.socialhabitapp.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, generalError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, generalError = null) }
    }

    fun login() {
        val currentState = _uiState.value
        _uiState.update {
            it.copy(
                isLoading = true,
                emailError = null,
                passwordError = null,
                generalError = null
            )
        }

        viewModelScope.launch {
            val result = loginUseCase(
                email = currentState.email,
                password = currentState.password
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
                            passwordError = if (result.field == AuthResult.Failure.ValidationField.PASSWORD) {
                                "Şifre alanı boş bırakılamaz"
                            } else null
                        )
                    }
                }

                is AuthResult.Failure.InvalidCredentials -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = "E-posta veya şifre hatalı."
                        )
                    }
                }

                is AuthResult.Failure.EmailNotConfirmed -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = "E-posta adresiniz henüz doğrulanmamış. Lütfen gelen kutunuzdaki onay bağlantısını kontrol edin."
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

                is AuthResult.Failure.UserAlreadyExists -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = "Hesap hatası oluştu."
                        )
                    }
                }

                is AuthResult.Failure.Unknown -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = "Giriş yapılırken beklenmeyen bir hata oluştu."
                        )
                    }
                }
            }
        }
    }
}
