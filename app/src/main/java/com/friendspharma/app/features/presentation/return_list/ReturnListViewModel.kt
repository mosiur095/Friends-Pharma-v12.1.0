package com.friendspharma.app.features.presentation.return_list

import android.os.Build

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.model.ReturnList
import com.friendspharma.app.features.data.remote.model.ReturnListData
import com.friendspharma.app.features.data.remote.model.ReturnListDto
import com.friendspharma.app.features.data.remote.model.ReturnListDtoData
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.GetReturnListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class ReturnListViewModel @Inject constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val getReturnListUseCase: GetReturnListUseCase
) :
    ViewModel() {

    private val _state = MutableStateFlow(ReturnListState())
    val state: StateFlow<ReturnListState> = _state.asStateFlow()

    init {
        getOrders()
    }

    private fun getOrders() {
        getReturnListUseCase.invoke(sharedPreferenceHelper.getUser().USER_ID.toString())
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        val returnList = mutableListOf<ReturnList>()

                        var data = result.data?.data?.toMutableList()

                        while (data?.isNotEmpty() == true) {
                            val item = data[0]
                            val itemList = data.filter { item.INVOICE_NO == it.INVOICE_NO }.map {
                                ReturnListData(
                                    imageUrl = it.IMAGE_URL,
                                    productName = it.PRODUCT_NAME,
                                    quantity = it.QUANTITY,
                                    totalPrice = it.TOTAL_PRICE
                                )
                            }
                            returnList.add(
                                ReturnList(
                                    invoice = item.INVOICE_NO,
                                    customerId = item.PID_CUSTOMER,
                                    customerName = item.CUSTOMER_NAME,
                                    mobile = item.MOBILE_NO,
                                    transectionNumber = item.PID_TRAN_MST,
                                    status = item.STATUS,
                                    data = itemList
                                )
                            )
                            data = data.filter { it.INVOICE_NO != item.INVOICE_NO }.toMutableList()
                        }

                        _state.update {
                            it.copy(
                                isLoading = false,
                                returnList = returnList
                            )
                        }
                    }

                    is Async.Error -> {}
                    is Async.Loading -> {}
                }
            }.launchIn(viewModelScope)
    }

    fun detailsDialog(details: ReturnList) {
        _state.update { it.copy(currentItem = details) }
    }


}