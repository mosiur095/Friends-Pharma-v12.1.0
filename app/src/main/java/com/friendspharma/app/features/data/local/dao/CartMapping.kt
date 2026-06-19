package com.friendspharma.app.features.data.local.dao

import com.friendspharma.app.features.data.local.entities.LocalCartItem
import com.friendspharma.app.features.data.remote.model.CartInfoDtoItem

fun CartInfoDtoItem.toLocal() = LocalCartItem(
    PID_PRODUCT = PID_PRODUCT ?: -1,
    PID_TRAN_DTL = PID_TRAN_DTL,
    PRODUCT = PRODUCT,
    QUANTITY = QUANTITY?.toInt(),
    SALES_PRICE = SALES_PRICE,
    TOTAL_PRICE = TOTAL_PRICE
)

fun LocalCartItem.toExternal() = CartInfoDtoItem(
    PID_PRODUCT = PID_PRODUCT,
    PID_TRAN_DTL = PID_TRAN_DTL,
    PRODUCT = PRODUCT,
    QUANTITY = QUANTITY,
    SALES_PRICE = SALES_PRICE,
    TOTAL_PRICE = TOTAL_PRICE
)

@JvmName("localToExternal")
fun List<LocalCartItem>.toExternal() = map(LocalCartItem::toExternal)

@JvmName("externalToLocal")
fun List<CartInfoDtoItem>.toLocal() = map(CartInfoDtoItem::toLocal)