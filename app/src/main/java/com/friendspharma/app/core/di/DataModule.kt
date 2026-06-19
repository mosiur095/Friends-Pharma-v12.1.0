package com.friendspharma.app.core.di

import android.content.Context
import androidx.room.Room
import com.friendspharma.app.features.data.local.PharmaDatabase
import com.friendspharma.app.features.data.local.dao.CartDao
import com.friendspharma.app.features.data.local.dao.CategoryDao
import com.friendspharma.app.features.data.local.dao.CompanyDao
import com.friendspharma.app.features.data.local.dao.NotificationDao
import com.friendspharma.app.features.data.local.dao.ProductDao
import com.friendspharma.app.features.data.local.dao.RetailProductDao
import com.friendspharma.app.features.data.local.dao.SpecialProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): PharmaDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            PharmaDatabase::class.java,
            "FriendPharma.db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    fun provideProductsDao(database: PharmaDatabase): ProductDao = database.productDao()

    @Provides
    fun provideCartDao(database: PharmaDatabase): CartDao = database.cartDao()

    @Provides
    fun provideCategoryDao(database: PharmaDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideCompanyDao(database: PharmaDatabase): CompanyDao = database.companyDao()

    @Provides
    fun provideRetailProductDao(database: PharmaDatabase): RetailProductDao = database.retailProductDao()

    @Provides
    fun provideSpecialProductDao(database: PharmaDatabase): SpecialProductDao = database.specialProductDao()

    @Provides
    fun provideNotificationDao(database: PharmaDatabase): NotificationDao = database.notificationDao()

}