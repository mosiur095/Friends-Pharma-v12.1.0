package com.friendspharma.app.features.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Upsert
import com.friendspharma.app.features.data.local.entities.LocalProductItem
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem

@RewriteQueriesToDropUnusedColumns
@Dao
interface ProductDao {

    @Query("SELECT * FROM product")
    suspend fun getAll(): List<LocalProductItem>

    @Query("SELECT * FROM product WHERE PID_PRODUCT = :id")
    suspend fun getById(id: String): LocalProductItem?

    @Upsert
    suspend fun upsert(track: LocalProductItem)

    @Upsert
    suspend fun upsertAll(tracks: List<LocalProductItem>)

    @Query("DELETE FROM product WHERE PID_PRODUCT = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM product")
    suspend fun deleteAll()

    @Query("SELECT * FROM product WHERE PID_COMPANY = :id")
    suspend fun getProductsByCompany(id: Int): List<ProductsDtoItem>

    @Query("SELECT * FROM product WHERE PID_CATEGORY = :id")
    suspend fun getProductsByCategory(id: Int): List<ProductsDtoItem>

    @Query("SELECT * FROM product WHERE userType = :userType ORDER BY COMPANY_NAME, PRODUCT_NAME")
    suspend fun getAllByUserType(userType: String): List<LocalProductItem>

    @Query("SELECT * FROM product WHERE userType = :userType ORDER BY COMPANY_NAME, PRODUCT_NAME LIMIT :limit")
    suspend fun getFirstNByUserType(userType: String, limit: Int): List<LocalProductItem>

    @Query("""
        SELECT CASE 
            WHEN COUNT(*) = 0 THEN 1
            WHEN MIN(cachedAt) < :expiryTime THEN 1
            ELSE 0
        END
        FROM product WHERE userType = :userType
    """)
    suspend fun isCacheExpired(userType: String, expiryTime: Long): Boolean

    @Query("DELETE FROM product WHERE userType = :userType")
    suspend fun deleteByUserType(userType: String)
}