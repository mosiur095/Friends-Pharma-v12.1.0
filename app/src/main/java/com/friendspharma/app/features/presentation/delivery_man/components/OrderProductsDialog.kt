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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.friendspharma.app.features.data.remote.model.OrderDetailsDtoItem
import com.friendspharma.app.features.presentation.delivery_man.DeliveryManState

private val Purple        = Color(0xFF6B4FBB)
private val PurpleLight   = Color(0xFFEDE9FB)
private val PurpleBorder  = Color(0xFFC5B9F0)
private val RedReturn     = Color(0xFFC0392B)
private val RedReturnBg   = Color(0xFFFDEEEE)
private val GreenTeal     = Color(0xFF1D9E75)
private val SurfaceBg     = Color(0xFFF8F7FC)
private val DividerColor  = Color(0xFFEEEBF5)
private val TextPrimary   = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF6E6B80)
private val TextTertiary  = Color(0xFFADABB8)

@Composable
fun OrderProductsDialog(
    state: DeliveryManState,
    onDismiss: () -> Unit,
    onReturnProduct: (Int) -> Unit,
    onRestoreProduct: (Int) -> Unit,
    onUpdateQty: (Int, Double) -> Unit,
    onReturnAll: () -> Unit,
    onUpdateInvoice: (onSuccess: () -> Unit, onError: () -> Unit) -> Unit,
    // ✅ Delivered button — confirms delivery, moves to Delivered tab
    onConfirmCashCollection: () -> Unit
) {
    val products   = state.orderProducts
    val editedQtys = state.editedQuantities
    val isLoading  = state.isProductsLoading

    val originalTotal = products.sumOf { (it.QUANTITY ?: 0.0) * (it.SALES_PRICE ?: 0.0) }
    val updatedTotal  = products.sumOf { p ->
        val qty = editedQtys[p.PID_TRAN_DTL] ?: p.QUANTITY ?: 0.0
        qty * (p.SALES_PRICE ?: 0.0)
    }

    val currentItem = if (state.currentCollectionItem.INVOICE_NO != null)
        state.currentCollectionItem else state.currentDeliveryItem

    val slNo        = currentItem.INVOICE_NO ?: "—"
    val date        = currentItem.TRANSACTION_DATE
        ?.replace("T", "  ")?.replace("Z", "") ?: "—"
    val address     = currentItem.ADDRESS ?: "—"
    val status      = currentItem.STATUS ?: "—"
    val salesAmount = currentItem.SALES_AMOUNT ?: 0.0
    val totalAmount = currentItem.TOTAL_AMOUNT ?: 0.0

    var invoiceError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress      = true,
            dismissOnClickOutside   = true
        )
    ) {
        Surface(
            modifier        = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape           = RoundedCornerShape(20.dp),
            color           = Color.White,
            tonalElevation  = 0.dp,
            shadowElevation = 20.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Header with invoice info ───────────────────────────────
                DialogHeader(
                    slNo        = slNo,
                    date        = date,
                    address     = address,
                    status      = status,
                    salesAmount = salesAmount,
                    totalAmount = totalAmount,
                    onClose     = onDismiss
                )

                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                ColumnLabels()
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
                                    val pid        = product.PID_TRAN_DTL ?: 0
                                    val origQty    = product.QUANTITY ?: 0.0
                                    val currentQty = editedQtys[pid] ?: origQty

                                    ProductRow(
                                        index      = index + 1,
                                        product    = product,
                                        currentQty = currentQty,
                                        origQty    = origQty,
                                        onMinus    = { onUpdateQty(pid, currentQty - 1.0) },
                                        onPlus     = { onUpdateQty(pid, currentQty + 1.0) }
                                    )

                                    if (index < products.lastIndex)
                                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

                // ── Footer ────────────────────────────────────────────────
                DialogFooter(
                    originalTotal           = originalTotal,
                    updatedTotal            = updatedTotal,
                    invoiceError            = invoiceError,
                    onUpdateInvoice         = {
                        invoiceError = false
                        onUpdateInvoice(
                            { /* onSuccess — dialog stays open after update */ },
                            { invoiceError = true }
                        )
                    },
                    onConfirmDelivered      = onConfirmCashCollection
                )
            }
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────
@Composable
private fun DialogHeader(
    slNo: String,
    date: String,
    address: String,
    status: String,
    salesAmount: Double,
    totalAmount: Double,
    onClose: () -> Unit
) {
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
                    text       = "Order Products",
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Purple
                )
                Text(text = slNo, fontSize = 11.sp, color = TextTertiary, letterSpacing = 0.5.sp)
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F2FB))
                    .clickable { onClose() },
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceBg)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InvoiceInfoRow(label = "Date",         value = date)
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            InvoiceInfoRow(label = "Order to",     value = address)
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            InvoiceInfoRow(label = "Status",       value = status, valueColor = GreenTeal)
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            InvoiceInfoRow(
                label      = "Total Amount",
                value      = "৳${"%.2f".format(salesAmount)}",
                valueColor = TextPrimary
            )
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            InvoiceInfoRow(
                label      = "Grand Total",
                value      = "৳${"%.2f".format(totalAmount)}",
                valueColor = Purple,
                bold       = true
            )
        }
    }
}

