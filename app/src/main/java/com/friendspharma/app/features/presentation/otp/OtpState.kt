package com.friendspharma.app.features.presentation.otp

data class OtpState(
    val phone: String = "",
    val otp: String = "",
    val isValidate: Boolean = false,
    val valid: Boolean = false,
    val isLoading: Boolean = false,
    val showSnackBar : Boolean = false,
    val message: String = "",
    val time: String = ""
)
