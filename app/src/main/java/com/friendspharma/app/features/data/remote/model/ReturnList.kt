package com.friendspharma.app.features.data.remote.model

data class ReturnList(
    val invoice: String? = null,
    val customerId: Int? = null,
    val customerName: String? = null,
    val mobile: String? = null,
    val transectionNumber: Int? = null,
    val status: String? = null,
    val data: List<ReturnListData>? = null
)

data class ReturnListData(
    val imageUrl: String? = null,
    val productName: String? = null,
    val quantity: Int? = null,
    val totalPrice: Double? = null
)
