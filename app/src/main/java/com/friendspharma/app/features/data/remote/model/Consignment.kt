package com.friendspharma.app.features.data.remote.model

data class Consignment(
    val cod_amount: Int,
    val consignment_id: Int,
    val created_at: String,
    val invoice: String,
    val note: String,
    val recipient_address: String,
    val recipient_name: String,
    val recipient_phone: String,
    val status: String,
    val tracking_code: String,
    val updated_at: String
)