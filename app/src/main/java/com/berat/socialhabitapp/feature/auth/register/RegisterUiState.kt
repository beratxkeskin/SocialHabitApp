package com.berat.socialhabitapp.feature.auth.register

data class RegisterUiState(
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val usernameError: String? = null,
    val displayNameError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isSuccess: Boolean = false
)
