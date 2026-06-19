package com.friendspharma.app.features.data.repository

import com.friendspharma.app.core.di.ApplicationScope
import com.friendspharma.app.core.di.DefaultDispatcher
import com.friendspharma.app.features.data.local.dao.RetailProductDao
import com.friendspharma.app.features.data.local.dao.toRetailExternal
import com.friendspharma.app.features.data.local.dao.toRetailLocal
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem
import com.friendspharma.app.features.domain.repository.RetailProductRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RetailProductRepoImpl @Inject constructor(
    private val retailProductDao: RetailProductDao,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val scope: CoroutineScope
) : RetailProductRepo {
    override suspend fun getProducts(): List<ProductsDtoItem> {
        return withContext(dispatcher) {
            retailProductDao.getAll().toRetailExternal()
        }
    }

    override suspend fun getProductsByCompany(id : Int): List<ProductsDtoItem> {
        return  withContext(dispatcher){
            retailProductDao.getProductsByCompany(id).toRetailExternal()
        }
    }

    override suspend fun getProduct(id: String): ProductsDtoItem? {
        return withContext(dispatcher) {
            retailProductDao.getById(id)?.toRetailExternal()
        }
    }

    override suspend fun createProduct(product: ProductsDtoItem): Int? {
        withContext(dispatcher) {
            retailProductDao.upsert(product.toRetailLocal())
        }
        return product.PID_PRODUCT
    }

    override suspend fun createProducts(products: List<ProductsDtoItem>) {
        withContext(dispatcher) {
            retailProductDao.upsertAll(products.toRetailLocal())
        }
    }

    override suspend fun deleteProduct(id: String) {
        withContext(dispatcher) {
            retailProductDao.deleteById(id)
        }
    }

    override suspend fun deleteAllProducts() {
        withContext(dispatcher) {
            retailProductDao.deleteAll()
        }
    }

    override suspend fun getProductsByCategory(id: Int): List<ProductsDtoItem> {
        return withContext(dispatcher) {
            retailProductDao.getProductsByCategory(id).toRetailExternal()
        }
    }
}