package com.friendspharma.app.features.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company")
data class LocalCompanyItem(
    val ADDRESS: String? = null,
    val COMPANY_NAME: String? = null,
    val IMAGE_BANNER_URL: String? = null,
    val IMAGE_LOGO_URL: String? = null,
    @PrimaryKey val PID_COMPANY: Int
)
