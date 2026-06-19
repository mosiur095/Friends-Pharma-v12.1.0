package com.friendspharma.app.features.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class LocalCartItem(
    @PrimaryKey val PID_PRODUCT: Int,
    val PID_TRAN_DTL: Int? = null,
    val PRODUCT: String? = null,
    val QUANTITY: Int? = null,
    val SALES_PRICE: Double? = null,
    val TOTAL_PRICE: Double? = null
)
