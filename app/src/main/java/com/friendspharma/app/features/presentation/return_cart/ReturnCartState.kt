package com.friendspharma.app.features.presentation.return_cart

import com.friendspharma.app.features.data.remote.model.ReturnCartInfoDto

data class ReturnCartState(
    val isLoading: Boolean = true,
    val cartInfoDto: ReturnCartInfoDto = ReturnCartInfoDto(),
    val totalQuantity: Int = 0,
    val totalPrice: Double = 0.0,
    val addToCartLoading: String = ""
)
