package com.friendspharma.app.features.data.remote.model

data class LoginDto(
    val `data`: List<UserDto>,
    val message: String,
    val status: Int
)