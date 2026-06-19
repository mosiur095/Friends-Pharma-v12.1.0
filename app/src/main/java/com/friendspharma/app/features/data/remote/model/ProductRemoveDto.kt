package com.friendspharma.app.features.data.remote.model

data class ProductRemoveDto(
    val message: String? = null,
    val pCARTQTY: Int? = null,
    val pINVOICE_DIS_AMT: Int? = null,
    val pPID_TRAN_MST: Int? = null,
    val pTOTAL_AMOUNT: Double? = null,
    val status: Int? = null
)