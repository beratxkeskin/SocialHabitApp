package com.berat.socialhabitapp.domain.model

sealed interface AuthResult {
    data class Success(
        val userId: String,
        val email: String
    ) : AuthResult

    sealed interface Failure : AuthResult {
        enum class ValidationField {
            EMAIL,
            USERNAME,
            DISPLAY_NAME,
            PASSWORD
        }

        data class ValidationError(val field: ValidationField) : Failure
        data object UserAlreadyExists : Failure
        data object NetworkError : Failure
        data object ServerError : Failure
        data object Unknown : Failure
    }
}
