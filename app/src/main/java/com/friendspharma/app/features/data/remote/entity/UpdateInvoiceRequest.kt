package com.friendspharma.app.features.data.remote.entity

/**
 * Batch payload for updating an invoice before delivery.
 * One POST carries every reduced/removed line; the backend applies
 * them in a single transaction (all-or-nothing).
 *
 * Product-level: the API stores each product as several fractional rows,
 * so we identify a line by pid_product and let the backend re-allocate the
 * return across its own rows. Field names are snake_case to match the backend.
 */
data class UpdateInvoiceRequest(
    val pid_tran_mst: Int,
    val mobile_no: String,
    val items: List<UpdateInvoiceItem>
)

data class UpdateInvoiceItem(
    val pid_product: Int,
    val psales_type: String,
    val pmrp_price: String,
    val psales_price: String,
    val pexpiry_date: String,
    val pReturnqty: String
)