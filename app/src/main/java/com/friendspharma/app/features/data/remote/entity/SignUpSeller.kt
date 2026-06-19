package com.friendspharma.app.features.data.remote.entity

data class SignUpSeller(
    val address: String,
    val email: String = "",
    val mobileNo: String,
    val passWordNo: String,
    val userName: String,
    val usertype: String,
    val drugno: String = ""
)