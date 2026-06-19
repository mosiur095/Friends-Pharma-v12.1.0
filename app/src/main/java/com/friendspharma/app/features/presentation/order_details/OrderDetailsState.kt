package com.friendspharma.app.features.presentation.order_details

import com.friendspharma.app.features.data.remote.model.OrderDetailsDto
import com.friendspharma.app.features.data.remote.model.OrderDetailsDtoItem
import com.friendspharma.app.features.data.remote.model.TrackOrderDto

data class OrderDetailsState(
    val isLoading: Boolean = true,
    val orders: OrderDetailsDto = OrderDetailsDto(),
    // Merged list: same PID_PRODUCT + same SALES_UNIT → combined into one row
    val mergedItems: List<OrderDetailsDtoItem> = emptyList(),
    val totalQuantity: Double = 0.0,
    val totalPrice: Double = 0.0,
    val totalAmount: Double = 0.0,
    val discount: Double = 0.0,
    val fileName: String = "",
    val track: TrackOrderDto = TrackOrderDto(),
    val hasError: Boolean = false
)