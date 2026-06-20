package com.friendspharma.app.features.presentation.delivery_man

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.activity.compose.BackHandler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.friendspharma.app.R
import com.friendspharma.app.core.components.ActionItem
import com.friendspharma.app.core.components.AppBar
import com.friendspharma.app.core.components.AppName
import com.friendspharma.app.core.components.Loader
import com.friendspharma.app.core.components.ReturnDrawer
import com.friendspharma.app.core.util.KeyboardUnFocusHandler
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.model.PendingDeliveryDto
import com.friendspharma.app.features.presentation.delivery_man.components.CashCollectionList
import com.friendspharma.app.features.presentation.delivery_man.components.DeliveriesDone
import com.friendspharma.app.features.presentation.delivery_man.components.DeliveryDetailsDialog
import com.friendspharma.app.features.presentation.delivery_man.components.OrderProductsDialog
import com.friendspharma.app.features.presentation.delivery_man.components.PaidDeliveries
import com.friendspharma.app.features.presentation.delivery_man.components.PaidDialog
import com.friendspharma.app.features.presentation.delivery_man.components.PendingDeliveries
import com.friendspharma.app.features.presentation.home.comonents.ExitDialogue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val Purple = Color(0xFF6B4FBB)
private val TabBg  = Color(0xFFF4F2FB)

@SuppressLint("ContextCastToActivity")

