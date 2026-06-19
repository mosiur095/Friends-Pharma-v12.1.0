package com.friendspharma.app.features.presentation.delivery_man

import com.friendspharma.app.features.data.remote.model.OrderDetailsDtoItem
import com.friendspharma.app.features.data.remote.model.PendignDeliveryDtoItem
import com.friendspharma.app.features.data.remote.model.PendingDeliveryDto

data class DeliveryManState(
    val isLoading: Boolean = true,
    val deliveries: PendingDeliveryDto = PendingDeliveryDto(),
    val deliveriesDone: PendingDeliveryDto = PendingDeliveryDto(),
    val deliveriesPaid: PendingDeliveryDto = PendingDeliveryDto(),
    val deliveriesCollected: PendingDeliveryDto = PendingDeliveryDto(),
    val currentDeliveryItem: PendignDeliveryDtoItem = PendignDeliveryDtoItem(),
    val currentCollectionItem: PendignDeliveryDtoItem = PendignDeliveryDtoItem(),
    val currentPaid: PendignDeliveryDtoItem = PendignDeliveryDtoItem(),
    val orderProducts: List<OrderDetailsDtoItem> = emptyList(),
    val isProductsLoading: Boolean = false,
    val editedQuantities: Map<Int, Double> = emptyMap(),
    val showProductDialog: Boolean = false,
)