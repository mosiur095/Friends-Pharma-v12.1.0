package com.friendspharma.app.features.data.repository

import com.friendspharma.app.core.di.ApplicationScope
import com.friendspharma.app.core.di.DefaultDispatcher
import com.friendspharma.app.features.data.local.dao.ProductDao
import com.friendspharma.app.features.data.local.dao.toExternal
import com.friendspharma.app.features.data.local.dao.toLocal
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem
import com.friendspharma.app.features.domain.repository.ProductRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductRepoImpl @Inject constructor(
    private val productDao: ProductDao,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val scope: CoroutineScope
) : ProductRepo {
    override suspend fun getProducts(): List<ProductsDtoItem> {
        return withContext(dispatcher) {
            productDao.getAll().toExternal()
        }
    }

    override suspend fun getProductsByCompany(id : Int): List<ProductsDtoItem> {
        return  withContext(dispatcher){
            productDao.getProductsByCompany(id)
        }
    }

    override suspend fun getProduct(id: String): ProductsDtoItem? {
        return withContext(dispatcher) {
            productDao.getById(id)?.toExternal()
        }
    }

    override suspend fun createProduct(product: ProductsDtoItem): Int? {
        withContext(dispatcher) {
            productDao.upsert(product.toLocal())
        }
        return product.PID_PRODUCT
    }

    override suspend fun createProducts(products: List<ProductsDtoItem>) {
        withContext(dispatcher) {
            productDao.upsertAll(products.toLocal())
        }
    }

    override suspend fun deleteProduct(id: String) {
        withContext(dispatcher) {
            productDao.deleteById(id)
        }
    }

    override suspend fun deleteAllProducts() {
        withContext(dispatcher) {
            productDao.deleteAll()
        }
    }

    override suspend fun getProductsByCategory(id: Int): List<ProductsDtoItem> {
        return withContext(dispatcher) {
            productDao.getProductsByCategory(id)
        }
    }
}