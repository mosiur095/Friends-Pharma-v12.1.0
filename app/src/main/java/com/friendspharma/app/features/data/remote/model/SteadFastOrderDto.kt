package com.friendspharma.app.features.data.remote.model

data class SteadFastOrderDto(
    val consignment: Consignment,
    val message: String,
    val status: Int
)