package com.friendspharma.app.features.data.remote.model

/**
 * Response for return/updateInvoice.
 * status == 200 => applied. Extra fields let us refresh the invoice
 * card without a second call (confirm exact names with Tohid bhai).
 */
data class UpdateInvoiceDto(
    val status: Int? = null,
    val message: String? = null,
    val pPID_TRAN_MST: Int? = null,
    val pTOTAL_AMOUNT: Double? = null,
    val pCARTQTY: Int? = null
)