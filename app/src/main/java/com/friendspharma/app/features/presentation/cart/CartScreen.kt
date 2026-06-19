package com.friendspharma.app.features.presentation.cart

import com.friendspharma.app.core.util.formatPercent

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.friendspharma.app.core.components.NoContent
import com.friendspharma.app.core.theme.BackGroundDark
import com.friendspharma.app.core.theme.Gray
import com.friendspharma.app.core.theme.GrayLight
import com.friendspharma.app.core.theme.TextFieldBackGround
import com.friendspharma.app.core.util.KeyboardUnFocusHandler
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.model.CartInfoDto
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem
import com.friendspharma.app.features.presentation.cart.components.AddressDialog
import com.friendspharma.app.features.presentation.cart.components.InfoItem
import com.friendspharma.app.features.presentation.home.comonents.IncreaseDecrease
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode


@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions,
    scrollSate: LazyListState = rememberLazyListState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(id = R.string.cart),
                navAction = navAction,
                icon = R.drawable.baseline_shopping_cart_24
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->

        val state by viewModel.state.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val activity = context as Activity

        // ✅ Navigate back when cart is empty
        LaunchedEffect(state.cartInfoDto.data?.size) {
            if (state.cartInfoDto.data != null &&
                state.cartInfoDto.data?.size == 0
            ) {
                navAction.pop()
            }
        }

        val settingResultRequest = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK)
                viewModel.getLocation(context, activity)
            else println("Denied")
        }

        val locationPermissionResultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = {
                if (it) {
                    viewModel.checkLocationSetting(
                        context = context,
                        activity = activity,
                        onDisabled = { intentSenderRequest ->
                            settingResultRequest.launch(intentSenderRequest)
                        },
                        onEnabled = { viewModel.getLocation(context, activity) }
                    )
                }
            }
        )

        KeyboardUnFocusHandler()

        if (state.showAddressDialog) {
            AddressDialog(
                addresses = state.addresses,
                onDismiss = { viewModel.closeAddressDialog() },
                onSelect = { viewModel.addressSelected(it) },
                insertAddress = { viewModel.insertAddress(it) },
                isLoading = state.addressLoading,
                changeAddress = { viewModel.changeAddress(it) },
                deleteAddress = { viewModel.deleteAddress(it) },
                selectedAddress = state.selectedAddress,
                mapAddress = state.address,
                mapPost = state.post,
                mapDistrict = state.district,
                requestLocation = {
                    scope.launch {
                        locationPermissionResultLauncher.launch(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = Color.White)
        ) {
            LazyColumn(
                Modifier
                    .padding(horizontal = 10.dp)
                    .weight(1f),
                state = scrollSate
            ) {
                item { Spacer(modifier = Modifier.height(10.dp)) }

                // ✅ Cart Items
                items(state.cartInfoDto.data?.size ?: 0) { index ->
                    val item = state.cartInfoDto.data?.get(index)

                    // ✅ Use item?.let to safely handle null
                    item?.let { cartItem ->

                        // ✅ Filter cart by unit for IncreaseDecrease
                        val unitCartInfo = remember(
                            state.cartInfoDto,
                            cartItem.SALES_UNIT,
                            cartItem.PID_PRODUCT
                        ) {
                            CartInfoDto(
                                data = state.cartInfoDto.data?.filter {
                                    it.SALES_UNIT == cartItem.SALES_UNIT &&
                                            it.PID_PRODUCT == cartItem.PID_PRODUCT
                                },
                                message = state.cartInfoDto.message
                            )
                        }

                        // ✅ Save is the line-total saving (already summed in the
                        // ViewModel merge) — display directly, do NOT × quantity.
                        val saveAmount = remember(cartItem) {
                            BigDecimal(cartItem.OFFER_VALUE ?: 0.0)
                                .setScale(2, RoundingMode.HALF_UP)
                                .toDouble()
                        }

                        // ✅ Correct total price display
                        val totalPriceDisplay = remember(cartItem) {
                            BigDecimal(cartItem.TOTAL_PRICE ?: 0.0)
                                .setScale(2, RoundingMode.HALF_UP)
                                .toDouble()
                        }

                        Card(
                            modifier = Modifier
                                .padding(vertical = 5.dp)
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(TextFieldBackGround)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = cartItem.IMAGE_URL,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    // ✅ Product name + unit badge
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cartItem.PRODUCT ?: "",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.W600,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Box(
                                            modifier = Modifier.background(
                                                BackGroundDark,
                                                RoundedCornerShape(10.dp)
                                            )
                                        ) {
                                            Text(
                                                text = cartItem.SALES_UNIT ?: "",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.W600,
                                                modifier = Modifier.padding(5.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(5.dp))

                                    // ✅ Price row + IncreaseDecrease
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${cartItem.SALES_PRICE}৳ * " +
                                                    "${cartItem.QUANTITY} ${cartItem.SALES_UNIT}" +
                                                    " = ${totalPriceDisplay}৳",
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        // ✅ Unit filtered cartInfo
                                        IncreaseDecrease(
                                            item = ProductsDtoItem(
                                                PID_PRODUCT = cartItem.PID_PRODUCT
                                            ),
                                            removeFromCart = { product, salesUnit ->
                                                viewModel.removeFromCart(
                                                    product,
                                                    salesUnit,
                                                    context
                                                )
                                            },
                                            increaseCartItem = { product, quantity, salesUnit ->
                                                viewModel.addToCart(
                                                    product,
                                                    quantity,
                                                    salesUnit,
                                                    context
                                                )
                                            },
                                            cartInfo = unitCartInfo,
                                            addToCartLoading = state.addToCartLoading
                                        )
                                    }

                                    // ✅ Correct save display
                                    Text(
                                        text = "Save: ${saveAmount}৳ (${cartItem.SALES_PER?.formatPercent() ?: "0"}%)"
                                    )
                                }
                            }
                        }
                    }
                }

                // ✅ Delivery charge warning
                if (state.deliveryCharge > 0) {
                    item {
                        Text(
                            "Add more ৳${
                                BigDecimal(500 - state.totalPrice)
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .toDouble()
                            } to get free delivery charge",
                            color = Color.Red
                        )
                    }
                }

                // ✅ Order summary + submit button
                if (!state.cartInfoDto.data.isNullOrEmpty())
                    item {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .padding(top = 10.dp)
                        ) {
                            InfoItem("Total Quantity:", "${state.totalQuantity}")
                            InfoItem(
                                "Total Price:",
                                "${
                                    BigDecimal(state.totalPrice)
                                        .setScale(2, RoundingMode.HALF_UP)
                                        .toDouble()
                                }"
                            )
                            InfoItem("Delivery Charge:", "${state.deliveryCharge}")
                            HorizontalDivider()
                            InfoItem(
                                "Sub Total:",
                                "${
                                    BigDecimal(state.totalPrice + state.deliveryCharge)
                                        .setScale(2, RoundingMode.HALF_UP)
                                        .toDouble()
                                }"
                            )

                            if (!state.selectedAddress.ADDRESS.isNullOrEmpty())
                                Column {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        "Delivery address: ",
                                        fontSize = 16.sp,
                                        color = Gray,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 5.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                BackGroundDark.copy(0.75f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.showAddressDialog()
                                            }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                (state.selectedAddress.ADDR_TYPE ?: "") +
                                                        ": " + state.selectedAddress.ADDRESS,
                                                fontSize = 16.sp,
                                                color = Gray,
                                                modifier = Modifier
                                                    .padding(5.dp)
                                                    .weight(1f)
                                            )
                                            Icon(
                                                Icons.Default.EditLocation,
                                                contentDescription = null,
                                                modifier = Modifier.padding(5.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = state.deliveryTime,
                                        color = GrayLight,
                                        modifier = Modifier
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        ButtonK(text = R.string.submit_order) {
                            viewModel.submitOrder(context, activity, navAction::pop)
                        }
                    }

                // ✅ Empty cart
                if (state.cartInfoDto.data != null &&
                    state.cartInfoDto.data?.size == 0
                ) {
                    item { NoContent() }
                }
            }

            AppName()
        }

        if (state.isLoading)
            Loader(paddingValues = paddingValues)
    }
}