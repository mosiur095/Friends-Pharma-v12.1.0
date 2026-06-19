package com.friendspharma.app.features.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class LocalCategoryItem(
    val CATEGORY_NAME: String? = null,
    val IMAGE_URL: String? = null,
    @PrimaryKey val PID_CATEGORY: Int
)
