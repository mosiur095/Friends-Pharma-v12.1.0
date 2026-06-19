package com.friendspharma.app.features.data.local.dao

import com.friendspharma.app.features.data.local.entities.LocalCompanyItem
import com.friendspharma.app.features.data.remote.model.AllCompanyDtoItem

fun AllCompanyDtoItem.toLocal() = LocalCompanyItem(
    PID_COMPANY = PID_COMPANY ?: -1,
    ADDRESS =  ADDRESS,
    COMPANY_NAME = COMPANY_NAME,
    IMAGE_BANNER_URL = IMAGE_BANNER_URL,
    IMAGE_LOGO_URL = IMAGE_LOGO_URL
)

fun LocalCompanyItem.toExternal() = AllCompanyDtoItem(
    PID_COMPANY = PID_COMPANY,
    ADDRESS =  ADDRESS,
    COMPANY_NAME = COMPANY_NAME,
    IMAGE_BANNER_URL = IMAGE_BANNER_URL,
    IMAGE_LOGO_URL = IMAGE_LOGO_URL
)

@JvmName("localToExternal")
fun List<LocalCompanyItem>.toExternal() = map(LocalCompanyItem::toExternal)

@JvmName("externalToLocal")
fun List<AllCompanyDtoItem>.toLocal() = map(AllCompanyDtoItem::toLocal)