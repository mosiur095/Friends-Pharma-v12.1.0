package com.friendspharma.app.features.presentation.order_details

import com.friendspharma.app.core.util.formatPercent

import android.os.Build
import androidx.annotation.RequiresApi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.friendspharma.app.R
import com.friendspharma.app.core.components.AppBar
import com.friendspharma.app.core.components.AppName
import com.friendspharma.app.core.components.ButtonK
import com.friendspharma.app.core.components.Loader
import com.friendspharma.app.core.theme.BackGroundColor
import com.friendspharma.app.core.util.KeyboardUnFocusHandler
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.model.OrderDetailsDtoItem
import com.friendspharma.app.features.data.remote.model.TrackOrderDtoData
import com.friendspharma.app.features.presentation.order_details.components.CourierOptionsDialog
import com.friendspharma.app.features.presentation.order_details.components.InvoiceDialog
import com.friendspharma.app.features.presentation.order_details.components.TrackOrderDialog
import java.io.File
import java.math.RoundingMode

// ─── Colors ────────────────────────────────────────────────────────────────
private val Purple        = Color(0xFF6B4FBB)
private val PurpleLight   = Color(0xFFEDE9FB)
private val SurfaceBg     = Color(0xFFF8F7FC)
private val CardBg        = Color(0xFFFFFFFF)
private val DividerColor  = Color(0xFFF3F4F6)
private val TextPrimary   = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val TextTertiary  = Color(0xFF9CA3AF)
private val GreenTeal     = Color(0xFF16A34A)
private val GreenLight    = Color(0xFFF0FDF4)
private val BlueTag       = Color(0xFF1A7AC4)
private val BlueTagLight  = Color(0xFFE8F4FD)
private val SectionLabel  = Color(0xFF9CA3AF)

@RequiresApi(Build.VERSION_CODES.Q)

@Composable
fun OrderDetailsScreen(
    viewModel: OrderDetailsViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions,
    scrollState: LazyListState = rememberLazyListState()
) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(id = R.string.order_details),
                navAction = navAction,
                icon = R.drawable.baseline_checklist_24
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->

        val state   = viewModel.state.collectAsStateWithLifecycle().value
        val context = LocalContext.current
        val openCourierOptionDialog = remember { mutableStateOf(false) }

        // ── Dialogs ──────────────────────────────────────────────────────────
        if (state.fileName.isNotEmpty())
            InvoiceDialog(File(context.filesDir, state.fileName), download = {
                viewModel.exportFileToDownloads(context)
                viewModel.closeInvoice()
            }) { viewModel.closeInvoice() }

        if (state.track.data != null)
            TrackOrderDialog(state.track.data?.get(0) ?: TrackOrderDtoData()) {
                viewModel.closeTrack()
            }

        if (openCourierOptionDialog.value) {
            CourierOptionsDialog(
                title = stringResource(R.string.select_courier),
                onDismiss = { openCourierOptionDialog.value = false },
                onSelected = {
                    openCourierOptionDialog.value = false
                    if (it == "Pathao") {
                        navAction.navigateToPathaoCourier(
                            state.orders.data?.get(0) ?: OrderDetailsDtoItem()
                        )
                    } else {
                        navAction.navToSteadFastCourier(
                            state.orders.data?.get(0) ?: OrderDetailsDtoItem()
                        )
                    }
                }
            )
        }

        KeyboardUnFocusHandler()

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(SurfaceBg)
        ) {
            // ── Error state ──────────────────────────────────────────────────
            if (state.hasError && !state.isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Unable to load order details.\nPlease go back and try again.",
                        color = TextTertiary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
                AppName()
                return@Column
            }

            // ── Main content ─────────────────────────────────────────────────
            if (state.mergedItems.isNotEmpty()) {
                val firstItem = state.orders.data!!.first()

                // Clean date: "2026-05-11T00:00:00Z" → "2026-05-11  00:00"
                val cleanDate = firstItem.ORDER_DATE
                    ?.replace("T", "  ")
                    ?.replace("Z", "")
                    ?.substringBeforeLast(":")
                    ?: "—"

                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Spacer(Modifier.height(6.dp)) }

                    // ── Order Info card ──────────────────────────────────────
                    item {
                        SectionCard {
                            SectionHeader("Order Info")
                            InfoRow("Order No",       firstItem.ORDER_NO ?: "—")
                            RowDivider()
                            InfoRow("Date",           cleanDate)
                            RowDivider()
                            InfoRow("Deliver To",     firstItem.DELIVERY_ADDRESS ?: "—")
                            RowDivider()
                            InfoRow(
                                "Delivery Charge",
                                if ((firstItem.DELIVERY_CHARGE ?: 0.0) == 0.0) "Free"
                                else "৳${"%.2f".format(firstItem.DELIVERY_CHARGE)}"
                            )
                        }
                    }

                    // ── Products section header + column labels ───────────────
                    item {
                        SectionCard {
                            SectionHeader("Products  (${state.mergedItems.size})")
                            // Column header row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    "Product", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = SectionLabel,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "Type", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = SectionLabel,
                                    modifier = Modifier.width(44.dp),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "Qty", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = SectionLabel,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "Amount", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = SectionLabel,
                                    modifier = Modifier.width(70.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                            RowDivider()

                            // Product rows
                            state.mergedItems.forEachIndexed { index, item ->
                                ProductRow(item)
                                if (index < state.mergedItems.size - 1) RowDivider()
                            }
                        }
                    }

                    // ── Order Summary card ───────────────────────────────────
                    item {
                        SectionCard {
                            SectionHeader("Order Summary")
                            SummaryRow(
                                label = "Subtotal (${state.mergedItems.size} items)",
                                value = "৳${"%.2f".format(state.totalAmount)}",
                                labelColor = TextSecondary,
                                valueColor = TextPrimary
                            )
                            if (state.discount > 0.0) {
                                RowDivider()
                                SummaryRow(
                                    label = "Discount",
                                    value = "− ৳${"%.2f".format(state.discount)}",
                                    labelColor = TextSecondary,
                                    valueColor = GreenTeal
                                )
                            }
                            RowDivider()
                            SummaryRow(
                                label = "Delivery Charge",
                                value = if ((firstItem.DELIVERY_CHARGE ?: 0.0) == 0.0) "Free"
                                else "৳${"%.2f".format(firstItem.DELIVERY_CHARGE)}",
                                labelColor = TextSecondary,
                                valueColor = GreenTeal
                            )
                            // Grand total with dashed separator
                            HorizontalDivider(
                                color = DividerColor,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            SummaryRow(
                                label = "Grand Total",
                                value = "৳${"%.2f".format(state.totalPrice)}",
                                labelColor = TextPrimary,
                                valueColor = Purple,
                                bold = true
                            )
                        }
                    }

                    // ── Action buttons ───────────────────────────────────────
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ButtonK(
                                R.string.track_order,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.trackOrder() }
                            Spacer(Modifier.width(10.dp))
                            ButtonK(
                                R.string.show_invoice,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.generateInvoicePdf(context) }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }

            AppName()
        }

        if (state.isLoading) Loader(paddingValues = paddingValues)
    }
}

