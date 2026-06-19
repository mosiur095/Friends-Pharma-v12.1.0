package com.friendspharma.app.features.presentation.return_cart

import android.os.Build

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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.core.theme.TextFieldBackGround
import com.friendspharma.app.core.util.KeyboardUnFocusHandler
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.model.ReturnCartInfoDtoData
import com.friendspharma.app.features.presentation.cart.components.InfoItem
import java.math.BigDecimal
import java.math.RoundingMode


@Composable
fun ReturnCartScreen(
    viewModel: ReturnCartViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions,
    scrollSate: LazyListState = rememberLazyListState()
) {

    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(id = R.string.return_cart),
                navAction = navAction,
                icon = R.drawable.baseline_shopping_cart_24
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->

        val state by viewModel.state.collectAsStateWithLifecycle()
        val context = LocalContext.current

        KeyboardUnFocusHandler()


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
                items(state.cartInfoDto.data?.size ?: 0) {
                    val item = state.cartInfoDto.data?.get(it)

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
                                model = item?.IMAGE_URL, contentDescription = null,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item?.PRODUCT ?: "",
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
                                            text = item?.SALES_UNIT ?: "",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.W600,
                                            modifier = Modifier
                                                .padding(5.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item?.SALES_PRICE.toString() + "৳ * " + item?.QUANTITY + " ${item?.SALES_UNIT} " + " = " + item?.TOTAL_PRICE + "৳",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = item?.QUANTITY.toString(), fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Box(
                                        modifier = Modifier.size(30.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (state.addToCartLoading == item?.PID_PRODUCT.toString())
                                            CircularProgressIndicator(
                                                modifier = Modifier.padding(5.dp),
                                                color = Primary,
                                                strokeWidth = 3.dp
                                            )
                                        else
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = null,
                                                tint = Color.Red,
                                                modifier = Modifier
                                                    .clickable {
                                                        viewModel.returnCartRemoveUseCase(
                                                            item ?: ReturnCartInfoDtoData()
                                                        )
                                                    }
                                            )
                                    }
                                }

                            }
                        }
                    }
                }

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
                                    BigDecimal(state.totalPrice).setScale(2, RoundingMode.HALF_UP)
                                        .toDouble()
                                }"
                            )
                            HorizontalDivider()
                            InfoItem(
                                "Sub Total:",
                                "${
                                    BigDecimal(state.totalPrice).setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                    ).toDouble()
                                }"
                            )

                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        ButtonK(text = R.string.submit_order) {
                            viewModel.submitReturn(context, navAction::pop)
                        }
                    }

                if (state.cartInfoDto.data != null && state.cartInfoDto.data?.size == 0) {
                    item {
                        NoContent()
                    }

                }

            }
            AppName()
        }


        if (state.isLoading)
            Loader(paddingValues = paddingValues)

    }
}
