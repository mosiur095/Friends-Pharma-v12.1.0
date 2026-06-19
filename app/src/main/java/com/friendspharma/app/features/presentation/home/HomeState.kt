package com.friendspharma.app.features.presentation.home

import com.friendspharma.app.features.data.remote.model.AllCompanyDto
import com.friendspharma.app.features.data.remote.model.CartInfoDto
import com.friendspharma.app.features.data.remote.model.CategoryProducts
import com.friendspharma.app.features.data.remote.model.ProductsDto
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem

data class HomeState(
    val isLoading: Boolean = true,
    val currentItem: ProductsDtoItem = ProductsDtoItem(),
    val products: ProductsDto = ProductsDto(),
    val allProduct: ArrayList<CategoryProducts> = arrayListOf(),
    val searchedProduct: List<ProductsDtoItem> = emptyList(),
    val search: String = "",
    val cartInfo: CartInfoDto = CartInfoDto(),
    val cartItemQuantity: Int = 0,
    val cartIds: Set<Int> = HashSet(),
    val currentCategoryIndex: Int = -1,
    val isBox: Boolean = false,          // default = Leaf (guest / no login)
    val addToCartLoading: String = "",
    val isSortedeByCategory: Boolean = true,
    val companies: AllCompanyDto = AllCompanyDto(),
    val refreshLoading: Boolean = false
)