package com.friendspharma.app.features.presentation.pathao_courier

import android.content.Context
import android.os.Build
import android.widget.Toast

import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.ScreenArgs
import com.friendspharma.app.features.data.remote.entity.PathaoOrder
import com.friendspharma.app.features.data.remote.entity.PathaoToken
import com.friendspharma.app.features.data.remote.model.AreaDto
import com.friendspharma.app.features.data.remote.model.AreaDtoItem
import com.friendspharma.app.features.data.remote.model.CityDto
import com.friendspharma.app.features.data.remote.model.CityDtoItem
import com.friendspharma.app.features.data.remote.model.OrderDetailsDtoItem
import com.friendspharma.app.features.data.remote.model.PathaoOrderErrorsDtoErrors
import com.friendspharma.app.features.data.remote.model.PathaoTokenDto
import com.friendspharma.app.features.data.remote.model.ZoneDto
import com.friendspharma.app.features.data.remote.model.ZoneDtoItem
import com.friendspharma.app.features.domain.use_case.GetAreasByZoneUseCase
import com.friendspharma.app.features.domain.use_case.GetCitiesUseCase
import com.friendspharma.app.features.domain.use_case.GetPathaoTokenUseCase
import com.friendspharma.app.features.domain.use_case.GetUserUseCase
import com.friendspharma.app.features.domain.use_case.GetZonesByCityUseCase
import com.friendspharma.app.features.domain.use_case.PathaoOrderUseCase
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
class PathaoCourierViewModel @Inject constructor(
    private val getPathaoTokenUseCase: GetPathaoTokenUseCase,
    private val getCitiesUseCase: GetCitiesUseCase,
    private val getZonesByCityUseCase: GetZonesByCityUseCase,
    private val getAreasByZoneUseCase: GetAreasByZoneUseCase,
    private val pathaoOrderUseCase: PathaoOrderUseCase,
    private val getUserUseCase: GetUserUseCase,
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    private val orderData: String = checkNotNull(savedStateHandle[ScreenArgs.DATA])
    private val order: OrderDetailsDtoItem =
        Gson().fromJson(orderData, OrderDetailsDtoItem::class.java)

    private val _state = MutableStateFlow(CourierState())
    val state: StateFlow<CourierState> = _state.asStateFlow()

    init {
        getPathaoToken()
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

    private fun getPathaoToken() {
        getPathaoTokenUseCase.invoke(PathaoToken()).onEach { result ->
            when (result) {
                is Async.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            token = result.data ?: PathaoTokenDto()
                        )
                    }
                    getCities(result.data?.token_type + " " + result.data?.access_token)
                }

                is Async.Error -> {}
                is Async.Loading -> {}
            }
        }.launchIn(viewModelScope)

    }

    private fun getCities(token: String) {
        getCitiesUseCase.invoke(token).onEach { result ->
            when (result) {
                is Async.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            cities = result.data ?: CityDto()
                        )
                    }
                }

                is Async.Error -> {}
                is Async.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    private fun getZonesByCity(token: String, city: String) {
        getZonesByCityUseCase.invoke(token, city).onEach { result ->
            when (result) {
                is Async.Success -> {
                    _state.update {
                        it.copy(zones = result.data ?: ZoneDto())
                    }
                }

                is Async.Error -> {}
                is Async.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    private fun getAreasByZone(token: String, zone: String) {
        getAreasByZoneUseCase.invoke(token, zone).onEach { result ->
            when (result) {
                is Async.Success -> {
                    _state.update {
                        it.copy(areas = result.data ?: AreaDto())
                    }
                }

                is Async.Error -> {}
                is Async.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun merchantOrderIdChanged(id: String) {
        _state.update {
            it.copy(merchantOrderId = id)
        }
    }

    fun recipientNameChanged(name: String) {
        _state.update {
            it.copy(
                recipientName = name,
                pathaoOrderError = state.value.pathaoOrderError.copy(recipient_name = null)
            )
        }
    }

    fun recipientPhoneChanged(phone: String) {
        _state.update {
            it.copy(
                recipientPhone = phone,
                pathaoOrderError = state.value.pathaoOrderError.copy(recipient_phone = null)
            )
        }
    }

    fun recipientAddressChanged(address: String) {
        _state.update {
            it.copy(
                recipientAddress = address,
                pathaoOrderError = state.value.pathaoOrderError.copy(recipient_address = null)
            )
        }

    }

    fun cityChanged(city: CityDtoItem) {
        _state.update {
            it.copy(
                recipientCity = city,
                recipientZone = ZoneDtoItem(),
                recipientArea = AreaDtoItem(),
                pathaoOrderError = state.value.pathaoOrderError.copy(recipient_city = null)
            )
        }
        getZonesByCity(
            state.value.token.token_type + " " + state.value.token.access_token,
            city.city_id.toString()
        )
    }

    fun zoneChanged(item: ZoneDtoItem) {
        _state.update {
            it.copy(
                recipientZone = item, recipientArea = AreaDtoItem(),
                pathaoOrderError = state.value.pathaoOrderError.copy(recipient_zone = null)
            )
        }
        getAreasByZone(
            state.value.token.token_type + " " + state.value.token.access_token,
            item.zone_id.toString()
        )
    }

    fun areaChanged(item: AreaDtoItem) {
        _state.update {
            it.copy(recipientArea = item)
        }
    }

    fun deliveryTypeChanged(type: DeliveryType) {
        _state.update {
            it.copy(
                deliveryType = type,
                pathaoOrderError = state.value.pathaoOrderError.copy(delivery_type = null)
            )
        }
    }

    fun itemTypeChanged(type: ItemType) {
        _state.update {
            it.copy(
                itemType = type,
                pathaoOrderError = state.value.pathaoOrderError.copy(item_type = null)
            )
        }

    }

    fun specialInstructionChanged(string: String) {
        _state.update {
            it.copy(specialInstruction = string)
        }

    }

    fun itemQuantityChanged(string: String) {
        _state.update {
            it.copy(
                itemQuantity = string,
                pathaoOrderError = state.value.pathaoOrderError.copy(item_quantity = null)
            )
        }
    }

    fun itemWeightChanged(string: String) {
        _state.update {
            it.copy(
                itemWeight = string,
                pathaoOrderError = state.value.pathaoOrderError.copy(item_weight = null)
            )
        }
    }

    fun amountToCollectChanged(string: String) {
        _state.update {
            it.copy(
                amountToCollect = string,
                pathaoOrderError = state.value.pathaoOrderError.copy(amount_to_collect = null)
            )
        }

    }

    fun itemDescriptionChanged(string: String) {
        _state.update {
            it.copy(itemDescription = string)
        }

    }

    fun submit(
        recipientNameFocusRequester: FocusRequester,
        recipientPhoneFocusRequester: FocusRequester,
        recipientAddressFocusRequester: FocusRequester,
        recipientCityFocusRequester: FocusRequester,
        recipientZoneFocusRequester: FocusRequester,
        deliveryTypeFocusRequester: FocusRequester,
        itemTypeFocusRequester: FocusRequester,
        itemQuantityFocusRequester: FocusRequester,
        itemWeightFocusRequester: FocusRequester,
        amountToCollectFocusRequester: FocusRequester,
        context: Context
    ) {
        _state.update { it.copy(isValidate = true) }

        if (state.value.recipientName.isEmpty() || !state.value.pathaoOrderError.recipient_name.isNullOrEmpty()) {
            recipientNameFocusRequester.requestFocus()
        } else if (state.value.recipientPhone.isEmpty() || !state.value.pathaoOrderError.recipient_phone.isNullOrEmpty()) {
            recipientPhoneFocusRequester.requestFocus()
        } else if (state.value.recipientAddress.isEmpty() || !state.value.pathaoOrderError.recipient_address.isNullOrEmpty()) {
            recipientAddressFocusRequester.requestFocus()
        } else if (state.value.recipientCity.city_name.isNullOrEmpty() || !state.value.pathaoOrderError.recipient_city.isNullOrEmpty()) {
            recipientCityFocusRequester.requestFocus()
        } else if (state.value.recipientZone.zone_name.isNullOrEmpty() || !state.value.pathaoOrderError.recipient_zone.isNullOrEmpty()) {
            recipientZoneFocusRequester.requestFocus()
        } else if (state.value.deliveryType.name.isNullOrEmpty() || !state.value.pathaoOrderError.delivery_type.isNullOrEmpty()) {
            deliveryTypeFocusRequester.requestFocus()
        } else if (state.value.itemType.name.isNullOrEmpty() || !state.value.pathaoOrderError.item_type.isNullOrEmpty()) {
            itemTypeFocusRequester.requestFocus()
        } else if (state.value.itemQuantity.isEmpty() || !state.value.pathaoOrderError.item_quantity.isNullOrEmpty()) {
            itemQuantityFocusRequester.requestFocus()
        } else if (state.value.itemWeight.isEmpty() || !state.value.pathaoOrderError.item_weight.isNullOrEmpty()) {
            itemWeightFocusRequester.requestFocus()
        } else if (state.value.amountToCollect.isEmpty() || !state.value.pathaoOrderError.amount_to_collect.isNullOrEmpty()) {
            amountToCollectFocusRequester.requestFocus()
        } else {
            pathaoOrderUseCase.invoke(
                body = PathaoOrder(
                    store_id = state.value.storeId,
                    merchant_order_id = state.value.merchantOrderId,
                    recipient_name = state.value.recipientName,
                    recipient_phone = state.value.recipientPhone,
                    recipient_address = state.value.recipientAddress,
                    recipient_city = state.value.recipientCity.city_id.toString(),
                    recipient_zone = state.value.recipientZone.zone_id.toString(),
                    recipient_area = state.value.recipientArea.area_id.toString(),
                    delivery_type = state.value.deliveryType.id ?: -1,
                    item_type = state.value.itemType.id ?: -1,
                    special_instruction = state.value.specialInstruction,
                    item_quantity = state.value.itemQuantity,
                    item_weight = state.value.itemWeight,
                    amount_to_collect = state.value.amountToCollect,
                    item_description = state.value.itemDescription
                ),
                token = state.value.token.token_type + " " + state.value.token.access_token
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
                                pathaoOrderError = Gson().fromJson(
                                    result.message,
                                    PathaoOrderErrorsDtoErrors::class.java
                                )
                            )
                        }
                        //Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }

                    is Async.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }.launchIn(viewModelScope)

        }
    }
}