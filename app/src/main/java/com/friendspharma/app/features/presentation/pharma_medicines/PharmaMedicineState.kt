package com.friendspharma.app.features.presentation.pharma_medicines

import com.friendspharma.app.features.data.remote.model.AllCompanyDto
import com.friendspharma.app.features.data.remote.model.AllCompanyDtoItem
import com.friendspharma.app.features.data.remote.model.CartInfoDto
import com.friendspharma.app.features.data.remote.model.CategoryProducts
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem


data class PharmaMedicineState(
    val isLoading: Boolean = true,
    val allProduct: ArrayList<CategoryProducts> = arrayListOf(),
    val allSearchedProduct: ArrayList<CategoryProducts> = arrayListOf(),
    val search: String = "",
    val cartInfo: CartInfoDto = CartInfoDto(),
    val cartItemQuantity: Int = 0,
    val cartIds: Set<Int> = HashSet(),
    val currentItem: ProductsDtoItem = ProductsDtoItem(),
    val isBox: Boolean = true,
    val addToCartLoading: String = "",
    val company: AllCompanyDtoItem = AllCompanyDtoItem(),
    val currentCategoryIndex: Int = -1,
    val companies: AllCompanyDto = AllCompanyDto()
)
