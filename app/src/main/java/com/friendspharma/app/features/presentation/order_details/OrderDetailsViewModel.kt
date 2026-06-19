package com.friendspharma.app.features.presentation.order_details

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.core.util.Async
import com.friendspharma.app.features.ScreenArgs
import com.friendspharma.app.features.data.remote.model.OrderDetailsDto
import com.friendspharma.app.features.data.remote.model.OrderDetailsDtoItem
import com.friendspharma.app.features.data.remote.model.TrackOrderDto
import com.friendspharma.app.features.domain.services.GeneratePdf
import com.friendspharma.app.features.domain.use_case.GetOrderDetailsUseCase
import com.friendspharma.app.features.domain.use_case.TrackOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    private val getOrderDetailsUseCase: GetOrderDetailsUseCase,
    private val trackOrderUseCase: TrackOrderUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(OrderDetailsState())
    val state: StateFlow<OrderDetailsState> = _state.asStateFlow()

    private val id: String = checkNotNull(savedStateHandle[ScreenArgs.DATA])

    init {
        getOrder(id)
    }

    fun trackOrder() {
        trackOrderUseCase.invoke(id).onEach { result ->
            when (result) {
                is Async.Success -> {
                    _state.update {
                        it.copy(isLoading = false, track = result.data ?: TrackOrderDto())
                    }
                }
                is Async.Error   -> { _state.update { it.copy(isLoading = false) } }
                is Async.Loading -> { _state.update { it.copy(isLoading = true) } }
            }
        }.launchIn(viewModelScope)
    }

    private fun getOrder(id: String) {
        getOrderDetailsUseCase.invoke(id).onEach { result ->
            when (result) {
                is Async.Success -> {
                    val rawItems = result.data?.data ?: emptyList()

                    // ── Merge rows with same PID_PRODUCT + same SALES_UNIT ──────────
                    // Different SALES_UNIT (e.g. BOX vs STRIP) → keep as separate rows
                    val mergedMap = LinkedHashMap<String, OrderDetailsDtoItem>()

                    for (item in rawItems) {
                        // Key = product ID + unit type; different types stay separate
                        val key = "${item.PID_PRODUCT}_${item.SALES_UNIT}"
                        val existing = mergedMap[key]
                        if (existing == null) {
                            mergedMap[key] = item
                        } else {
                            // Same product + same unit → merge by summing QUANTITY and TOTAL_PRICE
                            val mergedQty        = (existing.QUANTITY   ?: 0.0) + (item.QUANTITY   ?: 0.0)
                            val mergedTotalPrice = (existing.TOTAL_PRICE ?: 0.0) + (item.TOTAL_PRICE ?: 0.0)
                            mergedMap[key] = existing.copy(
                                QUANTITY    = mergedQty,
                                TOTAL_PRICE = mergedTotalPrice
                            )
                        }
                    }

                    val mergedItems = mergedMap.values.toList()

                    // ── Totals calculated from merged items ──────────────────────────
                    var totalQuantity = 0.0
                    var totalMrp      = 0.0  // sum(MRP_PRICE × QUANTITY) → Subtotal
                    var totalPrice    = 0.0  // sum(TOTAL_PRICE)           → Grand Total

                    for (item in mergedItems) {
                        totalQuantity += item.QUANTITY ?: 0.0
                        totalMrp      += (item.MRP_PRICE ?: 0.0) * (item.QUANTITY ?: 0.0)
                        totalPrice    += item.TOTAL_PRICE ?: 0.0
                    }

                    val discount = (totalMrp - totalPrice)
                        .toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toDouble()

                    _state.update {
                        it.copy(
                            isLoading     = false,
                            orders        = result.data ?: OrderDetailsDto(),
                            mergedItems   = mergedItems,
                            totalQuantity = totalQuantity,
                            totalAmount   = totalMrp.toBigDecimal()
                                .setScale(2, RoundingMode.HALF_EVEN).toDouble(),
                            totalPrice    = totalPrice.toBigDecimal()
                                .setScale(2, RoundingMode.HALF_EVEN).toDouble(),
                            discount      = discount
                        )
                    }
                }
                is Async.Error   -> { _state.update { it.copy(isLoading = false, hasError = true) } }
                is Async.Loading -> { _state.update { it.copy(isLoading = true) } }
            }
        }.launchIn(viewModelScope)
    }

    fun generateInvoicePdf(context: Context) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val fileName = "Invoice_${state.value.orders.data?.get(0)?.ORDER_NO}.pdf"
            GeneratePdf.generatePdf(context, state.value.orders, fileName)
            _state.update { it.copy(fileName = fileName, isLoading = false) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportFileToDownloads(context: Context) {
        GeneratePdf.exportPdfToDownloads(context, state.value.fileName)
    }

    fun closeInvoice() { _state.update { it.copy(fileName = "") } }
    fun closeTrack()   { _state.update { it.copy(track = TrackOrderDto()) } }
}