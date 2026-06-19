package com.friendspharma.app.features.presentation.search

import android.annotation.SuppressLint
import android.os.Build

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.friendspharma.app.MainActivity
import com.friendspharma.app.R
import com.friendspharma.app.core.components.NoContent
import com.friendspharma.app.core.components.SearchTextField
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.core.theme.TextFieldBackGround
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem
import com.friendspharma.app.features.presentation.home.comonents.AddCartDialogue
import com.friendspharma.app.features.presentation.home.comonents.BoxSwitch
import com.friendspharma.app.features.presentation.home.comonents.CartButton
import com.friendspharma.app.features.presentation.home.comonents.ProductDetails
import com.friendspharma.app.features.presentation.home.comonents.SearchedProductItem
import com.friendspharma.app.features.presentation.sign_up.components.UserMessage

@SuppressLint("ContextCastToActivity", "RememberInComposition", "ConfigurationScreenWidthHeight")

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    navAction: NavigationActions,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isRestrict = remember {
        mutableStateOf(false)
    }
    val searchFocusRequester = FocusRequester()

    val cartItem = remember {
        mutableStateOf(ProductsDtoItem())
    }

    if (isRestrict.value)
        UserMessage(stringResource(R.string.service_restricted)) {
            isRestrict.value = false
        }

    if (cartItem.value.PID_PRODUCT != null)
        AddCartDialogue(
            isBox = state.isBox,
            item = cartItem.value,
            cartInfo = state.cartInfo,
            addToCart = { item, quantity, salesUnit ->
                viewModel.addToCart(item, quantity, salesUnit, context)
                cartItem.value = ProductsDtoItem()
            },
            onDismiss = {
                cartItem.value = ProductsDtoItem()
            })

    if (state.currentItem.PID_PRODUCT != null)
        ProductDetails(
            item = state.currentItem,
            cartIds = state.cartIds,
            cartInfo = state.cartInfo,
            isBox = state.isBox,
            addToCart = { product ->
                if (MainActivity.isLoggedIn.value) {
                    if (MainActivity.isRestrict.intValue == 0) {
                        cartItem.value = product
                    } else {
                        isRestrict.value = true
                    }
                } else {
                    navAction.navToLogin()
                }
            },
            removeFromCart = { product, salesUnit ->
                viewModel.removeFromCart(
                    product,
                    salesUnit,
                    context = context
                )
            },
            increaseCartItem = { item, quantity, salesUnit ->
                viewModel.addToCart(item, quantity, salesUnit, context)
            }, addToCartLoading = state.addToCartLoading
        ) {
            viewModel.updateCurrentItem(ProductsDtoItem())
        }


    LaunchedEffect(key1 = Unit) {
        viewModel.checkBoxOrLeaf()
        searchFocusRequester.requestFocus()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.init()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    Scaffold { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = navAction::pop) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = null)
                }
                SearchTextField(
                    value = state.search,
                    onValueChange = { viewModel.searchChanged(it) },
                    focusRequester = searchFocusRequester,
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = Primary
                        )
                    },
                    suffixIcon = {
                        if (state.search.isNotEmpty())
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.clickable {
                                    viewModel.searchChanged("")
                                }
                            )
                    },
                    label = R.string.search,
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .weight(1f),
                    height = 40.dp,
                    containerColor = TextFieldBackGround,
                    cornerRadius = 20,
                    borderColor = Color.Transparent,
                    placeHolderFontSize = 14
                )
                BoxSwitch(isBox = state.isBox) {
                    viewModel.switch()
                }
                Spacer(Modifier.width(5.dp))
                CartButton(cartItemQuantity = MainActivity.cartQuantity.intValue) {
                    navAction.navToCart()
                }

            }

            if (state.searchedProduct.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .fillMaxSize()
                ) {
                    items(state.searchedProduct.size) {
                        val item = state.searchedProduct[it]
                        SearchedProductItem(
                            item = item,
                            cartIds = state.cartIds,
                            cartInfo = state.cartInfo,
                            onTap = { product ->
                                viewModel.updateCurrentItem(product)
                            },
                            addToCart = { product ->
                                if (MainActivity.isLoggedIn.value) {
                                    if (MainActivity.isRestrict.intValue == 0) {
                                        cartItem.value = product
                                    } else {
                                        isRestrict.value = true
                                    }
                                } else {
                                    navAction.navToLogin()
                                }
                            },
                            removeFromCart = { product, salesUnit ->
                                viewModel.removeFromCart(
                                    product,
                                    salesUnit,
                                    context = context
                                )
                            },
                            increaseCartItem = { item, quantity, salesUnit ->
                                viewModel.addToCart(item, quantity, salesUnit, context)
                            },
                            isBox = state.isBox,
                            addToCartLoading = state.addToCartLoading
                        )
                    }
                }
            }

            if (state.search.isNotEmpty() && state.searchedProduct.isEmpty())
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    NoContent(text = stringResource(R.string.no_product_found))
                }
        }
    }
}