// ─── Section wrapper card ───────────────────────────────────────────────────
@Composable
private fun SectionCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .padding(vertical = 4.dp),
        content = content
    )
}

// ─── Section label (e.g. "ORDER INFO") ─────────────────────────────────────
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = SectionLabel,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
    )
    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
}

// ─── Thin divider between rows ──────────────────────────────────────────────
@Composable
private fun RowDivider() {
    HorizontalDivider(
        color = DividerColor,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}

// ─── Key-value info row (Order No, Date, etc.) ──────────────────────────────
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.weight(0.38f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.62f)
        )
    }
}

// ─── Compact product row ─────────────────────────────────────────────────────
// Columns: [thumbnail + name + discount%] | [type badge] | [qty] | [amount + unit price]
@Composable
private fun ProductRow(item: OrderDetailsDtoItem) {
    val lineTotal  = (item.TOTAL_PRICE ?: 0.0)
        .toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
    val unitPrice  = item.SALES_PRICE ?: 0.0
    val qty        = item.QUANTITY?.toInt() ?: 0
    val discountPct = item.SALES_PER ?: 0.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        AsyncImage(
            model = item.IMAGE_URL,
            contentDescription = null,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(8.dp))

        // Product name + discount badge
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.PRODUCT ?: "—",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
            if (discountPct > 0.0) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "-${discountPct.formatPercent()}%",
                    fontSize = 10.sp,
                    color = GreenTeal,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GreenLight)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }

        Spacer(Modifier.width(6.dp))

        // Type badge (SALES_UNIT: BOX / TAB / SYR etc.)
        Text(
            text = item.SALES_UNIT ?: "—",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = BlueTag,
            modifier = Modifier
                .width(44.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(BlueTagLight)
                .padding(horizontal = 4.dp, vertical = 3.dp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.width(6.dp))

        // Quantity
        Text(
            text = "×$qty",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.width(4.dp))

        // Amount + unit price
        Column(
            modifier = Modifier.width(70.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "৳$lineTotal",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "৳$unitPrice/ea",
                fontSize = 10.sp,
                color = TextTertiary
            )
        }
    }
}

// ─── Summary amount row ──────────────────────────────────────────────────────
@Composable
private fun SummaryRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (bold) 14.sp else 12.sp,
            color = labelColor,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = if (bold) 15.sp else 12.sp,
            color = valueColor,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}