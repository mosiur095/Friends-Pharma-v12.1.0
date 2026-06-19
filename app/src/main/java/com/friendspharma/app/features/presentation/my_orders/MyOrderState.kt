package com.friendspharma.app.features.presentation.my_orders

import com.friendspharma.app.features.data.remote.model.OrdersDto

data class MyOrderState(
    val isLoading: Boolean = true,
    val orders: OrdersDto = OrdersDto()
)
