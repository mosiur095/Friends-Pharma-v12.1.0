package com.friendspharma.app.features.presentation.category_medicine

import android.annotation.SuppressLint
import android.os.Build

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.friendspharma.app.MainActivity
import com.friendspharma.app.R
import com.friendspharma.app.core.components.ActionItem
import com.friendspharma.app.core.components.AppBar
import com.friendspharma.app.core.components.AppName
import com.friendspharma.app.core.components.Loader
import com.friendspharma.app.core.components.NoContent
import com.friendspharma.app.core.components.SearchBar
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.model.AllCategoryDtoItem
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem
import com.friendspharma.app.features.presentation.home.comonents.BoxSwitch
import com.friendspharma.app.features.presentation.home.comonents.CartButton
import com.friendspharma.app.features.presentation.home.comonents.CategoryItem
import com.friendspharma.app.features.presentation.home.comonents.ProductDetails
import com.friendspharma.app.features.presentation.home.comonents.ProductItem
import com.friendspharma.app.features.presentation.home.comonents.SearchedProductItem
import com.friendspharma.app.features.presentation.sign_up.components.UserMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("RememberInComposition")

@Composable
fun CategoryMedicineScreen(
    viewModel: CategoryMedicineViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions,
    scrollSate: LazyGridState = rememberLazyGridState(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val state        by viewModel.state.collectAsStateWithLifecycle()
    val context      = LocalContext.current
    val focusManager = LocalFocusManager.current
    val width        = LocalConfiguration.current.screenWidthDp.dp
    val productWidth = width / 2
    val productHeight = productWidth * 9 / 16
    val isRestrict   = remember { mutableStateOf(false) }
    val salesUnit    = if (state.isBox) "BOX" else "STRIP"

    Scaffold(
        topBar = {
            AppBar(
                title     = state.category.CATEGORY_NAME ?: "",
                navAction = navAction,
                icon      = R.drawable.baseline_warehouse_24,
                actions   = listOf(ActionItem(Icons.Filled.Person, action = navAction::navToProfile)),
                suffix    = { CartButton(cartItemQuantity = state.cartItemQuantity) { navAction.navToCart() } },
                openDrawer = { scope.launch { drawerState.apply { if (isClosed) open() else close() } } }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->

        LaunchedEffect(Unit) { viewModel.getCartInfo() }

        if (isRestrict.value)
            UserMessage(stringResource(R.string.service_restricted)) { isRestrict.value = false }

        if (state.currentItem.PID_PRODUCT != null)
            ProductDetails(
                item             = state.currentItem,
                cartIds          = state.cartIds,
                cartInfo         = state.cartInfo,
                isBox            = state.isBox,
                addToCart        = { product ->
                    if (MainActivity.isLoggedIn.value) {
                        if (MainActivity.isRestrict.intValue == 0) viewModel.addToCart(product, 1, salesUnit, context)
                        else isRestrict.value = true
                    } else navAction.navToLogin()
                },
                removeFromCart   = { product, unit -> viewModel.removeFromCart(product, unit, context = context) },
                increaseCartItem = { item, quantity, unit -> viewModel.addToCart(item, quantity, unit, context) },
                addToCartLoading = state.addToCartLoading
            ) { viewModel.updateCurrentItem(ProductsDtoItem()) }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // ✅ Reusable SearchBar with BoxSwitch as trailing content
            SearchBar(
                value         = state.search,
                onValueChange = { viewModel.searchChanged(it, focusManager) },
                placeholder   = stringResource(R.string.search_category),
                focusManager  = focusManager,
                trailingContent = { BoxSwitch(isBox = state.isBox) { viewModel.switch() } }
            )

            LazyVerticalGrid(
                modifier       = Modifier.weight(1f).fillMaxWidth(),
                state          = scrollSate,
                columns        = GridCells.Fixed(2),
                contentPadding = PaddingValues(5.dp)
            ) {
                item(span = { GridItemSpan(2) }) { Spacer(modifier = Modifier.height(10.dp)) }

                state.allSearchedProduct.forEach { category ->
                    if (state.search.isNotEmpty() && !category.category.isNullOrEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text       = "Results: ${category.data?.size ?: 0} products",
                                fontWeight = FontWeight.W500,
                                color      = Primary,
                                fontSize   = 14.sp,
                                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (state.search.isNotEmpty()) {
                        items(count = category.data?.size ?: 0, span = { GridItemSpan(2) }) {
                            SearchedProductItem(
                                item             = category.data?.get(it) ?: ProductsDtoItem(),
                                cartIds          = state.cartIds,
                                cartInfo         = state.cartInfo,
                                onTap            = { product -> viewModel.updateCurrentItem(product) },
                                addToCart        = { product ->
                                    if (MainActivity.isLoggedIn.value) {
                                        if (MainActivity.isRestrict.intValue == 0) viewModel.addToCart(product, 1, salesUnit, context)
                                        else isRestrict.value = true
                                    } else navAction.navToLogin()
                                },
                                removeFromCart   = { product, unit -> viewModel.removeFromCart(product, unit, context = context) },
                                increaseCartItem = { item, quantity, unit -> viewModel.addToCart(item, quantity, unit, context) },
                                isBox            = state.isBox,
                                addToCartLoading = state.addToCartLoading
                            )
                        }
                    } else {
                        items(category.data?.size ?: 0) {
                            ProductItem(
                                category.data?.get(it) ?: ProductsDtoItem(),
                                cartIds          = state.cartIds,
                                cartInfo         = state.cartInfo,
                                onTap            = { product -> viewModel.updateCurrentItem(product) },
                                addToCart        = { product ->
                                    if (MainActivity.isLoggedIn.value) {
                                        if (MainActivity.isRestrict.intValue == 0) viewModel.addToCart(product, 1, salesUnit, context)
                                        else isRestrict.value = true
                                    } else navAction.navToLogin()
                                },
                                removeFromCart   = { product, unit -> viewModel.removeFromCart(product, unit, context = context) },
                                increaseCartItem = { item, quantity, unit -> viewModel.addToCart(item, quantity, unit, context) },
                                height           = productHeight,
                                width            = productWidth,
                                isBox            = state.isBox,
                                addToCartLoading = state.addToCartLoading
                            )
                        }
                    }
                }

                if (state.allSearchedProduct.isEmpty()) {
                    item(span = { GridItemSpan(2) }) { NoContent() }
                }

                item(span = { GridItemSpan(2) }) { Spacer(modifier = Modifier.height(10.dp)) }

                item(span = { GridItemSpan(2) }) {
                    Text(
                        stringResource(R.string.categories),
                        color      = Primary,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(start = 5.dp)
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    LazyRow {
                        items(state.categories.data?.size ?: 0) {
                            val item = state.categories.data?.get(it)
                            CategoryItem(
                                item ?: AllCategoryDtoItem(),
                                item?.PID_CATEGORY == state.category.PID_CATEGORY
                            ) { scope.launch { viewModel.categorySelected(it, scrollSate) } }
                        }
                    }
                }
            }
            AppName()
        }

        if (state.isLoading) Loader(paddingValues = paddingValues)
    }
}