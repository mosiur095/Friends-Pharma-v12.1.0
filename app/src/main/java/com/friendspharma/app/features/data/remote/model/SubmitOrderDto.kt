package com.friendspharma.app.features.data.remote.model

data class SubmitOrderDto(
    val message: String? = null,
    val pAMOUNT_TOBE_PAID: Double? = null,
    val pINVOICE_NO: String? = null,
    val pPID_TRAN_MST: Int? = null,
    val status: Int? = null,
    val pADDRESS: String? = null
)