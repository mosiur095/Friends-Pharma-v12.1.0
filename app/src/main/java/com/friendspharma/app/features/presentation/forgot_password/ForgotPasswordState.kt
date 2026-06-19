package com.friendspharma.app.features.presentation.forgot_password

data class ForgotPasswordState(
    val mobile: String = "",
    val isValidate: Boolean = false,
    val valid: Boolean = false,
    val isLoading: Boolean = false,
    val showSnackBar : Boolean = false,
    val message: String = ""
)
