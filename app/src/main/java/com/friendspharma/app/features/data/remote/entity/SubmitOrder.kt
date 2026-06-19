package com.friendspharma.app.features.data.remote.entity

data class SubmitOrder(
    val mobile_no: String,
    val pid_tran_mst: String,
    val address: String,
    val delivery_charge: String
)