package com.friendspharma.app.features.presentation.return_products

import android.os.Build

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.friendspharma.app.R
import com.friendspharma.app.core.components.ActionItem
import com.friendspharma.app.core.components.AppBar
import com.friendspharma.app.core.components.AppName
import com.friendspharma.app.core.components.Loader
import com.friendspharma.app.core.components.TextFieldK
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.core.theme.TextFieldBackGround
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.model.ReturnProductDtoItem
import com.friendspharma.app.features.presentation.home.comonents.CartButton
import com.friendspharma.app.features.presentation.return_products.components.ReturnAddCartDialogue
import com.friendspharma.app.features.presentation.return_products.components.ReturnProductDetails
import com.friendspharma.app.features.presentation.return_products.components.ReturnProductItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun ReturnMedicineScreen(
    viewModel: ReturnMedicineViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions,
    scrollSate: LazyGridState = rememberLazyGridState(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    scope: CoroutineScope = rememberCoroutineScope()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val searchFocusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current
    val cartItem = remember {
        mutableStateOf(ReturnProductDtoItem())
    }
    val width = LocalConfiguration.current.screenWidthDp.dp
    val productWidth = width / 2
    val productHeight = productWidth * 9 / 16

    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.returns),
                navAction = navAction,
                icon = R.drawable.outline_assignment_return_24,
                actions = listOf(
                    ActionItem(
                        Icons.Filled.Person,
                        action = navAction::navToProfile
                    )
                ),
                suffix = {
                    CartButton(cartItemQuantity = state.cartItemQuantity) {
                        navAction.navToReturnCart()
                    }
                },
                openDrawer = {
                    scope.launch {
                        drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->

        LaunchedEffect(Unit) {
            viewModel.init()
        }

        if (cartItem.value.PID_PRODUCT != null)
            ReturnAddCartDialogue(
                item = cartItem.value,
                cartInfo = state.cartInfo,
                addToCart = { item, quantity ->
                    cartItem.value = ReturnProductDtoItem()
                    viewModel.addToCart(item, quantity, context)
                },
                onDismiss = {
                    cartItem.value = ReturnProductDtoItem()
                })

        if (state.currentItem.PID_PRODUCT != null)
            ReturnProductDetails(
                item = state.currentItem,
                cartIds = state.cartIds,
                cartInfo = state.cartInfo,
                addToCart = { product ->
                    cartItem.value = product
                },
                removeFromCart = { product ->
                    viewModel.addToCart(product, (product.QUANTITY ?: 0) - 1, context)
                },
                increaseCartItem = { item ->
                    viewModel.addToCart(item, (item.QUANTITY ?: 0) + 1, context)
                }, addToCartLoading = state.addToCartLoading
            ) {
                viewModel.updateCurrentItem(ReturnProductDtoItem())
            }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextFieldK(
                    value = state.search,
                    onValueChange = { viewModel.searchChanged(it, focusManager) },
                    focusRequester = searchFocusRequester,
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = Primary
                        )
                    },
                    label = R.string.search_pharma,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .weight(1f),
                    height = 40.dp,
                    containerColor = TextFieldBackGround,
                    cornerRadius = 20,
                    borderColor = Color.Transparent,
                    placeHolderFontSize = 14
                )

                Spacer(modifier = Modifier.width(10.dp))
            }

            LazyVerticalGrid(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                state = scrollSate,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(5.dp)
            ) {

                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (!state.isLoading)
                    items(state.allSearchedProduct.data?.size ?: 0) {
                        ReturnProductItem(
                            state.allSearchedProduct.data?.get(it)
                                ?: ReturnProductDtoItem(),
                            cartIds = state.cartIds,
                            cartInfo = state.cartInfo,
                            onTap = { product ->
                                viewModel.updateCurrentItem(product)
                            },
                            addToCart = { product ->

                                cartItem.value = product

                            },
                            removeFromCart = { product ->
                                viewModel.addToCart(
                                    product,
                                    (product.QUANTITY ?: 0) - 1,
                                    context = context
                                )
                            },
                            increaseCartItem = { item ->
                                viewModel.addToCart(item, (item.QUANTITY ?: 0) + 1, context)
                            },
                            height = productHeight,
                            width = productWidth,
                            addToCartLoading = state.addToCartLoading
                        )
                    }


            }
            AppName()
        }

        if (state.isLoading)
            Loader(paddingValues = paddingValues)

    }
}
