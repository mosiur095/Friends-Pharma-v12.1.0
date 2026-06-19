package com.friendspharma.app.features.presentation.stead_fast_courier

import com.friendspharma.app.features.data.remote.model.OrderDetailsDtoItem

data class CourierState(
    val isLoading: Boolean = false,
    val recipientName: String = "",
    val recipientPhone: String = "",
    val recipientAddress: String = "",
    val codAmount: String = "",
    val note: String = "",
    val isValidate: Boolean = false,
    val order: OrderDetailsDtoItem = OrderDetailsDtoItem(),
)