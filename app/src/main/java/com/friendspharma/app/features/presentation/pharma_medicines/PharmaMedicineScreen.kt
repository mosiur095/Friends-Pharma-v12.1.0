package com.friendspharma.app.features.presentation.pharma_medicines

import android.annotation.SuppressLint
import android.os.Build

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
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
import com.friendspharma.app.features.data.remote.model.AllCompanyDtoItem
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem
import com.friendspharma.app.features.presentation.home.comonents.BoxSwitch
import com.friendspharma.app.features.presentation.home.comonents.CartButton
import com.friendspharma.app.features.presentation.home.comonents.ProductDetails
import com.friendspharma.app.features.presentation.home.comonents.ProductItem
import com.friendspharma.app.features.presentation.home.comonents.SearchedProductItem
import com.friendspharma.app.features.presentation.sign_up.components.UserMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val Purple       = Color(0xFF6B4FBB)
private val PurpleLight  = Color(0xFFEDE9FB)
private val SurfaceBg    = Color(0xFFF8F7FC)
private val TextSecondary= Color(0xFF6E6B80)
private val TextTertiary = Color(0xFFADABB8)

@SuppressLint("RememberInComposition")

@Composable
fun PharmaMedicineScreen(
    viewModel: PharmaMedicineViewModel = hiltViewModel(),
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
    val isSearching  = state.search.isNotEmpty()

    Scaffold(
        topBar = {
            AppBar(
                title     = state.company.COMPANY_NAME ?: "",
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
                .background(SurfaceBg)
        ) {
            // ✅ Reusable SearchBar with BoxSwitch as trailing content
            SearchBar(
                value         = state.search,
                onValueChange = { viewModel.searchChanged(it, focusManager) },
                placeholder   = stringResource(R.string.search_pharma),
                focusManager  = focusManager,
                trailingContent = { BoxSwitch(isBox = state.isBox) { viewModel.switch() } }
            )

            LazyVerticalGrid(
                modifier       = Modifier.weight(1f).fillMaxWidth(),
                state          = scrollSate,
                columns        = GridCells.Fixed(2),
                contentPadding = PaddingValues(5.dp)
            ) {
                item(span = { GridItemSpan(2) }) { Spacer(modifier = Modifier.height(8.dp)) }

                // Company tabs
                if (!isSearching) {
                    item(span = { GridItemSpan(2) }) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier       = Modifier.padding(bottom = 8.dp)
                        ) {
                            items(state.allSearchedProduct.size) { index ->
                                val cat        = state.allSearchedProduct[index]
                                val isSelected = state.currentCategoryIndex == index
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .clickable { scope.launch { viewModel.categorySelected(index, scrollSate) } }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) PurpleLight else Color.White)
                                    ) {
                                        if (!cat.data.isNullOrEmpty()) {
                                            AsyncImage(
                                                model              = cat.data[0].IMAGE_URL ?: "",
                                                contentDescription = null,
                                                modifier           = Modifier.size(56.dp).clip(CircleShape),
                                                contentScale       = ContentScale.Crop
                                            )
                                        }
                                        if (isSelected) {
                                            Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Purple.copy(alpha = 0.25f)))
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text       = cat.category ?: "",
                                        fontSize   = 10.sp,
                                        color      = if (isSelected) Purple else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines   = 1,
                                        overflow   = TextOverflow.Ellipsis,
                                        textAlign  = TextAlign.Center,
                                        modifier   = Modifier.width(60.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Search result count
                if (isSearching) {
                    val total = state.allSearchedProduct.sumOf { it.data?.size ?: 0 }
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text       = "Results: $total products",
                            fontWeight = FontWeight.W500,
                            color      = Primary,
                            fontSize   = 14.sp,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Selected category products
                if (state.currentCategoryIndex != -1 && !isSearching) {
                    item(span = { GridItemSpan(2) }) {
                        CategoryHeader(state.allSearchedProduct[state.currentCategoryIndex].category ?: "")
                    }
                    items(state.allSearchedProduct[state.currentCategoryIndex].data?.size ?: 0) {
                        ProductItem(
                            state.allSearchedProduct[state.currentCategoryIndex].data?.get(it) ?: ProductsDtoItem(),
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

                // All products (browse or search)
                state.allSearchedProduct.forEach { category ->
                    val showSection = state.currentCategoryIndex == -1 ||
                            category.category != state.allSearchedProduct[state.currentCategoryIndex].category
                    if (showSection) {
                        if (!isSearching) {
                            item(span = { GridItemSpan(2) }) { CategoryHeader(category.category ?: "") }
                        }
                        items(
                            count = category.data?.size ?: 0,
                            span  = { if (isSearching) GridItemSpan(2) else GridItemSpan(1) }
                        ) {
                            if (isSearching) {
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
                            } else {
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
                }

                if (state.allSearchedProduct.isEmpty()) {
                    item(span = { GridItemSpan(2) }) { NoContent() }
                }

                item(span = { GridItemSpan(2) }) { Spacer(modifier = Modifier.height(10.dp)) }

                // Pharmaceutical companies section
                if (!isSearching) {
                    item(span = { GridItemSpan(2) }) {
                        Row(
                            modifier          = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.width(3.dp).height(18.dp).background(Purple, RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.pharma), color = Purple, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    item(span = { GridItemSpan(2) }) {
                        LazyRow(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                            items(state.companies.data?.size ?: 0) {
                                val item     = state.companies.data?.get(it)
                                val isActive = item?.PID_COMPANY == state.company.PID_COMPANY
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .clickable { scope.launch { viewModel.companySelected(item ?: AllCompanyDtoItem(), scrollSate) } }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isActive) PurpleLight else Color.White)
                                    ) {
                                        AsyncImage(
                                            model              = item?.IMAGE_LOGO_URL ?: "",
                                            contentDescription = null,
                                            modifier           = Modifier.size(60.dp).clip(RoundedCornerShape(10.dp)),
                                            contentScale       = ContentScale.Crop
                                        )
                                        if (isActive) {
                                            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(10.dp)).background(Purple.copy(alpha = 0.2f)))
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text       = item?.COMPANY_NAME ?: "",
                                        fontSize   = 9.sp,
                                        color      = if (isActive) Purple else TextTertiary,
                                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines   = 1,
                                        overflow   = TextOverflow.Ellipsis,
                                        textAlign  = TextAlign.Center,
                                        modifier   = Modifier.width(64.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                item(span = { GridItemSpan(2) }) { Spacer(modifier = Modifier.height(10.dp)) }
            }
            AppName()
        }
        if (state.isLoading) Loader(paddingValues = paddingValues)
    }
}

@Composable
private fun CategoryHeader(title: String) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(3.dp).height(16.dp).background(Color(0xFF6B4FBB), RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B4FBB))
    }
}