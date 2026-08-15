package com.berat.socialhabitapp.domain.model

sealed interface AuthState {
    data object Initializing : AuthState
    data class Authenticated(val userId: String, val email: String) : AuthState
    data object Unauthenticated : AuthState
}
