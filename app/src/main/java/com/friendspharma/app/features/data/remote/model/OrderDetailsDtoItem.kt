package com.friendspharma.app.features.data.remote.model

data class OrderDetailsDtoItem(
    val IMAGE_URL: String? = null,
    val ORDER_NO: String? = null,
    val PID_PRODUCT: Int? = null,
    val PID_TRAN_DTL: Int? = null,
    val PID_TRAN_MST: Int? = null,
    val PRODUCT: String? = null,
    val QUANTITY: Double? = null,
    val SALES_PRICE: Double? = null,
    val SALES_UNIT: String? = null,
    val TOTAL_PRICE: Double? = null,
    val UNIT_NAME: String? = null,
    val DELIVERY_ADDRESS: String? = null,
    val OFFER_VALUE: Double? = null,
    val SALES_PER: Double? = null,
    val MRP_PRICE: Double? = null,
    val ORDER_DATE: String? = null,
    val CUSTOMER_ID: Int? = null,
    val USER_NAME: String? = null,
    val DELIVERY_CHARGE: Double? = null
)