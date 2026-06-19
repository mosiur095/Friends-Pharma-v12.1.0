package com.friendspharma.app.features.data.remote.entity

data class SteadFastOrder(
    val invoice: String,
    val recipient_name : String,
    val recipient_phone: String,
    val recipient_address : String,
    val cod_amount: Double,
    val note: String = "",
)
