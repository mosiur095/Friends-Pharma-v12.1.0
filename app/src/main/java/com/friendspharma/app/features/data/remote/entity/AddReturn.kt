package com.friendspharma.app.features.data.remote.entity

data class AddReturn(
    val mobile_no: String,
    val pReturnqty: String,
    val pexpiry_date: String,
    val pid_product: Int,
    val pid_tran_mst: Int,
    val pmrp_price: String,
    val ppid_tran_dtl: Int,
    val psales_price: String,
    val psales_type: String
)