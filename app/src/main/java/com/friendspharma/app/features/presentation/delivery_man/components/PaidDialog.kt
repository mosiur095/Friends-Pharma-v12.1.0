package com.friendspharma.app.features.presentation.delivery_man.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.friendspharma.app.features.data.remote.model.OrderDetailsDtoItem
import com.friendspharma.app.features.presentation.delivery_man.DeliveryManState

private val Purple        = Color(0xFF6B4FBB)
private val PurpleLight   = Color(0xFFEDE9FB)
private val RedReturn     = Color(0xFFC0392B)
private val RedReturnBg   = Color(0xFFFDEEEE)
private val GreenTeal     = Color(0xFF1D9E75)
private val SurfaceBg     = Color(0xFFF8F7FC)
private val DividerColor  = Color(0xFFEEEBF5)
private val TextPrimary   = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF6E6B80)
private val TextTertiary  = Color(0xFFADABB8)

@Composable
fun PaidDialog(
    state: DeliveryManState,
    onDismiss: () -> Unit
) {
    val item         = state.currentPaid
    val products     = state.orderProducts
    val isLoading    = state.isProductsLoading
    val salesAmount  = item.SALES_AMOUNT  ?: 0.0
    val returnAmount = item.RETURN_AMOUNT ?: 0.0
    val collected    = salesAmount - returnAmount

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress      = true,
            dismissOnClickOutside   = true
        )
    ) {
        Surface(
            modifier        = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.92f),
            shape           = RoundedCornerShape(20.dp),
            color           = Color.White,
            tonalElevation  = 0.dp,
            shadowElevation = 20.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Header — basic invoice info ───────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
                ) {
                    // Title + close button
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text       = "Order Products",
                                fontSize   = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Purple
                            )
                            Text(
                                text          = item.INVOICE_NO ?: "—",
                                fontSize      = 11.sp,
                                color         = TextTertiary,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SurfaceBg)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint               = TextSecondary,
                                modifier           = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ✅ Basic invoice info card (no pills)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceBg)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InvoiceRow(
                            label = "Date",
                            value = item.TRANSACTION_DATE
                                ?.replace("T", "  ")?.replace("Z", "") ?: "—"
                        )
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        InvoiceRow(label = "Order to", value = item.ADDRESS ?: "—")
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        InvoiceRow(
                            label      = "Status",
                            value      = item.STATUS ?: "—",
                            valueColor = GreenTeal
                        )
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        InvoiceRow(
                            label      = "Invoice Amount",
                            value      = "৳${"%.2f".format(salesAmount)}",
                            valueColor = TextPrimary
                        )
                        if (returnAmount > 0.0) {
                            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                            InvoiceRow(
                                label      = "Return Amount",
                                value      = "- ৳${"%.2f".format(returnAmount)}",
                                valueColor = RedReturn
                            )
                        }
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        InvoiceRow(
                            label      = "Collected Amount",
                            value      = "৳${"%.2f".format(collected)}",
                            valueColor = Purple,
                            bold       = true
                        )
                    }
                }

                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

                // ── Column labels ─────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceBg)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#",       fontSize = 10.sp, color = TextTertiary, modifier = Modifier.width(22.dp))
                    Text("Product", fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1f))
                    Text("Type",    fontSize = 10.sp, color = TextTertiary, modifier = Modifier.width(46.dp), textAlign = TextAlign.Center)
                    Text("QTY",     fontSize = 10.sp, color = TextTertiary, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                    Text("Price",   fontSize = 10.sp, color = TextTertiary, modifier = Modifier.width(64.dp), textAlign = TextAlign.End)
                }

                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

                // ── Product list ──────────────────────────────────────────
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(
                                modifier    = Modifier.align(Alignment.Center),
                                color       = Purple,
                                strokeWidth = 2.dp
                            )
                        }
                        products.isEmpty() -> {
                            Text(
                                text     = "No products found",
                                modifier = Modifier.align(Alignment.Center),
                                color    = TextTertiary,
                                fontSize = 13.sp
                            )
                        }
                        else -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                itemsIndexed(
                                    items = products,
                                    key   = { _, p -> p.PID_TRAN_DTL ?: 0 }
                                ) { index, product ->
                                    val qty        = product.QUANTITY ?: 0.0
                                    val isReturned = qty == 0.0
                                    PaidProductRow(index + 1, product, isReturned)
                                    if (index < products.lastIndex)
                                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

                // ── Footer — totals ───────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceBg)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Invoice Amount", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text       = "৳${"%.2f".format(salesAmount)}",
                            fontSize   = 12.sp,
                            color      = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (returnAmount > 0.0) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Return Amount", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text       = "- ৳${"%.2f".format(returnAmount)}",
                                fontSize   = 12.sp,
                                color      = RedReturn,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text       = "Collected Amount",
                            fontSize   = 14.sp,
                            color      = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text       = "৳${"%.2f".format(collected)}",
                            fontSize   = 15.sp,
                            color      = Purple,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ─── Invoice info row ─────────────────────────────────────────────────────────
@Composable
private fun InvoiceRow(
    label: String,
    value: String,
    valueColor: Color = TextSecondary,
    bold: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text       = label,
            fontSize   = if (bold) 13.sp else 12.sp,
            color      = TextTertiary,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            modifier   = Modifier.weight(0.4f)
        )
        Text(
            text       = value,
            fontSize   = if (bold) 14.sp else 12.sp,
            color      = valueColor,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Medium,
            textAlign  = TextAlign.End,
            modifier   = Modifier.weight(0.6f)
        )
    }
}

// ─── Product row ─────────────────────────────────────────────────────────────
@Composable
private fun PaidProductRow(index: Int, product: OrderDetailsDtoItem, isReturned: Boolean) {
    val qty      = product.QUANTITY ?: 0.0
    val rowTotal = qty * (product.SALES_PRICE ?: 0.0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isReturned) Color(0xFFFFF5F5) else Color.White)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$index", fontSize = 11.sp, color = TextTertiary, modifier = Modifier.width(22.dp))

        Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
            Text(
                text       = product.PRODUCT ?: "—",
                fontSize   = 12.sp,
                color      = if (isReturned) TextTertiary else TextPrimary,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
            AnimatedVisibility(
                visible = isReturned,
                enter   = fadeIn(tween(150)) + expandVertically()
            ) {
                Text(
                    text     = "Returned",
                    fontSize = 9.sp,
                    color    = RedReturn,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RedReturnBg)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }

        Box(modifier = Modifier.width(46.dp), contentAlignment = Alignment.Center) {
            Text(
                text       = product.SALES_UNIT ?: "—",
                fontSize   = 9.sp,
                color      = Purple,
                fontWeight = FontWeight.Medium,
                textAlign  = TextAlign.Center,
                modifier   = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(PurpleLight)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }

        Text(
            text       = qty.toInt().toString(),
            fontSize   = 12.sp,
            fontWeight = FontWeight.Medium,
            color      = if (isReturned) TextTertiary else TextPrimary,
            modifier   = Modifier.width(40.dp),
            textAlign  = TextAlign.Center
        )

        Text(
            text      = "৳${"%.2f".format(rowTotal)}",
            fontSize  = 11.sp,
            color     = if (isReturned) TextTertiary else TextSecondary,
            modifier  = Modifier.width(64.dp),
            textAlign = TextAlign.End,
            maxLines  = 1
        )
    }
}