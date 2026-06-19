package com.friendspharma.app.features.presentation.cart

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest

import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.MainActivity
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.data.remote.entity.ChangeAddress
import com.friendspharma.app.features.data.remote.entity.DeleteAddress
import com.friendspharma.app.features.data.remote.entity.InsertAddress
import com.friendspharma.app.features.data.remote.entity.ProductAdd
import com.friendspharma.app.features.data.remote.entity.ProductRemove
import com.friendspharma.app.features.data.remote.entity.SubmitOrder
import com.friendspharma.app.features.data.remote.model.AddressDto
import com.friendspharma.app.features.data.remote.model.AddressDtoItem
import com.friendspharma.app.features.data.remote.model.CartInfoDto
import com.friendspharma.app.features.data.remote.model.CartInfoDtoItem
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.ChangeAddressUseCase
import com.friendspharma.app.features.domain.use_case.DeleteAddressUseCase
import com.friendspharma.app.features.domain.use_case.GetAddressUseCase
import com.friendspharma.app.features.domain.use_case.GetCartInfoUseCase
import com.friendspharma.app.features.domain.use_case.GetUserUseCase
import com.friendspharma.app.features.domain.use_case.InsertAddressUseCase
import com.friendspharma.app.features.domain.use_case.ProductAddUseCase
import com.friendspharma.app.features.domain.use_case.ProductRemoveUseCase
import com.friendspharma.app.features.domain.use_case.SubmitOrderUseCase
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

