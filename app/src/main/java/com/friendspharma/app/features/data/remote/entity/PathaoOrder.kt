package com.friendspharma.app.features.data.remote.entity

data class PathaoOrder(
    val store_id: String,
    val merchant_order_id: String = "",
    val recipient_name: String,
    val recipient_phone: String,
    val recipient_address: String,
    val recipient_city: String,
    val recipient_zone: String,
    val recipient_area: String = "",
    val delivery_type: Int,
    val item_type: Int,
    val special_instruction: String = "",
    val item_quantity: String,
    val item_weight: String,
    val amount_to_collect: String,
    val item_description: String = ""
)
