package com.friendspharma.app.features.presentation.return_cart

import android.content.Context
import android.os.Build
import android.widget.Toast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.entity.ReturnCartRemove
import com.friendspharma.app.features.data.remote.entity.SubmitReturn
import com.friendspharma.app.features.data.remote.model.ReturnCartInfoDto
import com.friendspharma.app.features.data.remote.model.ReturnCartInfoDtoData
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.ReturnCartInfoUseCase
import com.friendspharma.app.features.domain.use_case.ReturnCartRemoveUseCase
import com.friendspharma.app.features.domain.use_case.SubmitReturnUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class ReturnCartViewModel @Inject constructor(
    private val returnCartInfoUseCase: ReturnCartInfoUseCase,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val submitReturnUseCase: SubmitReturnUseCase,
    private val returnCartRemoveUseCase: ReturnCartRemoveUseCase
) :
    ViewModel() {

    private val _state = MutableStateFlow(ReturnCartState())
    val state: StateFlow<ReturnCartState> = _state.asStateFlow()

    init {
        getCartInfo()
    }

    private fun getCartInfo() {
        returnCartInfoUseCase.invoke(sharedPreferenceHelper.getUser().MOBILE_NO ?: "")
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        var totalQuantity = 0
                        var totalPrice = 0.0
                        for (item in result.data?.data ?: emptyList()) {
                            totalQuantity += item.QUANTITY ?: 0
                            totalPrice += item.TOTAL_PRICE ?: 0.0
                        }
                        _state.update {
                            it.copy(
                                isLoading = false,
                                cartInfoDto = result.data ?: ReturnCartInfoDto(),
                                totalQuantity = totalQuantity,
                                totalPrice = totalPrice,
                                addToCartLoading = ""
                            )
                        }
                    }

                    is Async.Error -> {
                        _state.update {
                            it.copy(addToCartLoading = "")
                        }
                    }
                    is Async.Loading -> {}
                }
            }.launchIn(viewModelScope)
    }

    fun submitReturn(context: Context, pop: () -> Unit) {
        submitReturnUseCase.invoke(SubmitReturn(state.value.cartInfoDto.data?.get(0)?.PID_TRAN_MST.toString()))
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        _state.update {
                            it.copy(isLoading = false)
                        }
                        Toast.makeText(
                            context,
                            result.data?.message ?: "Return Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        pop()
                    }

                    is Async.Error -> {
                        _state.update {
                            it.copy(isLoading = false)
                        }
                    }

                    is Async.Loading -> {
                        _state.update {
                            it.copy(isLoading = true)
                        }
                    }
                }
            }.launchIn(viewModelScope)

    }

    fun returnCartRemoveUseCase(item: ReturnCartInfoDtoData) {
        returnCartRemoveUseCase.invoke(
            ReturnCartRemove(
                pPID_TRAN_DTL = item.PID_TRAN_DTL.toString(),
                pid_product = item.PID_PRODUCT.toString()
            )
        )
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        getCartInfo()
                    }

                    is Async.Error -> {
                        _state.update {
                            it.copy(addToCartLoading = "")
                        }
                    }

                    is Async.Loading -> {
                        _state.update {
                            it.copy(addToCartLoading = item.PID_PRODUCT.toString())
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

}
