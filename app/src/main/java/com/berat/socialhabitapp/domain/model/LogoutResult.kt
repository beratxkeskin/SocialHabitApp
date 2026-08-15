package com.berat.socialhabitapp.domain.model

sealed interface LogoutResult {
    data object Success : LogoutResult
    sealed interface Failure : LogoutResult {
        data object NetworkError : Failure
        data object ServerError : Failure
        data object Unknown : Failure
    }
}
