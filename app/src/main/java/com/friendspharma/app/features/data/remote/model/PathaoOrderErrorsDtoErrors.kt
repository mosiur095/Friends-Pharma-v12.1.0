package com.friendspharma.app.features.data.remote.model

data class PathaoOrderErrorsDtoErrors(
    val amount_to_collect: List<String>? = null,
    val delivery_type: List<String>? = null,
    val item_quantity: List<String>? = null,
    val item_type: List<String>? = null,
    val item_weight: List<String>? = null,
    val recipient_address: List<String>? = null,
    val recipient_city: List<String>? = null,
    val recipient_name: List<String>? = null,
    val recipient_phone: List<String>? = null,
    val recipient_zone: List<String>? = null,
    val store_id: List<String>? = null
)