package com.friendspharma.app.features.presentation.return_products

import android.content.Context
import android.os.Build
import android.widget.Toast

import androidx.compose.ui.focus.FocusManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.ScreenArgs
import com.friendspharma.app.features.data.remote.entity.AddReturn
import com.friendspharma.app.features.data.remote.model.ReturnCartInfoDto
import com.friendspharma.app.features.data.remote.model.ReturnProductDto
import com.friendspharma.app.features.data.remote.model.ReturnProductDtoItem
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.AddToReturnUseCase
import com.friendspharma.app.features.domain.use_case.ProductReturnUseCase
import com.friendspharma.app.features.domain.use_case.ReturnCartInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ReturnMedicineViewModel @Inject constructor(
    private val productReturnUseCase: ProductReturnUseCase,
    private val addToReturnUseCase: AddToReturnUseCase,
    private val preferenceHelper: SharedPreferenceHelper,
    private val returnCartInfoUseCase: ReturnCartInfoUseCase,
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    private val _state = MutableStateFlow(ReturnMedicineState())
    val state: StateFlow<ReturnMedicineState> = _state.asStateFlow()

    private val id: String = checkNotNull(savedStateHandle[ScreenArgs.DATA])

    fun init() {
        returnOrder(id)
        getReturnCartInfo()
    }

    private fun getReturnCartInfo() {
        returnCartInfoUseCase.invoke(preferenceHelper.getUser().MOBILE_NO ?: "").onEach { result ->
            when (result) {
                is Async.Success -> {
                    var quantity = 0
                    val set = HashSet<Int>()
                    for (item in result.data?.data ?: emptyList()) {
                        quantity += item.QUANTITY ?: 0
                        set.add(item.PID_PRODUCT ?: -1)
                    }
                    _state.update {
                        it.copy(
                            cartInfo = result.data ?: ReturnCartInfoDto(),
                            cartItemQuantity = quantity,
                            cartIds = set,
                            addToCartLoading = ""
                        )
                    }
                }

                is Async.Error<*> -> {
                    _state.update { it.copy(addToCartLoading = "") }
                }
                is Async.Loading<*> -> {}
            }
        }.launchIn(viewModelScope)
    }


    private fun returnOrder(id: String) {
        productReturnUseCase.invoke(id)
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                allProduct = result.data ?: ReturnProductDto(),
                                allSearchedProduct = result.data ?: ReturnProductDto()
                            )
                        }
                    }

                    is Async.Error -> {
                        _state.update { it.copy(isLoading = false) }
                    }

                    is Async.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }.launchIn(viewModelScope)
    }

    fun searchChanged(text: String, focusManager: FocusManager) {
        viewModelScope.launch {
            _state.update { it.copy(search = text) }
            if (text.isNotEmpty()) {
                val products = state.value.allProduct.data?.filter {
                    it.PRODUCT_NAME?.lowercase()?.contains(text.lowercase()) == true
                }
                _state.update {
                    it.copy(
                        allSearchedProduct = state.value.allSearchedProduct.copy(
                            data = products
                        )
                    )
                }
            } else {
                focusManager.clearFocus()
                _state.update { it.copy(allSearchedProduct = state.value.allProduct) }
            }
        }
    }

    fun updateCurrentItem(item: ReturnProductDtoItem) {
        _state.update { it.copy(currentItem = item) }
    }

    fun addToCart(
        product: ReturnProductDtoItem,
        quantity: Int,
        context: Context
    ) {
        addToReturnUseCase.invoke(
            AddReturn(
                mobile_no = preferenceHelper.getUser().MOBILE_NO ?: "",
                pReturnqty = quantity.toString(),
                pexpiry_date = "",
                pid_product = product.PID_PRODUCT ?: 0,
                pid_tran_mst = id.toInt(),
                pmrp_price = product.MRP_PRICE.toString(),
                ppid_tran_dtl = product.PID_TRAN_DTL ?: 0,
                psales_price = product.SALES_PRICE.toString(),
                psales_type = product.SALES_TYPE ?: "",
            )
        )
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        if (result.data?.status == 200) {
                            getReturnCartInfo()
                        }else{
                            _state.update { it.copy(addToCartLoading = "") }
                        }
                        Toast.makeText(context, result.data?.message, Toast.LENGTH_SHORT).show()
                    }

                    is Async.Error -> {
                        _state.update { it.copy(addToCartLoading = "") }
                    }

                    is Async.Loading -> {
                        _state.update { it.copy(addToCartLoading = product.PID_PRODUCT.toString()) }
                    }
                }
            }.launchIn(viewModelScope)
    }

}