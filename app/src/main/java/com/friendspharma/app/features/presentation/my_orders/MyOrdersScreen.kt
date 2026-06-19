package com.friendspharma.app.features.presentation.my_orders

import android.os.Build

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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.friendspharma.app.R
import com.friendspharma.app.core.components.AppBar
import com.friendspharma.app.core.components.AppName
import com.friendspharma.app.core.components.Loader
import com.friendspharma.app.core.util.KeyboardUnFocusHandler
import com.friendspharma.app.features.NavigationActions
import kotlinx.coroutines.CoroutineScope

// ─── Colors ───────────────────────────────────────────────────────────────────
private val Purple        = Color(0xFF6B4FBB)
private val PurpleLight   = Color(0xFFEDE9FB)
private val TextPrimary   = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF6E6B80)
private val TextTertiary  = Color(0xFFADABB8)
private val SurfaceBg     = Color(0xFFF8F7FC)
private val GreenTeal     = Color(0xFF1D9E75)
private val GreenLight    = Color(0xFFE8F8F3)
private val OrangeColor   = Color(0xFFF39C12)
private val OrangeLight   = Color(0xFFFEF5E7)
private val BlueColor     = Color(0xFF2980B9)
private val BlueLight     = Color(0xFFEBF5FB)
private val RedColor      = Color(0xFFC0392B)
private val RedLight      = Color(0xFFFDEEEE)


@Composable
fun MyOrdersScreen(
    viewModel: MyOrderViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions,
    scope: CoroutineScope = rememberCoroutineScope(),
    scrollSate: LazyListState = rememberLazyListState()
) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(id = R.string.my_orders),
                navAction = navAction,
                icon = R.drawable.baseline_checklist_24
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->

        val state by viewModel.state.collectAsStateWithLifecycle()

        KeyboardUnFocusHandler()

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(SurfaceBg)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f),
                state = scrollSate,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                val orders = state.orders.data ?: emptyList()

                if (!state.isLoading && orders.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No orders yet", fontSize = 15.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(4.dp))
                                Text("Your order history will appear here", fontSize = 12.sp, color = TextTertiary)
                            }
                        }
                    }
                }

                items(orders.size) { index ->
                    val item = orders[index]

                    // ── Status config ─────────────────────────────────────
                    val (statusColor, statusBg) = when (item.ORDER_STATUS?.uppercase()) {
                        "SUBMITTED"        -> Pair(Purple,      PurpleLight)
                        "CONFIRMED"        -> Pair(GreenTeal,   GreenLight)
                        "IN TRANSIT"       -> Pair(OrangeColor, OrangeLight)
                        "DELIVERED"        -> Pair(BlueColor,   BlueLight)
                        "CASH COLLECTION"  -> Pair(GreenTeal,   GreenLight)
                        "CANCELLED"        -> Pair(RedColor,    RedLight)
                        else               -> Pair(TextTertiary, SurfaceBg)
                    }

                    val isEditable = item.ORDER_STATUS?.uppercase() == "SUBMITTED"

                    // ── Clean date (remove time part) ─────────────────────
                    val cleanDate = item.ORDER_DATE
                        ?.substringBefore("T")
                        ?: "—"

                    val amount = item.TOTAL_AMOUNT ?: 0.0

                    // ── Card ──────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .clickable {
                                navAction.navToOrderDetails(item.PID_TRAN_MST.toString())
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: order icon circle
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PurpleLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Purple
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            // Center: order info
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = item.ORDER_NO ?: "—",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        if (isEditable) {
                                            Text(
                                                text = "Editable",
                                                fontSize = 9.sp,
                                                color = Purple,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(PurpleLight)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    // Amount — top right
                                    if (amount > 0.0) {
                                        Text(
                                            text = "৳${"%.2f".format(amount)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Purple
                                        )
                                    }
                                }

                                Spacer(Modifier.height(3.dp))

                                Text(
                                    text = cleanDate,
                                    fontSize = 11.sp,
                                    color = TextTertiary
                                )

                                Spacer(Modifier.height(6.dp))

                                // Status badge — bottom left
                                Text(
                                    text = item.ORDER_STATUS ?: "—",
                                    fontSize = 10.sp,
                                    color = statusColor,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(statusBg)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            // Right: chevron
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
            AppName()
        }

        if (state.isLoading)
            Loader(paddingValues = paddingValues)
    }
}