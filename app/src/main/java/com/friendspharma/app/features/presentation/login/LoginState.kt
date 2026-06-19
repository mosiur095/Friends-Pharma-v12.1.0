package com.friendspharma.app.features.presentation.login

enum class RestrictionType {
    NONE,
    BLOCKED,
    PENDING_APPROVAL
}

data class LoginState(
    val isLoading: Boolean = false,
    val mobile: String = "",
    val message: String = "",
    val isValidate: Boolean = false,
    val valid: Boolean = false,
    val rememberMe: Boolean = false,
    val password: String = "",
    val showPassword: Boolean = false,
    val success: Boolean = false,
    val restrictionType: RestrictionType = RestrictionType.NONE
)