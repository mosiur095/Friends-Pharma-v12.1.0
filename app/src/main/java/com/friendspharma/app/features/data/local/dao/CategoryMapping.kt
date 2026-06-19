package com.friendspharma.app.features.data.local.dao

import com.friendspharma.app.features.data.local.entities.LocalCategoryItem
import com.friendspharma.app.features.data.remote.model.AllCategoryDtoItem

fun AllCategoryDtoItem.toLocal() = LocalCategoryItem(
    PID_CATEGORY = PID_CATEGORY ?: -1,
    CATEGORY_NAME =  CATEGORY_NAME,
    IMAGE_URL = IMAGE_URL
)

fun LocalCategoryItem.toExternal() = AllCategoryDtoItem(
    PID_CATEGORY = PID_CATEGORY,
    CATEGORY_NAME =  CATEGORY_NAME,
    IMAGE_URL = IMAGE_URL
)

@JvmName("localToExternal")
fun List<LocalCategoryItem>.toExternal() = map(LocalCategoryItem::toExternal)

@JvmName("externalToLocal")
fun List<AllCategoryDtoItem>.toLocal() = map(AllCategoryDtoItem::toLocal)