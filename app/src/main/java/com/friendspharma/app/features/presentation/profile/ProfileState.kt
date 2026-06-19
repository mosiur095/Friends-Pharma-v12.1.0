package com.friendspharma.app.features.presentation.profile

import com.friendspharma.app.features.data.remote.model.UserDetailsDtoData

data class ProfileState(
    val phone: String = "",
    val user: UserDetailsDtoData = UserDetailsDtoData()
)
