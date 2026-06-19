package com.friendspharma.app.features.presentation.main

data class MainState(
    val splash: Boolean = true,
    val unreadCount: Int = 0,
)