package com.friendspharma.app.features.presentation.delivery_man.components

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
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.friendspharma.app.features.data.remote.model.PendignDeliveryDtoItem
import com.friendspharma.app.features.presentation.delivery_man.DeliveryManState

private val Purple        = Color(0xFF6B4FBB)
private val PurpleLight   = Color(0xFFEDE9FB)
private val SurfaceBg     = Color(0xFFF8F7FC)
private val DividerColor  = Color(0xFFEEEBF5)
private val TextPrimary   = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF6E6B80)
private val TextTertiary  = Color(0xFFADABB8)
private val GreenTeal     = Color(0xFF1D9E75)
private val GreenBg       = Color(0xFFE8F8F3)

@Composable
fun DeliveryDetailsDialog(
    item: PendignDeliveryDtoItem,
    state: DeliveryManState,
    confirm: (PendignDeliveryDtoItem) -> Unit,
    returnOrder: (PendignDeliveryDtoItem) -> Unit,
    onDismiss: () -> Unit,
    onLoad: () -> Unit = {}
) {
    val salesAmount  = item.SALES_AMOUNT ?: 0.0
    val totalAmount  = item.TOTAL_AMOUNT  ?: 0.0
    val products     = state.orderProducts
    val isLoading    = state.isProductsLoading

    // ✅ Load products once when dialog opens — triggered by item change only
    LaunchedEffect(item.PID_TRAN_MST) {
        onLoad()
    }

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
            shadowElevation = 20.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Header ────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text       = "Invoice Details",
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

                    // Invoice info card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceBg)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InfoRow("Date",       item.TRANSACTION_DATE?.replace("T", "  ")?.replace("Z", "") ?: "—")
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        InfoRow("Order to",   item.ADDRESS ?: "—")
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        InfoRow("Customer",   item.USER_NAME ?: "—")
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        InfoRow("Mobile",     item.MOBILE_NO ?: "—")
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        InfoRow("Status",     item.STATUS ?: "—", valueColor = GreenTeal)
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        InfoRow("Grand Total","৳${"%.2f".format(totalAmount)}", valueColor = Purple, bold = true)
                    }
                }

                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

                // ── Product list header ───────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceBg)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#",        fontSize = 10.sp, color = TextTertiary, modifier = Modifier.width(24.dp))
                    Text("Product",  fontSize = 10.sp, color = TextTertiary, modifier = Modifier.weight(1f))
                    Text("Type",     fontSize = 10.sp, color = TextTertiary, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                    Text("QTY",      fontSize = 10.sp, color = TextTertiary, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                    Text("Amount",   fontSize = 10.sp, color = TextTertiary, modifier = Modifier.width(64.dp), textAlign = TextAlign.End)
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
                                text     = "Loading products...",
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
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White)
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text     = "${index + 1}",
                                            fontSize = 11.sp,
                                            color    = TextTertiary,
                                            modifier = Modifier.width(24.dp)
                                        )
                                        Text(
                                            text       = product.PRODUCT ?: "—",
                                            fontSize   = 12.sp,
                                            color      = TextPrimary,
                                            maxLines   = 2,
                                            overflow   = TextOverflow.Ellipsis,
                                            lineHeight = 16.sp,
                                            modifier   = Modifier.weight(1f).padding(end = 4.dp)
                                        )
                                        Box(
                                            modifier         = Modifier.width(50.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
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
                                            text       = (product.QUANTITY ?: 0.0).toInt().toString(),
                                            fontSize   = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color      = TextPrimary,
                                            modifier   = Modifier.width(40.dp),
                                            textAlign  = TextAlign.Center
                                        )
                                        Text(
                                            text      = "৳${"%.2f".format((product.QUANTITY ?: 0.0) * (product.SALES_PRICE ?: 0.0))}",
                                            fontSize  = 11.sp,
                                            color     = TextSecondary,
                                            modifier  = Modifier.width(64.dp),
                                            textAlign = TextAlign.End,
                                            maxLines  = 1
                                        )
                                    }
                                    if (index < products.lastIndex)
                                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

                // ── Footer: total + Confirm Pickup ────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceBg)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Total Amount", fontSize = 10.sp, color = TextTertiary)
                            Text(
                                text       = "৳${"%.2f".format(salesAmount)}",
                                fontSize   = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Purple
                            )
                        }
                        // ✅ Confirm Pickup — delivery man verified items with storekeeper
                        Button(
                            onClick   = { confirm(item) },
                            shape     = RoundedCornerShape(10.dp),
                            colors    = ButtonDefaults.buttonColors(
                                containerColor = GreenTeal,
                                contentColor   = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                            modifier  = Modifier.height(46.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.Done,
                                contentDescription = null,
                                modifier           = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text       = "Confirm Pickup",
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = TextSecondary,
    bold: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text       = label,
            fontSize   = 12.sp,
            color      = TextTertiary,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            modifier   = Modifier.weight(0.38f)
        )
        Text(
            text       = value,
            fontSize   = if (bold) 13.sp else 12.sp,
            color      = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            textAlign  = TextAlign.End,
            modifier   = Modifier.weight(0.62f)
        )
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF6E6B80),
    bold: Boolean = false
) {
    InfoRow(label = label, value = value, valueColor = valueColor, bold = bold)
}