package com.friendspharma.app.features.presentation.search

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.MainActivity
import com.friendspharma.app.MainActivity.Companion.isRestrict
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.core.util.Utils
import com.friendspharma.app.features.data.remote.entity.ProductAdd
import com.friendspharma.app.features.data.remote.entity.ProductRemove
import com.friendspharma.app.features.data.remote.model.CartInfoDto
import com.friendspharma.app.features.data.remote.model.CartInfoDtoItem
import com.friendspharma.app.features.data.remote.model.ProductsDto
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem
import com.friendspharma.app.features.domain.services.LocalConstant
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.AddToCartRestrictUseCase
import com.friendspharma.app.features.domain.use_case.GetAllCategoryUseCase
import com.friendspharma.app.features.domain.use_case.GetAllCompanyUseCase
import com.friendspharma.app.features.domain.use_case.GetCartInfoUseCase
import com.friendspharma.app.features.domain.use_case.GetProductsUseCase
import com.friendspharma.app.features.domain.use_case.ProductAddUseCase
import com.friendspharma.app.features.domain.use_case.ProductRemoveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCartInfoUseCase: GetCartInfoUseCase,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val productAddUseCase: ProductAddUseCase,
    private val productRemoveUseCase: ProductRemoveUseCase,
    private val getAllCompanyUseCase: GetAllCompanyUseCase,
    private val getAllCategoryUseCase: GetAllCategoryUseCase,
    private val getRestrictUseCase: AddToCartRestrictUseCase,
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    // ✅ FIX: dedicated search job — cancels previous debounce on each keystroke
    private var searchJob: Job? = null

    fun init() {
        getProducts()
        if (MainActivity.isLoggedIn.value) {
            getCartInfo()
            checkRestriction()
        }
    }

    private fun checkRestriction() {
        isRestrict.intValue = sharedPreferenceHelper.getIntData(LocalConstant.isRestrict)
        getRestrictUseCase.invoke(sharedPreferenceHelper.getUser().MOBILE_NO ?: "")
            .onEach { result ->
                when (result) {
                    is Async.Success<*> -> {
                        isRestrict.intValue = result.data?.isexists ?: -1
                        sharedPreferenceHelper.saveIntData(LocalConstant.isRestrict, isRestrict.intValue)
                    }
                    is Async.Error<*>   -> {}
                    is Async.Loading<*> -> {}
                }
            }.launchIn(viewModelScope)
    }

    fun checkBoxOrLeaf() {
        val userType   = MainActivity.userType.value
        val isLoggedIn = MainActivity.isLoggedIn.value
        // guest / type 1 → leaf, type 2/3 → box
        val isBox = isLoggedIn && (userType == "2" || userType == "3")
        _state.update { it.copy(isBox = isBox) }
    }

    fun getProducts() {
        getProductsUseCase.invoke(isForceRefresh = false).onEach { result ->
            when (result) {
                is Async.Success -> {
                    _state.update {
                        it.copy(
                            products       = result.data ?: ProductsDto(),
                            isLoading      = false,
                            refreshLoading = false
                        )
                    }
                }
                is Async.Error   -> { _state.update { it.copy(refreshLoading = false) } }
                is Async.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun updateCurrentItem(item: ProductsDtoItem) {
        _state.update { it.copy(currentItem = item) }
    }

    // ✅ FIX: search text updates SYNCHRONOUSLY outside the coroutine.
    // Only the filtering work runs inside the debounced coroutine.
    fun searchChanged(text: String) {
        _state.update { it.copy(search = text) }

        searchJob?.cancel()

        if (text.isNotEmpty()) {
            searchJob = viewModelScope.launch {
                delay(150)
                if (state.value.search == text) {
                    val items = state.value.products.data?.filter {
                        it.PRODUCT_NAME?.lowercase()?.contains(text.lowercase()) == true ||
                                it.COMPANY_NAME?.lowercase()?.contains(text.lowercase()) == true ||
                                it.CATEGORY_NAME?.lowercase()?.contains(text.lowercase()) == true
                    } ?: emptyList()
                    _state.update { it.copy(searchedProduct = items) }
                }
            }
        } else {
            _state.update { it.copy(searchedProduct = emptyList()) }
        }
    }

    fun addToCart(product: ProductsDtoItem, quantity: Int, salesUnit: String, context: Context) {
        productAddUseCase.invoke(
            ProductAdd(
                mobile_no   = sharedPreferenceHelper.getUser().MOBILE_NO ?: "",
                pid_product = product.PID_PRODUCT.toString(),
                pqty        = quantity.toString(),
                salesunit   = salesUnit
            )
        ).onEach { result ->
            when (result) {
                is Async.Success -> {
                    getCartInfo()
                    Toast.makeText(context, result.data?.message ?: "", Toast.LENGTH_SHORT).show()
                }
                is Async.Error   -> {
                    _state.update { it.copy(addToCartLoading = "") }
                    Toast.makeText(context, result.data?.message ?: "An unknown error occurred", Toast.LENGTH_SHORT).show()
                }
                is Async.Loading -> { _state.update { it.copy(addToCartLoading = product.PID_PRODUCT.toString()) } }
            }
        }.launchIn(viewModelScope)
    }

    fun removeFromCart(item: ProductsDtoItem, salesUnit: String, context: Context) {
        val product = getProductFromCart(item.PID_PRODUCT)
        productRemoveUseCase.invoke(
            ProductRemove(
                sharedPreferenceHelper.getUser().MOBILE_NO ?: "",
                pid_product  = product.PID_PRODUCT.toString(),
                pid_tran_dtl = product.PID_TRAN_DTL.toString(),
                salesunit    = salesUnit
            )
        ).onEach { result ->
            when (result) {
                is Async.Success -> {
                    getCartInfo()
                    Toast.makeText(context, result.data?.message ?: "", Toast.LENGTH_SHORT).show()
                }
                is Async.Error   -> {
                    _state.update { it.copy(addToCartLoading = "") }
                    Toast.makeText(context, result.data?.message ?: "An unknown error occurred", Toast.LENGTH_SHORT).show()
                }
                is Async.Loading -> { _state.update { it.copy(addToCartLoading = item.PID_PRODUCT.toString()) } }
            }
        }.launchIn(viewModelScope)
    }

    private fun getProductFromCart(pidProduct: Int?): CartInfoDtoItem {
        for (item in state.value.cartInfo.data ?: emptyList()) {
            if (pidProduct == item.PID_PRODUCT) return item
        }
        return CartInfoDtoItem()
    }

    private fun getCartInfo() {
        if (MainActivity.isLoggedIn.value) {
            getCartInfoUseCase.invoke(sharedPreferenceHelper.getUser().MOBILE_NO ?: "")
                .onEach { result ->
                    when (result) {
                        is Async.Success -> {
                            var quantity = 0
                            val set      = HashSet<Int>()
                            for (item in result.data?.data ?: emptyList()) {
                                quantity += item.QUANTITY?.toInt() ?: 0
                                set.add(item.PID_PRODUCT ?: -1)
                            }
                            _state.update {
                                it.copy(
                                    cartInfo         = result.data ?: CartInfoDto(),
                                    cartItemQuantity = quantity,
                                    cartIds          = set,
                                    addToCartLoading = ""
                                )
                            }
                            MainActivity.cartQuantity.intValue = quantity
                        }
                        is Async.Error   -> {}
                        is Async.Loading -> {}
                    }
                }.launchIn(viewModelScope)
        } else {
            _state.update {
                it.copy(cartInfo = CartInfoDto(), cartItemQuantity = 0, cartIds = HashSet())
            }
        }
    }

    fun switch() {
        val userType   = MainActivity.userType.value
        val isLoggedIn = MainActivity.isLoggedIn.value
        if (!isLoggedIn || userType == "1") return
        _state.update { it.copy(isBox = !state.value.isBox) }
    }

    fun navToWhatsApp(context: Context) {
        Utils.openWhatsAppChat(context, "8801826034230")
    }
}