package com.friendspharma.app.features.data.local.dao

import com.friendspharma.app.features.data.local.entities.LocalRetailProduct
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem

fun ProductsDtoItem.toRetailLocal() = LocalRetailProduct(
    PID_COMPANY = PID_COMPANY,
    PID_CATEGORY = PID_CATEGORY,
    BOX_MRP_PRICE = BOX_MRP_PRICE,
    BOX_OFFER_VALUE = BOX_OFFER_VALUE,
    BOX_SALES_PER = BOX_SALES_PER,
    BOX_SALES_PRICE = BOX_SALES_PRICE,
    BOX_SIZE_TITLE = BOX_SIZE_TITLE,
    CATEGORY_NAME = CATEGORY_NAME,
    COMPANY_NAME = COMPANY_NAME,
    IMAGE_URL = IMAGE_URL,
    LEAF_MRP_PRICE = LEAF_MRP_PRICE,
    LEAF_OFFER_VALUE = LEAF_OFFER_VALUE,
    LEAF_SALES_PER = LEAF_SALES_PER,
    LEAF_SALES_PRICE = LEAF_SALES_PRICE,
    NO_OF_PCS = NO_OF_PCS,
    PID_PRODUCT = PID_PRODUCT ?: -1,
    PRODUCT_NAME = PRODUCT_NAME,
    SKU = SKU,
    STOCK_QTY_BOX = STOCK_QTY_BOX,
    STOCK_QTY_LEAF = STOCK_QTY_LEAF,
    UNIT_NAME = UNIT_NAME,
    SALES_TYPE = SALES_TYPE,
    STRIP_QTY =STRIP_QTY
)

fun LocalRetailProduct.toRetailExternal() = ProductsDtoItem(
    PID_COMPANY = PID_COMPANY,
    PID_CATEGORY = PID_CATEGORY,
    BOX_MRP_PRICE = BOX_MRP_PRICE,
    BOX_OFFER_VALUE = BOX_OFFER_VALUE,
    BOX_SALES_PER = BOX_SALES_PER,
    BOX_SALES_PRICE = BOX_SALES_PRICE,
    BOX_SIZE_TITLE = BOX_SIZE_TITLE,
    CATEGORY_NAME = CATEGORY_NAME,
    COMPANY_NAME = COMPANY_NAME,
    IMAGE_URL = IMAGE_URL,
    LEAF_MRP_PRICE = LEAF_MRP_PRICE,
    LEAF_OFFER_VALUE = LEAF_OFFER_VALUE,
    LEAF_SALES_PER = LEAF_SALES_PER,
    LEAF_SALES_PRICE = LEAF_SALES_PRICE,
    NO_OF_PCS = NO_OF_PCS,
    PID_PRODUCT = PID_PRODUCT,
    PRODUCT_NAME = PRODUCT_NAME,
    SKU = SKU,
    STOCK_QTY_BOX = STOCK_QTY_BOX,
    STOCK_QTY_LEAF = STOCK_QTY_LEAF,
    UNIT_NAME = UNIT_NAME,
    SALES_TYPE = SALES_TYPE,
    STRIP_QTY =STRIP_QTY
)

@JvmName("localToExternal")
fun List<LocalRetailProduct>.toRetailExternal() = map(LocalRetailProduct::toRetailExternal)

@JvmName("externalToLocal")
fun List<ProductsDtoItem>.toRetailLocal() = map(ProductsDtoItem::toRetailLocal)