package com.friendspharma.app.features.presentation.delivery_man.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.friendspharma.app.core.components.NoContent
import com.friendspharma.app.features.data.remote.model.PendignDeliveryDtoItem
import com.friendspharma.app.features.presentation.delivery_man.DeliveryManState
import com.friendspharma.app.features.presentation.delivery_man.DeliveryManViewModel

private val Purple       = Color(0xFF6B4FBB)
private val PurpleLight  = Color(0xFFEDE9FB)
private val PurpleBorder = Color(0xFFC5B9F0)
private val SurfaceBg    = Color(0xFFF8F7FC)
private val DividerColor = Color(0xFFEEEBF5)
private val TextPrimary  = Color(0xFF1A1A2E)
private val TextSecondary= Color(0xFF6E6B80)
private val TextTertiary = Color(0xFFADABB8)
private val GreenTeal    = Color(0xFF1D9E75)
private val GreenBg      = Color(0xFFE8F8F3)

@Composable
fun PendingDeliveries(
    modifier: Modifier = Modifier,
    viewModel: DeliveryManViewModel,
    state: DeliveryManState
) {
    val items = state.deliveries.data ?: emptyList()

    if (items.isEmpty() && !state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending orders", color = TextTertiary, fontSize = 13.sp)
        }
        return
    }

    LazyColumn(
        modifier            = modifier
            .fillMaxSize()
            .background(SurfaceBg)
            .padding(horizontal = 16.dp),
        state               = rememberLazyListState(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.height(6.dp)) }
        items(items.size) { index ->
            val item = items[index]
            OrderCard(
                item          = item,
                onViewInvoice = {
                    // Opens DeliveryDetailsDialog — products load inside dialog
                    viewModel.showDetails(item)
                },
                onPickup      = { viewModel.confirmPickup(item) }
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
        if (items.isEmpty()) item { NoContent() }
    }
}

@Composable
private fun OrderCard(
    item: PendignDeliveryDtoItem,
    onViewInvoice: () -> Unit,
    onPickup: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // ── Row 1: Icon + Invoice No + Status ─────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PurpleLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.Done,
                            contentDescription = null,
                            tint               = Purple,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text       = item.INVOICE_NO ?: "—",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GreenBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text       = item.STATUS ?: "—",
                        fontSize   = 10.sp,
                        color      = GreenTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))

            // ── Row 2: Address + Customer in one compact row ──────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Address
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint               = Purple,
                        modifier           = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text     = item.ADDRESS ?: "—",
                        fontSize = 11.sp,
                        color    = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))
                // Customer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Rounded.Person,
                        contentDescription = null,
                        tint               = Purple,
                        modifier           = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text     = item.USER_NAME ?: "—",
                        fontSize = 11.sp,
                        color    = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ── Phone (tap-to-call + copy) ────────────────────────────────
            if (!item.MOBILE_NO.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                DeliveryPhoneRow(phone = item.MOBILE_NO)
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))

            // ── Row 3: Amount + Buttons ───────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Amount", fontSize = 10.sp, color = TextTertiary)
                    Text(
                        text       = "৳${"%.2f".format(item.TOTAL_AMOUNT ?: 0.0)}",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Purple
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Invoice — view product list
                    OutlinedButton(
                        onClick        = onViewInvoice,
                        shape          = RoundedCornerShape(8.dp),
                        colors         = ButtonDefaults.outlinedButtonColors(
                            containerColor = PurpleLight,
                            contentColor   = Purple
                        ),
                        border         = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                        modifier       = Modifier.height(36.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(text = "Invoice", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    // Pickup — direct confirm
                    Button(
                        onClick   = onPickup,
                        shape     = RoundedCornerShape(8.dp),
                        colors    = ButtonDefaults.buttonColors(containerColor = Purple, contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        modifier  = Modifier.height(36.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Done, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(text = "Pickup", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryPhoneRow(phone: String) {
    val context   = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope     = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }

    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEEF6FF))
                .clickable {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                }
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Rounded.Phone, contentDescription = "Call", tint = Color(0xFF2563EB), modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(5.dp))
            Text(text = phone, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
        }
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (copied) Color(0xFFF0FDF4) else Color(0xFFF8FAFC))
                .clickable {
                    scope.launch {
                        clipboard.setClipEntry(ClipEntry(android.content.ClipData.newPlainText("phone", phone)))
                        copied = true
                        delay(2000)
                        copied = false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = if (copied) Icons.Rounded.Done else Icons.Rounded.ContentCopy,
                contentDescription = if (copied) "Copied" else "Copy",
                tint               = if (copied) Color(0xFF16A34A) else Color(0xFF64748B),
                modifier           = Modifier.size(14.dp)
            )
        }
    }
}