package com.friendspharma.app.features.data.remote.model

data class CartInfoDtoItem(
    val PID_PRODUCT: Int? = null,
    val PID_TRAN_DTL: Int? = null,
    val PRODUCT: String? = null,
    val SALES_UNIT: String? = null,
    val UNIT_NAME: String? = null,
    val QUANTITY: Number? = null,
    val SALES_PRICE: Double? = null,
    val TOTAL_PRICE: Double? = null,
    val IMAGE_URL: String? = null,
    val PID_TRAN_MST: String? = null,
    val OFFER_VALUE: Double? = null,
    val SALES_PER: Double? = null,
    val MRP_PRICE: Double? = null
)