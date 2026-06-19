package com.friendspharma.app.features.presentation.my_orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.model.OrdersDto
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.GetOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// ✅ Removed @RequiresExtension from @HiltViewModel class — causes kapt processor warnings
@HiltViewModel
class MyOrderViewModel @Inject constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val getOrdersUseCase: GetOrdersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyOrderState())
    val state: StateFlow<MyOrderState> = _state.asStateFlow()

    init {
        getOrders()
    }

    private fun getOrders() {
        getOrdersUseCase.invoke(
            sharedPreferenceHelper.getUser().MOBILE_NO ?: ""
        ).onEach { result ->
            when (result) {
                is Async.Success -> {
                    _state.update {
                        it.copy(isLoading = false, orders = result.data ?: OrdersDto())
                    }
                }
                is Async.Error   -> {}
                is Async.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }
}