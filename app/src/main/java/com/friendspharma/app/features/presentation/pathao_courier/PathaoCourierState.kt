package com.friendspharma.app.features.presentation.pathao_courier

import com.friendspharma.app.features.data.remote.model.AreaDto
import com.friendspharma.app.features.data.remote.model.AreaDtoItem
import com.friendspharma.app.features.data.remote.model.CityDto
import com.friendspharma.app.features.data.remote.model.CityDtoItem
import com.friendspharma.app.features.data.remote.model.PathaoOrderErrorsDtoErrors
import com.friendspharma.app.features.data.remote.model.PathaoTokenDto
import com.friendspharma.app.features.data.remote.model.ZoneDto
import com.friendspharma.app.features.data.remote.model.ZoneDtoItem

data class CourierState(
    val isLoading: Boolean = false,
    val token: PathaoTokenDto = PathaoTokenDto(),
    val cities: CityDto = CityDto(),
    val zones: ZoneDto = ZoneDto(),
    val areas: AreaDto = AreaDto(),
    val storeId: String = "147905",
    val merchantOrderId: String = "",
    val recipientName: String = "",
    val recipientPhone: String = "",
    val recipientAddress: String = "",
    val recipientCity: CityDtoItem = CityDtoItem(),
    val recipientZone: ZoneDtoItem = ZoneDtoItem(),
    val recipientArea: AreaDtoItem = AreaDtoItem(),
    val deliveryType: DeliveryType = DeliveryType(),
    val itemType: ItemType = ItemType(),
    val specialInstruction: String = "",
    val itemQuantity: String = "1",
    val itemWeight: String = "",
    val amountToCollect: String = "0",
    val itemDescription: String = "",
    val isValidate: Boolean = false,
    val pathaoOrderError: PathaoOrderErrorsDtoErrors = PathaoOrderErrorsDtoErrors()
)

data class DeliveryType(
    val name: String? = null,
    val id: Int? = null
)

data class ItemType(
    val name: String? = null,
    val id: Int? = null
)
