package com.friendspharma.app.features.data.remote.entity

data class Otp(
    val action: String = "Forget",
    val msisdn: String,
    val servicename: String = "FriendsPharma",
    val user: String = "v44\$\$mMss"
)