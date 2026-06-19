package com.friendspharma.app.features.data.remote.model

data class OrderDetailsDto(
    val `data`: List<OrderDetailsDtoItem>? = null,
    val message: String? = null,
    val status: String? = null
)