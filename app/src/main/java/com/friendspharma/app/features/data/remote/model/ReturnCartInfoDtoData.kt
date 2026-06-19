package com.friendspharma.app.features.data.remote.model

data class ReturnCartInfoDtoData(
    val IMAGE_URL: String? = null,
    val MRP_PRICE: Double? = null,
    val PID_PRODUCT: Int? = null,
    val PID_TRAN_DTL: Int? = null,
    val PID_TRAN_MST: Int? = null,
    val PRODUCT: String? = null,
    val QUANTITY: Int? = null,
    val SALES_PRICE: Double? = null,
    val SALES_UNIT: String? = null,
    val TOTAL_PRICE: Double? = null,
    val UNIT_NAME: String? = null
)