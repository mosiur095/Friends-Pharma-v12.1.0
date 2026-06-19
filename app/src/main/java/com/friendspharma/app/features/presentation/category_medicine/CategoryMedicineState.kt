package com.friendspharma.app.features.presentation.category_medicine

import com.friendspharma.app.features.data.remote.model.AllCategoryDto
import com.friendspharma.app.features.data.remote.model.AllCategoryDtoItem
import com.friendspharma.app.features.data.remote.model.CartInfoDto
import com.friendspharma.app.features.data.remote.model.CategoryProducts
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem


data class CategoryMedicineState(
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
    val category: AllCategoryDtoItem = AllCategoryDtoItem(),
    val categories: AllCategoryDto = AllCategoryDto(),
    val currentCategoryIndex: Int = -1
)
