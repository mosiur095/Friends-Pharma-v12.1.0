package com.friendspharma.app.features.data.remote.model

data class OrdersDto(
    val `data`: List<OrdersDtoItem>? = null,
    val message: String? = null,
    val status: String? = null
)