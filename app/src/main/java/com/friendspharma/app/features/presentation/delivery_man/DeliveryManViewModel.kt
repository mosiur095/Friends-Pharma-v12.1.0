package com.friendspharma.app.features.presentation.delivery_man

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.MainActivity
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.entity.UpdateInvoiceItem
import com.friendspharma.app.features.data.remote.entity.UpdateInvoiceRequest
import com.friendspharma.app.features.data.remote.model.PendignDeliveryDtoItem
import com.friendspharma.app.features.data.remote.model.PendingDeliveryDto
import com.friendspharma.app.features.data.remote.model.UserDto
import com.friendspharma.app.features.domain.services.SharedPreferenceHelper
import com.friendspharma.app.features.domain.use_case.ConfirmCashCollectionUseCase
import com.friendspharma.app.features.domain.use_case.ConfirmDeliveryUseCase
import com.friendspharma.app.features.domain.use_case.ConfirmPickupUseCase
import com.friendspharma.app.features.domain.use_case.GetDeliveryDoneUseCase
import com.friendspharma.app.features.domain.use_case.GetIntransitDeliveriesUseCase
import com.friendspharma.app.features.domain.use_case.GetOrderDetailsUseCase
import com.friendspharma.app.features.domain.use_case.GetPaidDeliveriesUseCase
import com.friendspharma.app.features.domain.use_case.GetPendingDeliveriesUseCase
import com.friendspharma.app.features.domain.use_case.GetUserUseCase
import com.friendspharma.app.features.domain.use_case.UpdateInvoiceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Snapshot of one invoice line as it stands in the UI at submit time. */
data class InvoiceLineUpdate(
    val pidTranDtl: Int,
    val pidProduct: Int,
    val salesUnit: String,
    val mrpPrice: Double,
    val salesPrice: Double,
    val originalQty: Double,
    val finalQty: Double,
    val returnQty: Double        // originalQty - finalQty
)

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
    private val updateInvoiceUseCase: UpdateInvoiceUseCase
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

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    // ── Order Products ────────────────────────────────────────────────────────
    // Merge by (PID_PRODUCT, SALES_UNIT) and sum quantity — the API returns the
    // same product as several rows with fractional quantities (e.g. 1.5 + 0.5),
    // so we combine them into one whole-quantity line, same as the Order Details
    // screen. Update payload is product-level (pid_product), so we don't need the
    // individual ppid_tran_dtl rows.
    fun loadOrderProducts(orderId: String) {
        getOrderDetailsUseCase.invoke(orderId)
            .onEach { result ->
                when (result) {
                    is Async.Success -> {
                        val products = (result.data?.data ?: emptyList())
                            .filter { (it.QUANTITY ?: 0.0) > 0.0 }
                            .groupBy { (it.PID_PRODUCT ?: 0) to (it.SALES_UNIT ?: "") }
                            .map { (_, rows) ->
                                rows.first().copy(QUANTITY = rows.sumOf { it.QUANTITY ?: 0.0 })
                            }

                        val initialQty = products.associate { item ->
                            (item.PID_TRAN_DTL ?: 0) to (item.QUANTITY ?: 0.0)
                        }
                        _state.update {
                            it.copy(
                                isProductsLoading = false,
                                orderProducts     = products,
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
        val safeQty     = newQty.coerceIn(0.0, originalQty)   // decrease/remove only
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

    fun restoreAllProducts() {
        val updated = state.value.editedQuantities.toMutableMap()
        state.value.orderProducts.forEach { product ->
            updated[product.PID_TRAN_DTL ?: 0] = product.QUANTITY ?: 0.0
        }
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

    // ── Capture the whole invoice as it stands in the UI right now ─────────────
    fun captureInvoiceUpdate(): List<InvoiceLineUpdate> =
        state.value.orderProducts.map { p ->
            val orig  = p.QUANTITY ?: 0.0
            val final = state.value.editedQuantities[p.PID_TRAN_DTL] ?: orig
            InvoiceLineUpdate(
                pidTranDtl  = p.PID_TRAN_DTL ?: 0,
                pidProduct  = p.PID_PRODUCT ?: 0,
                salesUnit   = p.SALES_UNIT ?: "",
                mrpPrice    = p.MRP_PRICE ?: p.SALES_PRICE ?: 0.0,
                salesPrice  = p.SALES_PRICE ?: 0.0,
                originalQty = orig,
                finalQty    = final,
                returnQty   = orig - final
            )
        }

    private fun Double.asQtyString(): String =
        if (this % 1.0 == 0.0) toLong().toString() else toString()

    // ── Update Invoice → single batch POST (return/updateInvoice) ─────────────
    fun submitUpdatedInvoice(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (state.value.isSubmitting) return                                  // double-tap guard
        val mstId    = state.value.currentCollectionItem.PID_TRAN_MST
            ?: return onError("Missing order id")
        val mobileNo = sharedPreferenceHelper.getUser().MOBILE_NO ?: ""

        // Only reduced/removed lines become return items (product-level)
        val items = captureInvoiceUpdate()
            .filter { it.returnQty > 0.0 }
            .map { line ->
                UpdateInvoiceItem(
                    pid_product  = line.pidProduct,
                    psales_type  = line.salesUnit,
                    pmrp_price   = line.mrpPrice.toString(),
                    psales_price = line.salesPrice.toString(),
                    pexpiry_date = "",
                    pReturnqty   = line.returnQty.asQtyString()
                )
            }

        if (items.isEmpty()) { onSuccess(); return }                          // nothing changed

        // TODO product decision: if every line is reduced to 0 (full return),
        // either block here or require a confirm before sending.

        updateInvoiceUseCase.invoke(
            UpdateInvoiceRequest(pid_tran_mst = mstId, mobile_no = mobileNo, items = items)
        ).onEach { result ->
            when (result) {
                is Async.Loading -> {
                    _state.update { it.copy(isSubmitting = true, isProductsLoading = true) }
                }
                is Async.Success -> {
                    if (result.data?.status == 200) {
                        _state.update { it.copy(isSubmitting = false) }
                        // Atomic update applied → re-sync card + dialog with server truth
                        refreshIntransitList()
                        loadOrderProducts(mstId.toString())
                        onSuccess()
                    } else {
                        _state.update { it.copy(isSubmitting = false, isProductsLoading = false) }
                        onError(result.data?.message ?: "Update failed")
                    }
                }
                is Async.Error -> {
                    // Atomic endpoint rolled back → nothing changed; keep edits for retry
                    _state.update { it.copy(isSubmitting = false, isProductsLoading = false) }
                    onError("Couldn't reach server. Please try again.")
                }
            }
        }.launchIn(viewModelScope)
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