@Composable
private fun InvoiceInfoRow(
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

@Composable
private fun ColumnLabels() {
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
        Text("QTY",     fontSize = 10.sp, color = TextTertiary, modifier = Modifier.width(80.dp), textAlign = TextAlign.Center)
        Text("Price",   fontSize = 10.sp, color = TextTertiary, modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun ProductRow(
    index: Int,
    product: OrderDetailsDtoItem,
    currentQty: Double,
    origQty: Double,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    val displayName  = product.PRODUCT ?: "—"
    val salesUnit    = product.SALES_UNIT ?: "—"
    val unitPrice    = product.SALES_PRICE ?: 0.0
    val displayPrice = unitPrice * currentQty

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$index", fontSize = 11.sp, color = TextTertiary, modifier = Modifier.width(22.dp))

        Text(
            text       = displayName,
            fontSize   = 12.sp,
            color      = TextPrimary,
            maxLines   = 2,
            overflow   = TextOverflow.Ellipsis,
            lineHeight = 16.sp,
            modifier   = Modifier.weight(1f).padding(end = 4.dp)
        )

        Box(modifier = Modifier.width(46.dp), contentAlignment = Alignment.Center) {
            Text(
                text       = salesUnit,
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

        Row(
            modifier              = Modifier.width(80.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            SmallIconButton(icon = Icons.Rounded.Remove, onClick = onMinus, enabled = currentQty > 0.0)
            Text(
                text       = currentQty.toInt().toString(),
                fontSize   = 12.sp,
                fontWeight = FontWeight.Medium,
                color      = TextPrimary,
                modifier   = Modifier.width(20.dp),
                textAlign  = TextAlign.Center
            )
            SmallIconButton(icon = Icons.Rounded.Add, onClick = onPlus, enabled = currentQty < origQty)
        }

        Text(
            text      = "৳${"%.2f".format(displayPrice)}",
            fontSize  = 11.sp,
            color     = TextSecondary,
            modifier  = Modifier.width(70.dp),
            textAlign = TextAlign.End,
            maxLines  = 1
        )
    }
}

@Composable
private fun SmallIconButton(icon: ImageVector, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(if (enabled) SurfaceBg else Color(0xFFF3F3F3))
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = if (enabled) Purple else TextTertiary,
            modifier           = Modifier.size(11.dp)
        )
    }
}

// ─── Footer: Update Invoice + Delivered ───────────────────────────────────────
@Composable
private fun DialogFooter(
    originalTotal: Double,
    updatedTotal: Double,
    invoiceError: Boolean,
    onUpdateInvoice: () -> Unit,
    onConfirmDelivered: () -> Unit
) {
    val hasChanges = "%.2f".format(originalTotal) != "%.2f".format(updatedTotal)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceBg)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Totals
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            if (hasChanges) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Original", fontSize = 11.sp, color = TextTertiary)
                    Text(
                        text           = "৳${"%.2f".format(originalTotal)}",
                        fontSize       = 12.sp,
                        color          = TextTertiary,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text     = if (hasChanges) "Updated" else "Total",
                    fontSize = 11.sp,
                    color    = TextSecondary
                )
                Text(
                    text       = "৳${"%.2f".format(updatedTotal)}",
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Purple
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Error message
        if (invoiceError) {
            Text(
                text     = "⚠ Failed to update invoice. Please try again.",
                fontSize = 11.sp,
                color    = RedReturn,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(RedReturnBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        // ── Two buttons ───────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Update Invoice — saves changes, dialog stays open
            OutlinedButton(
                onClick        = onUpdateInvoice,
                modifier       = Modifier.weight(1f).height(46.dp),
                shape          = RoundedCornerShape(10.dp),
                colors         = ButtonDefaults.outlinedButtonColors(
                    containerColor = PurpleLight,
                    contentColor   = Purple
                ),
                border         = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
            ) {
                Text(text = "Update Invoice", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            // Delivered — confirms delivery, moves order to Delivered tab
            Button(
                onClick   = onConfirmDelivered,
                modifier  = Modifier.weight(1f).height(46.dp),
                shape     = RoundedCornerShape(10.dp),
                colors    = ButtonDefaults.buttonColors(
                    containerColor = GreenTeal,
                    contentColor   = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(text = "Delivered", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}