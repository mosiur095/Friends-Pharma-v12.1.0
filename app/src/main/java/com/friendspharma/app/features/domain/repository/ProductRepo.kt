package com.friendspharma.app.features.domain.repository

import com.friendspharma.app.features.data.remote.model.ProductsDtoItem

interface ProductRepo {

    suspend fun getProducts(): List<ProductsDtoItem>

    suspend fun getProductsByCompany(id: Int): List<ProductsDtoItem>

    suspend fun getProduct(id: String): ProductsDtoItem?

    suspend fun createProduct(product: ProductsDtoItem): Int?

    suspend fun createProducts(products: List<ProductsDtoItem>)

    suspend fun deleteProduct(id: String)

    suspend fun deleteAllProducts()

    suspend fun getProductsByCategory(id: Int) : List<ProductsDtoItem>
}