package com.friendspharma.app.features.presentation.stead_fast_courier

import android.content.Context
import android.os.Build
import android.widget.Toast

import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.ScreenArgs
import com.friendspharma.app.features.data.remote.entity.SteadFastOrder
import com.friendspharma.app.features.data.remote.model.OrderDetailsDtoItem
import com.friendspharma.app.features.domain.use_case.GetUserUseCase
import com.friendspharma.app.features.domain.use_case.SteadFastCreateOrderUseCase
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class SteadFastCourierViewModel @Inject constructor(
    private val steadFastCreateCourierUseCase: SteadFastCreateOrderUseCase,
    private val getUserUseCase: GetUserUseCase,
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    private val _state = MutableStateFlow(CourierState())
    val state: StateFlow<CourierState> = _state.asStateFlow()

    private val orderData: String = checkNotNull(savedStateHandle[ScreenArgs.DATA])
    private val order: OrderDetailsDtoItem =
        Gson().fromJson(orderData, OrderDetailsDtoItem::class.java)

    init {
        _state.update { it.copy(order = order) }
        getUser(order.CUSTOMER_ID)
    }

    private fun getUser(customerId: Int?) {
        getUserUseCase.invoke(customerId.toString()).onEach { result ->
            when (result) {
                is Async.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                    if (!result.data?.data.isNullOrEmpty()) {
                        _state.update {
                            it.copy(
                                recipientName = result.data?.data?.get(0)?.USER_NAME ?: "",
                                recipientPhone = result.data?.data?.get(0)?.MOBILE_NO ?: "",
                                recipientAddress = result.data?.data?.get(0)?.ADDRESS ?: ""
                            )
                        }
                    }

                }

                is Async.Error -> {}
                is Async.Loading -> {}
            }

        }.launchIn(viewModelScope)
    }


    fun recipientNameChanged(name: String) {
        _state.update {
            it.copy(
                recipientName = name
            )
        }
    }

    fun recipientPhoneChanged(phone: String) {
        _state.update {
            it.copy(
                recipientPhone = phone,
            )
        }
    }

    fun recipientAddressChanged(address: String) {
        _state.update {
            it.copy(
                recipientAddress = address,
            )
        }

    }

    fun amountToCollectChanged(string: String) {
        _state.update {
            it.copy(
                codAmount = string,
            )
        }

    }

    fun itemDescriptionChanged(string: String) {
        _state.update {
            it.copy(note = string)
        }

    }

    fun submit(
        recipientNameFocusRequester: FocusRequester,
        recipientPhoneFocusRequester: FocusRequester,
        recipientAddressFocusRequester: FocusRequester,
        amountToCollectFocusRequester: FocusRequester,
        context: Context
    ) {
        _state.update { it.copy(isValidate = true) }

        if (state.value.recipientName.isEmpty()) {
            recipientNameFocusRequester.requestFocus()
        } else if (state.value.recipientPhone.isEmpty()) {
            recipientPhoneFocusRequester.requestFocus()
        } else if (state.value.recipientAddress.isEmpty()) {
            recipientAddressFocusRequester.requestFocus()
        } else if (state.value.codAmount.toDouble() < 0) {
            amountToCollectFocusRequester.requestFocus()
        } else {
            steadFastCreateCourierUseCase.invoke(
                body = SteadFastOrder(
                    invoice = state.value.order.ORDER_NO ?: "",
                    recipient_name = state.value.recipientName,
                    recipient_phone = state.value.recipientPhone,
                    recipient_address = state.value.recipientAddress,
                    cod_amount = state.value.codAmount.toDouble(),
                    note = state.value.note
                )
            ).onEach { result ->
                when (result) {
                    is Async.Success -> {
                        _state.update {
                            it.copy(isLoading = false)
                        }
                        Toast.makeText(context, result.message ?: "Success", Toast.LENGTH_SHORT)
                            .show()
                    }

                    is Async.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                            )
                        }
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }

                    is Async.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }.launchIn(viewModelScope)

        }
    }
}