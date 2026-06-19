package com.friendspharma.app.features.presentation.cart

import com.friendspharma.app.features.data.remote.model.AddressDto
import com.friendspharma.app.features.data.remote.model.AddressDtoItem
import com.friendspharma.app.features.data.remote.model.CartInfoDto

data class CartState(
    val isLoading: Boolean = true,
    val cartInfoDto: CartInfoDto = CartInfoDto(),
    val totalQuantity: Int = 0,
    val totalPrice: Double = 0.0,
    val addresses: AddressDto = AddressDto(),
    val selectedAddress: AddressDtoItem = AddressDtoItem(),
    val showAddressDialog: Boolean = false,
    val addressLoading: Boolean = false,
    val addToCartLoading: String = "",
    val deliveryCharge: Int = 0,
    val deliveryTime: String = "",
    val address: String = "",
    val post: String = "",
    val district: String = ""
)