// ✅ Removed @RequiresExtension from @HiltViewModel class
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartInfoUseCase: GetCartInfoUseCase,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val productAddUseCase: ProductAddUseCase,
    private val productRemoveUseCase: ProductRemoveUseCase,
    private val submitOrderUseCase: SubmitOrderUseCase,
    private val getAddressUseCase: GetAddressUseCase,
    private val preferenceHelper: SharedPreferenceHelper,
    private val insertAddressUseCase: InsertAddressUseCase,
    private val changeAddressUseCase: ChangeAddressUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase,
    private val getUserUseCase: GetUserUseCase,
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    private var fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)
    private var geocoder: Geocoder = Geocoder(application)
    private var addresses: List<Address>? = listOf()

    init {
        getCartInfo()
        getAddress()
    }

    fun checkLocationSetting(
        context: Context,
        activity: Activity,
        onDisabled: (IntentSenderRequest) -> Unit,
        onEnabled: () -> Unit
    ) {
        // ✅ LocationRequest.create() deprecated → use LocationRequest.Builder()
        val locationRequest = LocationRequest.Builder(1000)
            .setMinUpdateIntervalMillis(1000)
            .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest
            .Builder()
            .addLocationRequest(locationRequest)

        val gpsSettingTask: Task<LocationSettingsResponse> =
            client.checkLocationSettings(builder.build())

        gpsSettingTask.addOnSuccessListener { getLocation(context, activity) }
        gpsSettingTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest
                        .Builder(exception.resolution)
                        .build()
                    onDisabled(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // ignore here
                }
            }
        }
    }

    fun getLocation(context: Context, activity: Activity) {
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }

        val task = fusedLocationProviderClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        )
        task.addOnSuccessListener {
            if (it != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(it.latitude, it.longitude, 1) { addrs ->
                            addresses = addrs
                            var address = ""
                            var district = ""
                            var post = ""
                            if (addrs.isNotEmpty()) {
                                address  = addrs[0].getAddressLine(0) ?: ""
                                district = addrs[0].subAdminArea ?: ""
                                post     = addrs[0].subLocality ?: ""
                            }
                            if (address.isNotEmpty()) {
                                _state.update { it.copy(address = address, district = district, post = post) }
                            }
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        var address = ""
                        var district = ""
                        var post = ""
                        if (!addresses.isNullOrEmpty()) {
                            address  = addresses?.get(0)?.getAddressLine(0) ?: ""
                            district = addresses?.get(0)?.subAdminArea ?: ""
                            post     = addresses?.get(0)?.subLocality ?: ""
                        }
                        if (address.isNotEmpty()) {
                            _state.update { it.copy(address = address, district = district, post = post) }
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun getAddress() {
        getAddressUseCase.invoke(preferenceHelper.getUser().USER_ID.toString())
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        result.data?.data?.forEach { address ->
                            if (address.ADDR_TYPE == "Home") {
                                _state.update { it.copy(selectedAddress = address) }
                            }
                        }
                        _state.update {
                            it.copy(
                                isLoading = false,
                                addresses = result.data ?: AddressDto(),
                                addressLoading = false
                            )
                        }
                        calculateDeliveryCharge()
                    }
                    is Async.Error -> _state.update { it.copy(isLoading = false) }
                    is Async.Loading -> _state.update { it.copy(isLoading = true) }
                }
            }.launchIn(viewModelScope)
    }

    // ✅ FIXED getCartInfo
    // Merges duplicates + corrects price calculation
    private fun getCartInfo() {
        cartInfoUseCase.invoke(sharedPreferenceHelper.getUser().MOBILE_NO ?: "")
            .onEach { result ->
                when (result) {
                    is Async.Success -> {

                        // ✅ Merge by PID + SALES_UNIT
                        val mergedItems = result.data?.data
                            ?.groupBy { Pair(it.PID_PRODUCT, it.SALES_UNIT) }
                            ?.map { (_, items) ->
                                val firstItem = items.first()

                                // ✅ Sum quantities
                                val totalQuantity = items.sumOf {
                                    it.QUANTITY?.toDouble() ?: 0.0
                                }
                                val roundedQty = Math.round(totalQuantity)

                                // ✅ Recalculate price correctly
                                // price = unit_price * rounded_quantity
                                val unitPrice = firstItem.SALES_PRICE ?: 0.0
                                val correctTotalPrice = BigDecimal(unitPrice * roundedQty)
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .toDouble()

                                // ✅ Save = (MRP − sales price) per unit × quantity.
                                // The cart API's OFFER_VALUE is unreliable (it can exceed the
                                // price and not match SALES_PER — e.g. 632.5 on a 456.5 box at
                                // 17%), so derive the saving from coherent values instead.
                                // Falls back to deriving from the discount % (off MRP) when
                                // MRP isn't present.
                                val unitMrp = firstItem.MRP_PRICE ?: 0.0
                                val pct = firstItem.SALES_PER ?: 0.0
                                val perUnitSave = when {
                                    unitMrp > unitPrice      -> unitMrp - unitPrice
                                    pct > 0.0 && pct < 100.0 -> unitPrice * pct / (100 - pct)
                                    else                     -> 0.0
                                }
                                val correctSaveTotal = BigDecimal(perUnitSave * roundedQty)
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .toDouble()

                                firstItem.copy(
                                    QUANTITY = roundedQty,
                                    TOTAL_PRICE = correctTotalPrice,
                                    OFFER_VALUE = correctSaveTotal
                                )
                            } ?: emptyList()

                        // ✅ Calculate totals from merged
                        var totalQuantity = 0
                        var totalPrice = 0.0
                        for (item in mergedItems) {
                            totalQuantity += item.QUANTITY?.toInt() ?: 0
                            totalPrice += item.TOTAL_PRICE ?: 0.0
                        }

                        val cleanCartInfo = CartInfoDto(
                            data = mergedItems,
                            message = result.data?.message
                        )

                        _state.update {
                            it.copy(
                                isLoading = false,
                                cartInfoDto = cleanCartInfo,
                                totalQuantity = totalQuantity,
                                totalPrice = totalPrice,
                                addToCartLoading = ""
                            )
                        }
                        calculateDeliveryCharge()
                    }
                    is Async.Error -> {}
                    is Async.Loading -> {}
                }
            }.launchIn(viewModelScope)
    }

    fun calculateDeliveryCharge() {
        if (state.value.selectedAddress.ADDRESS != null) {
            val addresses = state.value.selectedAddress.ADDRESS?.split(",")
            val insideDhaka =
                addresses?.get(addresses.size - 1)?.lowercase()?.contains("dhaka") == true ||
                        addresses?.get(addresses.size - 1)?.lowercase()?.contains("ঢাকা") == true
            if (MainActivity.userType.value == "1" && state.value.totalPrice < 500) {
                _state.update {
                    it.copy(
                        deliveryCharge = if (insideDhaka) 40 else 150,
                        deliveryTime = if (insideDhaka) "delivery within 12-48 hours"
                        else "delivery within 2-5 days"
                    )
                }
            } else if (state.value.totalPrice < 500) {
                _state.update {
                    it.copy(
                        deliveryCharge = if (insideDhaka) 20 else 180,
                        deliveryTime = if (insideDhaka) "delivery within 6-48 hours"
                        else "delivery within 2-5 days"
                    )
                }
            } else {
                _state.update { it.copy(deliveryCharge = 0, deliveryTime = "") }
            }
        }
    }

    fun addToCart(
        product: ProductsDtoItem,
        quantity: Int,
        salesUnit: String,
        context: Context
    ) {
        productAddUseCase.invoke(
            ProductAdd(
                mobile_no = sharedPreferenceHelper.getUser().MOBILE_NO ?: "",
                pid_product = product.PID_PRODUCT.toString(),
                pqty = quantity.toString(),
                salesunit = salesUnit
            )
        ).onEach { result ->
            when (result) {
                is Async.Success -> {
                    getCartInfo()
                    Toast.makeText(
                        context,
                        result.data?.message ?: "",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Async.Error -> {
                    _state.update { it.copy(addToCartLoading = "") }
                    Toast.makeText(
                        context,
                        result.data?.message ?: "An unknown error occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Async.Loading -> {
                    _state.update {
                        it.copy(addToCartLoading = product.PID_PRODUCT.toString())
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    // ✅ FIXED removeFromCart
    // Only removes the specific unit entry
    fun removeFromCart(item: ProductsDtoItem, salesUnit: String, context: Context) {
        // ✅ Find entries matching PID + SALES_UNIT
        val products = state.value.cartInfoDto.data?.filter {
            it.PID_PRODUCT == item.PID_PRODUCT &&
                    it.SALES_UNIT == salesUnit  // ✅ only remove matching unit!
        } ?: emptyList()

        products.forEach { cartItem ->
            productRemoveUseCase.invoke(
                ProductRemove(
                    sharedPreferenceHelper.getUser().MOBILE_NO ?: "",
                    pid_product = cartItem.PID_PRODUCT.toString(),
                    pid_tran_dtl = cartItem.PID_TRAN_DTL.toString(),
                    salesunit = cartItem.SALES_UNIT ?: salesUnit
                )
            ).onEach { result ->
                when (result) {
                    is Async.Success -> {
                        getCartInfo()
                        Toast.makeText(
                            context,
                            result.data?.message ?: "",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Async.Error -> {
                        _state.update { it.copy(addToCartLoading = "") }
                        Toast.makeText(
                            context,
                            result.data?.message ?: "An unknown error occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Async.Loading -> {
                        _state.update {
                            it.copy(addToCartLoading = cartItem.PID_PRODUCT.toString())
                        }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun getProductFromCart(pidProduct: Int?): CartInfoDtoItem {
        for (item in state.value.cartInfoDto.data ?: emptyList()) {
            if (pidProduct == item.PID_PRODUCT)
                return item
        }
        return CartInfoDtoItem()
    }

    @androidx.annotation.RequiresExtension(extension = android.os.Build.VERSION_CODES.S, version = 7)
    fun submitOrder(context: Context, activity: android.app.Activity, pop: () -> Unit) {
        val mobileNo = sharedPreferenceHelper.getUser().MOBILE_NO ?: return
        getUserUseCase.invoke(id = mobileNo).onEach { result ->
            when (result) {
                is Async.Success -> {
                    val user = result.data?.data?.getOrNull(0)

                    if (user == null) {
                        // User data not returned — do not block, proceed with order.
                        // Verification is best-effort; blocking on null would affect
                        // active users when the API returns an unexpected empty response.
                        proceedWithOrder(context, pop)
                        return@onEach
                    }

                    val isInactive = user.ACTIVE_FLAG
                        ?.equals("Inactive", ignoreCase = true) == true

                    if (isInactive) {
                        // Admin set ACTIVE_FLAG = Inactive → clear cart and logout
                        Toast.makeText(
                            context,
                            "Your account has been blocked. Your cart has been cleared.",
                            Toast.LENGTH_LONG
                        ).show()
                        (activity as? MainActivity)?.clearCartThenLogout(mobileNo)
                        pop()
                    } else if (MainActivity.isRestrict.intValue > 0) {
                        // User has an unpaid delivered order → block order submission
                        Toast.makeText(
                            context,
                            "Please pay your previous bill before placing a new order.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // Active and no restriction → proceed normally
                        proceedWithOrder(context, pop)
                    }
                }
                is Async.Error -> {
                    Toast.makeText(
                        context,
                        "Unable to verify account. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Async.Loading -> {}
            }
        }.launchIn(viewModelScope)
    }

    private fun proceedWithOrder(context: Context, pop: () -> Unit) {
        if (!state.value.selectedAddress.ADDRESS.isNullOrEmpty()) {
            val addresses = state.value.selectedAddress.ADDRESS?.split(",")
            val area =
                if ((addresses?.size ?: 0) > 2)
                    addresses?.get(addresses.size - 2)?.trim()
                else ""

            submitOrderUseCase.invoke(
                area = area ?: "",
                submitOrder = SubmitOrder(
                    mobile_no = sharedPreferenceHelper.getUser().MOBILE_NO ?: "",
                    pid_tran_mst = state.value.cartInfoDto.data?.get(0)?.PID_TRAN_MST ?: "",
                    address = state.value.selectedAddress.ADDRESS ?: "",
                    delivery_charge = state.value.deliveryCharge.toString()
                )
            ).onEach { result ->
                when (result) {
                    is Async.Success -> {
                        Toast.makeText(context, result.data?.message ?: "", Toast.LENGTH_SHORT).show()
                        pop()
                    }
                    is Async.Error -> {}
                    is Async.Loading -> {}
                }
            }.launchIn(viewModelScope)
        } else {
            _state.update { it.copy(showAddressDialog = true) }
        }
    }

    fun closeAddressDialog() {
        _state.update { it.copy(showAddressDialog = false) }
    }

    fun insertAddress(item: AddressDtoItem) {
        insertAddressUseCase.invoke(
            InsertAddress(
                userId = preferenceHelper.getUser().USER_ID.toString(),
                address = item.ADDRESS ?: "",
                addrType = item.ADDR_TYPE ?: ""
            )
        ).onEach { result ->
            when (result) {
                is Async.Success -> { getAddress() }
                is Async.Error -> {}
                is Async.Loading -> {
                    _state.update { it.copy(addressLoading = true) }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun addressSelected(item: AddressDtoItem) {
        _state.update { it.copy(selectedAddress = item) }
        calculateDeliveryCharge()
    }

    fun changeAddress(item: ChangeAddress) {
        changeAddressUseCase.invoke(item).onEach { result ->
            when (result) {
                is Async.Success -> {
                    if (state.value.selectedAddress.PID == item.aid) {
                        _state.update {
                            it.copy(
                                selectedAddress = AddressDtoItem(
                                    PID = item.aid,
                                    ADDRESS = item.address,
                                    ADDR_TYPE = item.addrType
                                )
                            )
                        }
                    }
                    getAddress()
                }
                is Async.Error -> {}
                is Async.Loading -> {
                    _state.update { it.copy(addressLoading = true) }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun deleteAddress(item: DeleteAddress) {
        deleteAddressUseCase.invoke(item).onEach { result ->
            when (result) {
                is Async.Success -> {
                    if (item.pid == state.value.selectedAddress.PID) {
                        _state.update { it.copy(selectedAddress = AddressDtoItem()) }
                    }
                    getAddress()
                }
                is Async.Error -> {}
                is Async.Loading -> {
                    _state.update { it.copy(addressLoading = true) }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun showAddressDialog() {
        _state.update { it.copy(showAddressDialog = true) }
    }
}