@Suppress("DEPRECATION")
@Composable
fun DeliveryManScreen(
    viewModel: DeliveryManViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions,
    scope: CoroutineScope = rememberCoroutineScope(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
) {
    val tabs = listOf("Order List", "Intransit", "Delivered", "Cash Collection")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val isExit = remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                ReturnDrawer(
                    closeDrawer = { scope.launch { drawerState.close() } },
                    navAction   = navAction
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    title      = stringResource(id = R.string.deliveries),
                    navAction  = navAction,
                    icon       = R.drawable.baseline_checklist_24,
                    isBack     = false,
                    actions    = listOf(ActionItem(Icons.Filled.Person, action = navAction::navToProfile)),
                    openDrawer = { scope.launch { drawerState.apply { if (isClosed) open() else close() } } },
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
        ) { paddingValues ->

            val state    by viewModel.state.collectAsStateWithLifecycle()
            val activity = LocalContext.current as Activity

            LaunchedEffect(Unit) { viewModel.getUserProfile(navAction) }

            KeyboardUnFocusHandler()

            if (isExit.value) ExitDialogue(activity = activity) { isExit.value = false }
            BackHandler { isExit.value = true }

            // ── Tab 0: Invoice Details Dialog ─────────────────────────────
            // Shows product list for storekeeper validation
            // [Confirm Pickup] → moves order to Intransit
            if (state.currentDeliveryItem.INVOICE_NO != null)
                DeliveryDetailsDialog(
                    item        = state.currentDeliveryItem,
                    state       = state,
                    confirm     = { item ->
                        viewModel.confirmPickup(item)
                        viewModel.closeProductDialog()
                        viewModel.closeDetails()
                    },
                    returnOrder = {
                        viewModel.closeProductDialog()
                        viewModel.closeDetails()
                    },
                    onDismiss   = {
                        viewModel.closeProductDialog()
                        viewModel.closeDetails()
                    },
                    // ✅ Load products only when this specific dialog opens
                    onLoad      = {
                        viewModel.loadOrderProducts(
                            state.currentDeliveryItem.PID_TRAN_MST.toString()
                        )
                    }
                )

            //Tab 1: Intransit — OrderProductsDialog ────────────────────
            // [Update Invoice] → batch POST, dialog stays open & refreshes
            // [Delivered] → confirmDelivered() → moves to Delivered tab
            if (state.showProductDialog
                && state.currentPaid.INVOICE_NO == null
                && state.currentDeliveryItem.INVOICE_NO == null
            ) {
                OrderProductsDialog(
                    state            = state,
                    onDismiss        = { viewModel.closeProductDialog() },
                    onReturnProduct  = { viewModel.returnProduct(it) },
                    onRestoreProduct = { viewModel.restoreProduct(it) },
                    onUpdateQty      = { pid, qty -> viewModel.updateProductQuantity(pid, qty) },
                    onReturnAll      = { viewModel.returnAllProducts() },
                    onRestoreAll     = { viewModel.restoreAllProducts() },
                    onUpdateInvoice  = { onSuccess, onError ->
                        viewModel.submitUpdatedInvoice(
                            onSuccess = { onSuccess() },
                            onError   = { msg ->
                                scope.launch { snackBarHostState.showSnackbar(msg) }
                                onError()
                            }
                        )
                    }
                )
            }

            // ── Tab 3: Cash Collection — PaidDialog (read-only) ───────────
            if (state.currentPaid.INVOICE_NO != null) {
                PaidDialog(
                    state     = state,
                    onDismiss = {
                        viewModel.closeProductDialog()
                        viewModel.closeDetails()
                    }
                )
            }

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(color = Color.White)
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor   = TabBg,
                    contentColor     = Purple,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                .padding(horizontal = 24.dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                            height = 3.dp,
                            color  = Purple
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected               = selectedTabIndex == index,
                            onClick                = {
                                selectedTabIndex = index
                                viewModel.tabSelected(index)
                            },
                            selectedContentColor   = Purple,
                            unselectedContentColor = Color(0xFFADABB8),
                            text = {
                                Text(
                                    text          = title,
                                    fontSize      = 13.sp,
                                    fontWeight    = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                                    letterSpacing = 0.2.sp
                                )
                            }
                        )
                    }
                }

                DeliverySearchBar(
                    query         = state.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange
                )

                Box(Modifier.weight(1f)) {
                    val q = state.searchQuery
                    when (selectedTabIndex) {
                        // Tab 0: Order List → [Invoice] opens product list dialog
                        //                  → [Pickup] confirms directly
                        0 -> PendingDeliveries(
                            viewModel = viewModel,
                            state     = state.copy(deliveries = state.deliveries.filteredBy(q))
                        )

                        // Tab 1: Intransit → click card → OrderProductsDialog → [Update Invoice]
                        1 -> DeliveriesDone(
                            viewModel = viewModel,
                            state     = state.copy(deliveriesDone = state.deliveriesDone.filteredBy(q))
                        )

                        // Tab 2: Delivered → [Confirm Cash Collection] on card
                        2 -> PaidDeliveries(
                            viewModel = viewModel,
                            state     = state.copy(deliveriesPaid = state.deliveriesPaid.filteredBy(q))
                        )

                        // Tab 3: Cash Collection → click card → PaidDialog (read-only)
                        3 -> CashCollectionList(
                            viewModel = viewModel,
                            state     = state.copy(deliveriesCollected = state.deliveriesCollected.filteredBy(q))
                        )
                    }
                }

                AppName()
            }

            if (state.isLoading || state.isProductsLoading)
                Loader(paddingValues = paddingValues)
        }
    }
}

// Filters a delivery list by invoice no, shop name, address, or phone.
private fun PendingDeliveryDto.filteredBy(query: String): PendingDeliveryDto {
    val q = query.trim()
    if (q.isBlank()) return this
    val list = (data ?: emptyList()).filter { item ->
        listOf(item.INVOICE_NO, item.USER_NAME, item.ADDRESS, item.MOBILE_NO)
            .any { it?.contains(q, ignoreCase = true) == true }
    }
    return copy(data = list)
}

@Composable
private fun DeliverySearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder   = { Text("Search invoice, shop or phone", fontSize = 13.sp, color = Color(0xFFADABB8)) },
        leadingIcon   = {
            Icon(Icons.Filled.Search, contentDescription = null, tint = Purple, modifier = Modifier.size(20.dp))
        },
        trailingIcon  = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Color(0xFFADABB8), modifier = Modifier.size(18.dp))
                }
            }
        },
        singleLine = true,
        shape      = RoundedCornerShape(12.dp),
        textStyle  = TextStyle(fontSize = 14.sp, color = Color(0xFF1A1A2E)),
        colors     = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = Purple,
            unfocusedBorderColor    = Color(0xFFE3E0EF),
            cursorColor             = Purple,
            focusedContainerColor   = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}