package com.friendspharma.app.features.presentation.update_password

import com.friendspharma.app.features.data.remote.model.UserDto

data class UpdatePasswordState(
    val isLoading: Boolean = false,
    val oldPassword: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isValidate: Boolean = false,
    val valid: Boolean = false,
    val showPassword: Boolean = false,
    val showConfirmPassword: Boolean = false,
    val user: UserDto = UserDto()
)
