package com.friendspharma.app.features.presentation.delivery_man

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.MainActivity
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.entity.AddReturn
import com.friendspharma.app.features.data.remote.entity.SubmitReturn
import com.friendspharma.app.features.data.remote.model.PendignDeliveryDtoItem
import com.friendspharma.app.features.data.remote.model.PendingDeliveryDto
import com.friendspharma.app.features.data.remote.model.UserDto
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.AddToReturnUseCase
import com.friendspharma.app.features.domain.use_case.SubmitReturnUseCase
import com.friendspharma.app.features.domain.use_case.ConfirmCashCollectionUseCase
import com.friendspharma.app.features.domain.use_case.ConfirmDeliveryUseCase
import com.friendspharma.app.features.domain.use_case.ConfirmPickupUseCase
import com.friendspharma.app.features.domain.use_case.GetDeliveryDoneUseCase
import com.friendspharma.app.features.domain.use_case.GetIntransitDeliveriesUseCase
import com.friendspharma.app.features.domain.use_case.GetOrderDetailsUseCase
import com.friendspharma.app.features.domain.use_case.GetPaidDeliveriesUseCase
import com.friendspharma.app.features.domain.use_case.GetPendingDeliveriesUseCase
import com.friendspharma.app.features.domain.use_case.GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DeliveryManViewModel @Inject constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val getPendingDeliveriesUseCase: GetPendingDeliveriesUseCase,
    private val confirmPickupUseCase: ConfirmPickupUseCase,
    private val getIntransitDeliveriesUseCase: GetIntransitDeliveriesUseCase,
    private val confirmDeliveryUseCase: ConfirmDeliveryUseCase,
    private val getDeliveryDoneUseCase: GetDeliveryDoneUseCase,
    private val confirmCashCollectionUseCase: ConfirmCashCollectionUseCase,
    private val getPaidDeliveriesUseCase: GetPaidDeliveriesUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getOrderDetailsUseCase: GetOrderDetailsUseCase,
    private val addToReturnUseCase: AddToReturnUseCase,
    private val submitReturnUseCase: SubmitReturnUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DeliveryManState())
    val state: StateFlow<DeliveryManState> = _state.asStateFlow()

    init { getDeliveries() }

    fun getUserProfile(navAction: NavigationActions) {
        if (MainActivity.isLoggedIn.value) {
            getUserUseCase.invoke(sharedPreferenceHelper.getUser().MOBILE_NO ?: "")
                .onEach { result ->
                    when (result) {
                        is Async.Success -> {
                            val user = result.data?.data?.get(0)
                            sharedPreferenceHelper.saveUser(
                                UserDto(
                                    MOBILE_NO       = user?.MOBILE_NO,
                                    PASSWORD        = user?.PASSWORD,
                                    USER_ID         = user?.USER_ID,
                                    USER_NAME       = user?.USER_NAME,
                                    USER_TYPE       = user?.USER_TYPE.toString(),
                                    APPROVAL_STATUS = sharedPreferenceHelper.getUser().APPROVAL_STATUS
                                )
                            )
                            if (!result.data?.data.isNullOrEmpty()) {
                                if (sharedPreferenceHelper.getUser().USER_TYPE != "4") {
                                    navAction.navToMain()
                                }
                            }
                        }
                        is Async.Error<*>   -> {}
                        is Async.Loading<*> -> {}
                    }
                }.launchIn(viewModelScope)
        }
    }

    // ── Tab 0: Order List ─────────────────────────────────────────────────────
    private fun getDeliveries() {
        getPendingDeliveriesUseCase.invoke(
            sharedPreferenceHelper.getUser().USER_ID.toString()
        ).onEach { result ->
            when (result) {
                is Async.Success -> { _state.update { it.copy(isLoading = false, deliveries = result.data ?: PendingDeliveryDto()) } }
                is Async.Error   -> { _state.update { it.copy(isLoading = false) } }
                is Async.Loading -> { _state.update { it.copy(isLoading = true) } }
            }
        }.launchIn(viewModelScope)
    }

    // ── Tab 0 Action: Confirm Pickup → Intransit ──────────────────────────────
    fun confirmPickup(item: PendignDeliveryDtoItem) {
        confirmPickupUseCase.invoke(item.PID_TRAN_MST.toString())
            .onEach { result ->
                when (result) {
                    is Async.Success -> { getDeliveries(); getIntransitDeliveries() }
                    is Async.Error   -> { _state.update { it.copy(isLoading = false) } }
                    is Async.Loading -> { _state.update { it.copy(isLoading = true) } }
                }
            }.launchIn(viewModelScope)
    }

    // ── Tab 1: Intransit List ─────────────────────────────────────────────────
    private fun getIntransitDeliveries() {
        getIntransitDeliveriesUseCase.invoke(
            sharedPreferenceHelper.getUser().USER_ID.toString()
        ).onEach { result ->
            when (result) {
                is Async.Success -> { _state.update { it.copy(isLoading = false, deliveriesDone = result.data ?: PendingDeliveryDto()) } }
                is Async.Error   -> { _state.update { it.copy(isLoading = false) } }
                is Async.Loading -> { _state.update { it.copy(isLoading = true) } }
            }
        }.launchIn(viewModelScope)
    }

    // ── Tab 1 Action: Confirm Delivered → Delivered ───────────────────────────
    fun confirmDelivered(item: PendignDeliveryDtoItem) {
        confirmDeliveryUseCase.invoke(
            item.PID_TRAN_MST.toString(),
            sharedPreferenceHelper.getUser().USER_ID.toString()
        ).onEach { result ->
            when (result) {
                is Async.Success -> { getIntransitDeliveries(); getDeliveryDone() }
                is Async.Error   -> { _state.update { it.copy(isLoading = false) } }
                is Async.Loading -> { _state.update { it.copy(isLoading = true) } }
            }
        }.launchIn(viewModelScope)
    }

    // ── Tab 2: Delivered List ─────────────────────────────────────────────────
    private fun getDeliveryDone() {
        getDeliveryDoneUseCase.invoke(
            sharedPreferenceHelper.getUser().USER_ID.toString()
        ).onEach { result ->
            when (result) {
                is Async.Success -> { _state.update { it.copy(isLoading = false, deliveriesPaid = result.data ?: PendingDeliveryDto()) } }
                is Async.Error   -> { _state.update { it.copy(isLoading = false) } }
                is Async.Loading -> { _state.update { it.copy(isLoading = true) } }
            }
        }.launchIn(viewModelScope)
    }

    fun refreshDeliveredList() { getDeliveryDone() }
    fun refreshIntransitList() { getIntransitDeliveries() }

    // ── Tab 2 Action: Confirm Cash Collection ─────────────────────────────────
    fun confirmCollection(id: String) {
        confirmCashCollectionUseCase.invoke(
            id,
            sharedPreferenceHelper.getUser().USER_ID.toString()
        ).onEach { result ->
            when (result) {
                is Async.Success -> { getDeliveryDone(); getPaidDeliveries() }
                is Async.Error   -> { _state.update { it.copy(isLoading = false) } }
                is Async.Loading -> { _state.update { it.copy(isLoading = true) } }
            }
        }.launchIn(viewModelScope)
    }

    // ── Tab 3: Cash Collection List ───────────────────────────────────────────
    private fun getPaidDeliveries() {
        getPaidDeliveriesUseCase.invoke(
            sharedPreferenceHelper.getUser().USER_ID.toString()
        ).onEach { result ->
            when (result) {
                is Async.Success -> { _state.update { it.copy(isLoading = false, deliveriesCollected = result.data ?: PendingDeliveryDto()) } }
                is Async.Error   -> { _state.update { it.copy(isLoading = false) } }
                is Async.Loading -> { _state.update { it.copy(isLoading = true) } }
            }
        }.launchIn(viewModelScope)
    }

    fun tabSelected(index: Int) {
        when (index) {
            0 -> getDeliveries()
            1 -> getIntransitDeliveries()
            2 -> getDeliveryDone()
            3 -> getPaidDeliveries()
        }
    }

    // ── Order Products ────────────────────────────────────────────────────────
    fun loadOrderProducts(orderId: String) {
        getOrderDetailsUseCase.invoke(orderId)
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        val filteredProducts = (result.data?.data ?: emptyList())
                            .filter { it.QUANTITY != null && it.QUANTITY > 0.0 }
                        val mergedProducts = filteredProducts
                            .groupBy { Pair(it.PID_PRODUCT, it.SALES_UNIT) }
                            .map { (_, items) ->
                                val first      = items.first()
                                val totalQty   = items.sumOf { it.QUANTITY ?: 0.0 }
                                val totalPrice = items.sumOf { it.TOTAL_PRICE ?: 0.0 }
                                first.copy(QUANTITY = totalQty, TOTAL_PRICE = totalPrice)
                            }
                        val initialQty = mergedProducts.associate { item ->
                            (item.PID_TRAN_DTL ?: 0) to (item.QUANTITY ?: 0.0)
                        }
                        _state.update {
                            it.copy(
                                isProductsLoading = false,
                                orderProducts     = mergedProducts,
                                editedQuantities  = initialQty,
                                showProductDialog = true
                            )
                        }
                    }
                    is Async.Error   -> { _state.update { it.copy(isProductsLoading = false) } }
                    is Async.Loading -> { _state.update { it.copy(isProductsLoading = true) } }
                }
            }.launchIn(viewModelScope)
    }

    fun updateProductQuantity(pidTranDtl: Int, newQty: Double) {
        val product     = state.value.orderProducts.find { it.PID_TRAN_DTL == pidTranDtl }
        val originalQty = product?.QUANTITY ?: 0.0
        val safeQty     = newQty.coerceIn(0.0, originalQty)
        val updated     = state.value.editedQuantities.toMutableMap()
        updated[pidTranDtl] = safeQty
        _state.update { it.copy(editedQuantities = updated) }
    }

    fun returnProduct(pidTranDtl: Int) {
        val updated = state.value.editedQuantities.toMutableMap()
        updated[pidTranDtl] = 0.0
        _state.update { it.copy(editedQuantities = updated) }
    }

    fun restoreProduct(pidTranDtl: Int) {
        val product     = state.value.orderProducts.find { it.PID_TRAN_DTL == pidTranDtl }
        val originalQty = product?.QUANTITY ?: 0.0
        val updated     = state.value.editedQuantities.toMutableMap()
        updated[pidTranDtl] = originalQty
        _state.update { it.copy(editedQuantities = updated) }
    }

    fun returnAllProducts() {
        val updated = state.value.editedQuantities.toMutableMap()
        state.value.orderProducts.forEach { updated[it.PID_TRAN_DTL ?: 0] = 0.0 }
        _state.update { it.copy(editedQuantities = updated) }
    }

    fun calculateUpdatedTotal(): Double {
        return state.value.orderProducts.sumOf { product ->
            val editedQty = state.value.editedQuantities[product.PID_TRAN_DTL] ?: product.QUANTITY ?: 0.0
            val unitPrice = product.SALES_PRICE ?: 0.0
            editedQty * unitPrice
        }
    }

    fun isAllReturned(): Boolean =
        state.value.editedQuantities.values.all { it == 0.0 }

    fun getEffectiveQty(pidTranDtl: Int): Double {
        val product = state.value.orderProducts.find { it.PID_TRAN_DTL == pidTranDtl }
        return state.value.editedQuantities[pidTranDtl] ?: product?.QUANTITY ?: 0.0
    }

    fun isProductReturned(pidTranDtl: Int): Boolean =
        (state.value.editedQuantities[pidTranDtl] ?: 1.0) == 0.0

    fun submitReturnedProducts(onSuccess: () -> Unit, onError: () -> Unit) {
        val mstId      = state.value.currentCollectionItem.PID_TRAN_MST ?: return
        val mobileNo   = sharedPreferenceHelper.getUser().MOBILE_NO ?: ""
        val products   = state.value.orderProducts
        val editedQtys = state.value.editedQuantities

        val changedProducts = products.filter { product ->
            val origQty   = product.QUANTITY ?: 0.0
            val editedQty = editedQtys[product.PID_TRAN_DTL] ?: origQty
            editedQty < origQty
        }

        // Nothing changed — treat as success
        if (changedProducts.isEmpty()) { onSuccess(); return }

        _state.update { it.copy(isProductsLoading = true) }
        var completed = 0
        var hasError  = false

        // POST return/AddReturn for each changed product
        // NOTE: Do NOT call SubmitReturn — it resets order status back to pending
        changedProducts.forEach { product ->
            val origQty   = product.QUANTITY ?: 0.0
            val editedQty = editedQtys[product.PID_TRAN_DTL] ?: origQty
            val returnQty = (origQty - editedQty).toInt()

            addToReturnUseCase.invoke(
                AddReturn(
                    mobile_no     = mobileNo,
                    pReturnqty    = returnQty.toString(),
                    pexpiry_date  = "",
                    pid_product   = product.PID_PRODUCT ?: 0,
                    pid_tran_mst  = mstId,
                    pmrp_price    = (product.MRP_PRICE ?: product.SALES_PRICE ?: 0.0).toString(),
                    ppid_tran_dtl = product.PID_TRAN_DTL ?: 0,
                    psales_price  = product.SALES_PRICE?.toString() ?: "0",
                    psales_type   = product.SALES_UNIT ?: ""
                )
            ).onEach { result ->
                when (result) {
                    is Async.Success -> {
                        completed++
                        if (completed == changedProducts.size) {
                            _state.update { it.copy(isProductsLoading = false) }
                            if (hasError) onError() else onSuccess()
                        }
                    }
                    is Async.Error   -> {
                        hasError = true
                        completed++
                        if (completed == changedProducts.size) {
                            _state.update { it.copy(isProductsLoading = false) }
                            onError()
                        }
                    }
                    is Async.Loading -> { _state.update { it.copy(isProductsLoading = true) } }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun closeProductDialog() {
        _state.update {
            it.copy(showProductDialog = false, orderProducts = emptyList(), editedQuantities = emptyMap())
        }
    }

    fun showDetails(item: PendignDeliveryDtoItem) {
        _state.update { it.copy(currentDeliveryItem = item) }
    }

    fun showPaidDetails(item: PendignDeliveryDtoItem) {
        _state.update { it.copy(currentPaid = item) }
    }

    fun closeDetails() {
        _state.update {
            it.copy(
                currentDeliveryItem   = PendignDeliveryDtoItem(),
                currentCollectionItem = PendignDeliveryDtoItem(),
                currentPaid           = PendignDeliveryDtoItem()
            )
        }
    }

    fun showCollectionDetails(item: PendignDeliveryDtoItem) {
        _state.update { it.copy(currentCollectionItem = item) }
    }
}