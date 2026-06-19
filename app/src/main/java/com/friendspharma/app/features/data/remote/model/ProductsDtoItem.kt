package com.friendspharma.app.features.data.remote.model

data class ProductsDtoItem(
    val PID_COMPANY: Int? = null,
    val PID_CATEGORY: Int? = null,
    val BOX_MRP_PRICE: Double? = null,
    val BOX_OFFER_VALUE: Double? = null,
    val BOX_SALES_PER: Double? = null,
    val BOX_SALES_PRICE: Double? = null,
    val BOX_SIZE_TITLE: String? = null,
    val CATEGORY_NAME: String? = null,
    val COMPANY_NAME: String? = null,
    val IMAGE_URL: String? = null,
    val LEAF_MRP_PRICE: Double? = null,
    val LEAF_OFFER_VALUE: Double? = null,
    val LEAF_SALES_PER: Double? = null,
    val LEAF_SALES_PRICE: Double? = null,
    val NO_OF_PCS: Int? = null,
    val PID_PRODUCT: Int? = null,
    val PRODUCT_NAME: String? = null,
    val SKU: String? = null,
    val STOCK_QTY_BOX: Double? = null,
    val STOCK_QTY_LEAF: Double? = null,
    val UNIT_NAME: String? = null,
    val SALES_TYPE: String? = null,
    val STRIP_QTY: Int? = null
)