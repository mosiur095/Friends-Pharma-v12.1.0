package com.friendspharma.app.features.presentation.reset_password

data class ResetPasswordState(
    val mobile: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isValidate: Boolean = false,
    val valid: Boolean = false,
    val isLoading: Boolean = false,
    val showDialogue : Boolean = false,
    val showError : Boolean = false,
    val message: String = "",
    val userId: String = ""
)
