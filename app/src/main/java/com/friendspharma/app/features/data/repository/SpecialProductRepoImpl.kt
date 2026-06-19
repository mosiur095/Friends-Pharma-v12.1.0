package com.friendspharma.app.features.data.repository

import com.friendspharma.app.core.di.ApplicationScope
import com.friendspharma.app.core.di.DefaultDispatcher
import com.friendspharma.app.features.data.local.dao.SpecialProductDao
import com.friendspharma.app.features.data.local.dao.toSpecialExternal
import com.friendspharma.app.features.data.local.dao.toSpecialLocal
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem
import com.friendspharma.app.features.domain.repository.SpecialProductRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SpecialProductRepoImpl @Inject constructor(
    private val specialProductDao: SpecialProductDao,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val scope: CoroutineScope
) : SpecialProductRepo {
    override suspend fun getProducts(): List<ProductsDtoItem> {
        return withContext(dispatcher) {
            specialProductDao.getAll().toSpecialExternal()
        }
    }

    override suspend fun getProductsByCompany(id : Int): List<ProductsDtoItem> {
        return  withContext(dispatcher){
            specialProductDao.getProductsByCompany(id).toSpecialExternal()
        }
    }

    override suspend fun getProduct(id: String): ProductsDtoItem? {
        return withContext(dispatcher) {
            specialProductDao.getById(id)?.toSpecialExternal()
        }
    }

    override suspend fun createProduct(product: ProductsDtoItem): Int? {
        withContext(dispatcher) {
            specialProductDao.upsert(product.toSpecialLocal())
        }
        return product.PID_PRODUCT
    }

    override suspend fun createProducts(products: List<ProductsDtoItem>) {
        withContext(dispatcher) {
            specialProductDao.upsertAll(products.toSpecialLocal())
        }
    }

    override suspend fun deleteProduct(id: String) {
        withContext(dispatcher) {
            specialProductDao.deleteById(id)
        }
    }

    override suspend fun deleteAllProducts() {
        withContext(dispatcher) {
            specialProductDao.deleteAll()
        }
    }

    override suspend fun getProductsByCategory(id: Int): List<ProductsDtoItem> {
        return withContext(dispatcher) {
            specialProductDao.getProductsByCategory(id).toSpecialExternal()
        }
    }
}