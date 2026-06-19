package com.friendspharma.app.features.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.friendspharma.app.features.data.local.dao.CartDao
import com.friendspharma.app.features.data.local.dao.CategoryDao
import com.friendspharma.app.features.data.local.dao.CompanyDao
import com.friendspharma.app.features.data.local.dao.NotificationDao
import com.friendspharma.app.features.data.local.dao.ProductDao
import com.friendspharma.app.features.data.local.dao.RetailProductDao
import com.friendspharma.app.features.data.local.dao.SpecialProductDao
import com.friendspharma.app.features.data.local.entities.LocalCartItem
import com.friendspharma.app.features.data.local.entities.LocalCategoryItem
import com.friendspharma.app.features.data.local.entities.LocalCompanyItem
import com.friendspharma.app.features.data.local.entities.LocalNotificationItem
import com.friendspharma.app.features.data.local.entities.LocalProductItem
import com.friendspharma.app.features.data.local.entities.LocalRetailProduct
import com.friendspharma.app.features.data.local.entities.LocalSpecialProduct

@Database(
    entities = [
        LocalProductItem::class,
        LocalCartItem::class,
        LocalCategoryItem::class,
        LocalCompanyItem::class,
        LocalRetailProduct::class,
        LocalSpecialProduct::class,
        LocalNotificationItem::class,
    ],
    version = 5,
    exportSchema = false
)
abstract class PharmaDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun categoryDao(): CategoryDao
    abstract fun companyDao(): CompanyDao
    abstract fun retailProductDao(): RetailProductDao
    abstract fun specialProductDao(): SpecialProductDao
    abstract fun notificationDao(): NotificationDao
}