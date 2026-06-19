package com.friendspharma.app.features.data.remote.model

data class CartInfoDto(
    val `data`: List<CartInfoDtoItem>? = null,
    val message: String? = null
)