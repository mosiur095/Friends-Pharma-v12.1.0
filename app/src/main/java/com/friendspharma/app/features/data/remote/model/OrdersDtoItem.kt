package com.friendspharma.app.features.data.remote.model

data class OrdersDtoItem(
    val ORDER_DATE: String? = null,
    val ORDER_NO: String? = null,
    val PID_TRAN_MST: Int? = null,
    val TOTAL_AMOUNT: Double? = null,
    val ORDER_STATUS: String? = null
)