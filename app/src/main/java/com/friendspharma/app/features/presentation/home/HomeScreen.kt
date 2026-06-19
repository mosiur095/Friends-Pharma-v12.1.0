package com.friendspharma.app.features.presentation.home

import com.friendspharma.app.core.util.formatPercent

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.friendspharma.app.MainActivity
import com.friendspharma.app.R
import com.friendspharma.app.core.components.AppName
import com.friendspharma.app.core.components.NoContent
import com.friendspharma.app.core.components.SearchBar
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.features.NavigationActions
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem
import com.friendspharma.app.features.presentation.home.comonents.BoxSwitch
import com.friendspharma.app.features.presentation.home.comonents.ExitDialogue
import com.friendspharma.app.features.presentation.home.comonents.GetSpecialDialogue
import com.friendspharma.app.features.presentation.home.comonents.ProductDetails
import com.friendspharma.app.features.presentation.home.comonents.ProductItem
import com.friendspharma.app.features.presentation.home.comonents.SearchedProductItem
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val BannerGradient = listOf(Color.Black.copy(alpha = 0.80f), Color.Black.copy(alpha = 0.0f))
private val DiscountRed    = Color(0xFFE53935)
private val DiscountOrange = Color(0xFFFF6F00)
private val SaveGreen      = Color(0xFF69F0AE)

@SuppressLint("ContextCastToActivity", "RememberInComposition", "ConfigurationScreenWidthHeight")

@Suppress("DEPRECATION")
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navAction: NavigationActions,
    scrollSate: LazyGridState = rememberLazyGridState(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val state             by viewModel.state.collectAsStateWithLifecycle()
    val context           = LocalContext.current
    val activity          = LocalContext.current as Activity
    val isExit            = remember { mutableStateOf(false) }
    val isRestrict        = remember { mutableStateOf(false) }
    val isSpecialDialogue = remember { mutableStateOf(false) }
    val focusManager      = LocalFocusManager.current
    val haptic            = LocalHapticFeedback.current
    val width             = LocalConfiguration.current.screenWidthDp.dp
    val productWidth      = width / 2
    val productHeight     = productWidth * 9 / 16
    val bannerHeight      = (width * 9 / 20)
    val salesUnit         = if (state.isBox) "BOX" else "STRIP"

    // ✅ OPTIMIZATION: hoisted outside grid item — prevents new scroll state on recompose
    val registerScrollState = rememberScrollState()

    // ✅ OPTIMIZATION: computed with stable keys — cheaper equality check
    // Use allProduct (already stock-filtered, same data the grid shows) so the
    // banner always has products. Derive discount % from MRP vs sale price when
    // SALES_PER is null so all discounted products appear in the banner.
    val topDiscountProducts = remember(state.allProduct, state.isBox) {
        state.allProduct
            .flatMap { it.data ?: emptyList() }
            .mapNotNull { product ->
                val mrp       = if (state.isBox) product.BOX_MRP_PRICE   else product.LEAF_MRP_PRICE
                val salePrice = if (state.isBox) product.BOX_SALES_PRICE else product.LEAF_SALES_PRICE
                val explicit  = if (state.isBox) product.BOX_SALES_PER   else product.LEAF_SALES_PER
                val pct = when {
                    (explicit ?: 0.0) > 0.0                                        -> explicit!!
                    mrp != null && salePrice != null
                            && mrp > 0.0 && salePrice < mrp -> ((mrp - salePrice) / mrp) * 100.0
                    else                                                            -> null
                }
                if (pct != null && pct > 0.0) product to pct else null
            }
            .sortedByDescending { (_, pct) -> pct }
            .take(5)
            .map { (product, _) -> product }
    }

    val pagerState        = rememberPagerState(initialPage = 0) { topDiscountProducts.size }
    val swipeRefreshState  = rememberSwipeRefreshState(isRefreshing = state.refreshLoading)
    // ✅ Scroll to Top — show FAB only when user scrolls past 3 items
    val showScrollToTop = remember { androidx.compose.runtime.derivedStateOf {
        scrollSate.firstVisibleItemIndex > 3
    } }

    if (isRestrict.value)
        ServiceRestrictedDialog { isRestrict.value = false }

    if (state.currentItem.PID_PRODUCT != null)
        ProductDetails(
            item             = state.currentItem,
            cartIds          = state.cartIds,
            cartInfo         = state.cartInfo,
            isBox            = state.isBox,
            addToCart        = { product ->
                if (MainActivity.isLoggedIn.value) {
                    if (MainActivity.isRestrict.intValue <= 0)
                        viewModel.addToCart(product, 1, salesUnit, context)
                    else isRestrict.value = true
                } else navAction.navToLogin()
            },
            removeFromCart   = { product, unit ->
                viewModel.removeFromCart(product, unit, context = context)
            },
            increaseCartItem = { item, quantity, unit ->
                viewModel.addToCart(item, quantity, unit, context)
            },
            addToCartLoading = state.addToCartLoading
        ) { viewModel.updateCurrentItem(ProductsDtoItem()) }

    if (isExit.value) ExitDialogue(activity = activity) { isExit.value = false }
    if (isSpecialDialogue.value) GetSpecialDialogue { isSpecialDialogue.value = false }

    BackHandler {
        if (state.search.isNotEmpty()) viewModel.searchChanged("", focusManager)
        else if (!state.isSortedeByCategory) scope.launch { viewModel.sortByCategory(scrollSate) }
        else isExit.value = true
    }

    // Re-run checkBoxOrLeaf when login state changes so box/leaf mode is
    // always correct after login/logout (userType may have changed).
    val isLoggedIn by MainActivity.isLoggedIn
    LaunchedEffect(isLoggedIn) {
        viewModel.checkBoxOrLeaf()   // always set correct mode first
        if (!isLoggedIn) {
            // ✅ Call reloadForLogout() on ANY logout — normal or forced
            // hasActiveSession() check removed — it caused force logout
            // to skip reloadForLogout() because notifyForcedLogout() had
            // already reset lastKnownLoginState = false before this fired
            viewModel.reloadForLogout(navAction)
        }
    }

    // Register forced logout callback. Also consume pendingForcedLogout here —
    // this fires when HomeScreen recomposes after a forced logout that happened
    // while the user was on CartScreen (onForcedLogout was null at that time).
    DisposableEffect(Unit) {
        MainActivity.onForcedLogout = { viewModel.notifyForcedLogout() }
        if (MainActivity.pendingForcedLogout) {
            MainActivity.pendingForcedLogout = false
            viewModel.notifyForcedLogout()
        }
        onDispose { MainActivity.onForcedLogout = null }
    }

    // ── Auto-scroll banner every 5 seconds ────────────────────────────────────
    LaunchedEffect(pagerState, topDiscountProducts.size) {
        if (topDiscountProducts.size > 1) {
            while (true) {
                delay(5000)
                if (pagerState.canScrollForward || pagerState.currentPage > 0) {
                    val target = if (pagerState.currentPage < topDiscountProducts.size - 1)
                        pagerState.currentPage + 1 else 0
                    pagerState.animateScrollToPage(target)
                }
            }
        }
    }

    // ── Lifecycle: smart resume refresh + polling ─────────────────────────────
    // ✅ OPTIMIZATION: replaced old init()-on-every-resume (which reloaded ALL
    // products from network each time) with time-aware smart refresh
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.onAppForegrounded(navAction)
                Lifecycle.Event.ON_PAUSE  -> viewModel.onAppBackgrounded()
                else                      -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SwipeRefresh(
            state     = swipeRefreshState,
            onRefresh = {
                viewModel.getUserProfile(navAction)
                viewModel.getProducts(isRefresh = true)
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                SearchBar(
                    value           = state.search,
                    onValueChange   = { viewModel.searchChanged(it, focusManager) },
                    placeholder     = stringResource(R.string.search_pharma),
                    focusManager    = focusManager,
                    trailingContent = {
                        val userType  = MainActivity.userType.value
                        val canToggle = isLoggedIn && (userType == "2" || userType == "3")
                        BoxSwitch(isBox = state.isBox) {
                            if (canToggle) viewModel.switch()   // guard: only type 2 & 3 can toggle
                        }
                    }
                )

                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()) {

                    // ✅ Flatten product list here (Composable scope) — safe to use remember
                    // Cannot use remember inside LazyGridScope
                    val flatProducts = remember(state.allProduct, state.currentCategoryIndex) {
                        if (state.currentCategoryIndex == -1) {
                            state.allProduct.flatMap { it.data ?: emptyList() }
                        } else {
                            state.allProduct
                                .filter { it.category != state.allProduct[state.currentCategoryIndex].category }
                                .flatMap { it.data ?: emptyList() }
                        }
                    }

                    LazyVerticalGrid(
                        state          = scrollSate,
                        columns        = GridCells.Fixed(2),
                        contentPadding = PaddingValues(5.dp)
                    ) {

                        // ── Banner ────────────────────────────────────────────────
                        if (state.search.isEmpty() && topDiscountProducts.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                HorizontalPager(state = pagerState) { pageIndex ->
                                    val product    = topDiscountProducts.getOrNull(pageIndex)
                                    val mrpPrice   = if (state.isBox) product?.BOX_MRP_PRICE   else product?.LEAF_MRP_PRICE
                                    val salePrice  = if (state.isBox) product?.BOX_SALES_PRICE else product?.LEAF_SALES_PRICE
                                    val saveAmount = if (state.isBox) product?.BOX_OFFER_VALUE else product?.LEAF_OFFER_VALUE
                                    val explicit   = if (state.isBox) product?.BOX_SALES_PER   else product?.LEAF_SALES_PER
                                    // Derive discount % same way as topDiscountProducts selection
                                    val discountPercent: Double? = when {
                                        (explicit ?: 0.0) > 0.0                                              -> explicit
                                        mrpPrice != null && salePrice != null
                                                && mrpPrice > 0.0 && salePrice < mrpPrice -> ((mrpPrice - salePrice) / mrpPrice) * 100.0
                                        else                                                                  -> null
                                    }

                                    // ✅ OPTIMIZATION: banner image with disk + memory cache
                                    val bannerRequest = remember(product?.IMAGE_URL) {
                                        ImageRequest.Builder(context)
                                            .data(product?.IMAGE_URL)
                                            .size(
                                                width.value.toInt() * 2,
                                                bannerHeight.value.toInt() * 2
                                            )
                                            .memoryCacheKey(product?.IMAGE_URL)
                                            .diskCacheKey(product?.IMAGE_URL)
                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .crossfade(300)
                                            .build()
                                    }

                                    Box(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .width(width)
                                            .height(bannerHeight)
                                            .clip(shape = RoundedCornerShape(15.dp))
                                            .clickable { product?.let { viewModel.updateCurrentItem(it) } }
                                    ) {
                                        AsyncImage(
                                            model              = bannerRequest,
                                            contentDescription = null,
                                            contentScale       = ContentScale.Crop,
                                            modifier           = Modifier
                                                .width(width)
                                                .height(bannerHeight)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(width)
                                                .height(bannerHeight)
                                                .background(
                                                    brush = Brush.horizontalGradient(
                                                        colors = BannerGradient
                                                    )
                                                )
                                        )
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.CenterStart)
                                                .padding(16.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = DiscountRed,
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text       = "🔥 BEST DEAL",
                                                    color      = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize   = 11.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text       = product?.PRODUCT_NAME ?: "",
                                                color      = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize   = 16.sp,
                                                maxLines   = 2,
                                                overflow   = TextOverflow.Ellipsis,
                                                modifier   = Modifier.width(width * 0.65f)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (discountPercent != null) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(
                                                                color = DiscountOrange,
                                                                shape = RoundedCornerShape(4.dp)
                                                            )
                                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                                    ) {
                                                        Text(
                                                            text       = "${discountPercent.formatPercent()}% OFF",
                                                            color      = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize   = 13.sp
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                if (saveAmount != null) {
                                                    Text(
                                                        text       = "Save ${saveAmount.toInt()}৳",
                                                        color      = SaveGreen,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize   = 13.sp
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text           = "${mrpPrice}৳",
                                                    color          = Color.White.copy(alpha = 0.6f),
                                                    textDecoration = TextDecoration.LineThrough,
                                                    fontSize       = 12.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text       = "${salePrice}৳",
                                                    color      = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize   = 18.sp
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 8.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            repeat(topDiscountProducts.size) { index ->
                                                Box(
                                                    modifier = Modifier
                                                        .padding(horizontal = 3.dp)
                                                        .size(
                                                            if (pagerState.currentPage == index) 8.dp
                                                            else 5.dp
                                                        )
                                                        .background(
                                                            color = if (pagerState.currentPage == index)
                                                                Color.White
                                                            else Color.White.copy(alpha = 0.4f),
                                                            shape = RoundedCornerShape(50)
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ── Register banners ──────────────────────────────────────
                        if (!MainActivity.isLoggedIn.value && state.search.isEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                // ✅ OPTIMIZATION: scroll state hoisted above — stable across recompositions
                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 10.dp)
                                        .horizontalScroll(registerScrollState),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Spacer(modifier = Modifier.width(2.dp))

                                    // ── General Retailer — Option 6: Dark Slate ──
                                    RegisterBannerCard(
                                        gradient    = listOf(Color(0xFF3D3B8E), Color(0xFF5752B8)),
                                        icon        = "🛒",
                                        title       = "Up to 10% Discount",
                                        subtitle    = "For general retailers",
                                        buttonLabel = "REGISTER NOW",
                                        onClick     = { navAction.navToSignUp("1") }
                                    )

                                    // ── Wholesaler — Option 6: Dark Navy Slate ──
                                    RegisterBannerCard(
                                        gradient    = listOf(Color(0xFF1C3A5E), Color(0xFF2E5C8A)),
                                        icon        = "🏪",
                                        title       = "Up to 14% Discount",
                                        subtitle    = "Shop owner / Wholesaler",
                                        buttonLabel = "JOIN AS SELLER",
                                        onClick     = { navAction.navToSignUp("2") }
                                    )

                                    // ── Special — Option 6: Dark Violet ──
                                    RegisterBannerCard(
                                        gradient    = listOf(Color(0xFF4B2882), Color(0xFF7040B8)),
                                        icon        = "⭐",
                                        title       = "Get Special Offer",
                                        subtitle    = "Exclusive member benefits",
                                        buttonLabel = "GET OFFER",
                                        onClick     = { isSpecialDialogue.value = true }
                                    )

                                    // ── WhatsApp — Option 6: Dark Forest Green ──
                                    RegisterBannerCard(
                                        gradient    = listOf(Color(0xFF1A4A3A), Color(0xFF2E7A5E)),
                                        icon        = "💬",
                                        title       = "Chat on WhatsApp",
                                        subtitle    = "Quick support & orders",
                                        buttonLabel = "CHAT NOW",
                                        onClick     = { viewModel.navToWhatsApp(context) }
                                    )

                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                            }
                        }

                        // ── Search Results ────────────────────────────────────────
                        if (state.searchedProduct.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Text(
                                    text       = "Results: ${state.searchedProduct.size} products",
                                    fontWeight = FontWeight.W500,
                                    color      = Primary,
                                    fontSize   = 14.sp,
                                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            // ✅ OPTIMIZATION: stable keys so Compose reuses existing
                            // item compositions instead of recreating on every cart change
                            items(
                                items = state.searchedProduct,
                                key   = { it.PID_PRODUCT ?: it.hashCode() },
                                span  = { GridItemSpan(2) }
                            ) { product ->
                                SearchedProductItem(
                                    item             = product,
                                    cartIds          = state.cartIds,
                                    cartInfo         = state.cartInfo,
                                    onTap            = { p -> viewModel.updateCurrentItem(p) },
                                    addToCart        = { p ->
                                        if (MainActivity.isLoggedIn.value) {
                                            if (MainActivity.isRestrict.intValue <= 0) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.addToCartSafe(p, 1, salesUnit, context)
                                            } else isRestrict.value = true
                                        } else navAction.navToLogin()
                                    },
                                    removeFromCart   = { p, unit ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.removeFromCart(p, unit, context = context)
                                    },
                                    increaseCartItem = { p, quantity, unit ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.addToCart(p, quantity, unit, context)
                                    },
                                    isBox            = state.isBox,
                                    addToCartLoading = state.addToCartLoading
                                )
                            }
                        }

                        // ── No results message ────────────────────────────────────
                        if (state.search.isNotEmpty() && state.searchedProduct.isEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Box(
                                    modifier         = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text     = "No products found for \"${state.search}\"",
                                        color    = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // ── Current selected category ─────────────────────────────
                        // shown only when user taps a category from the Categories tab
                        if (state.search.isEmpty() && state.currentCategoryIndex != -1) {
                            item(span = { GridItemSpan(2) }) {
                                Box(modifier = Modifier.padding(5.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .border(
                                                BorderStroke(1.dp, Primary),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .fillMaxWidth()
                                            .padding(horizontal = 5.dp)
                                    ) {
                                        Text(
                                            text       = state.allProduct[state.currentCategoryIndex].category ?: "",
                                            fontWeight = FontWeight.W500,
                                            color      = Primary,
                                            fontSize   = 18.sp,
                                            modifier   = Modifier.padding(5.dp)
                                        )
                                    }
                                }
                            }
                            items(
                                items = state.allProduct[state.currentCategoryIndex].data ?: emptyList(),
                                key   = { it.PID_PRODUCT ?: it.hashCode() }
                            ) { product ->
                                ProductItem(
                                    item             = product,
                                    cartIds          = state.cartIds,
                                    cartInfo         = state.cartInfo,
                                    onTap            = { p -> viewModel.updateCurrentItem(p) },
                                    addToCart        = { p ->
                                        if (MainActivity.isLoggedIn.value) {
                                            if (MainActivity.isRestrict.intValue <= 0) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.addToCartSafe(p, 1, salesUnit, context)
                                            } else isRestrict.value = true
                                        } else navAction.navToLogin()
                                    },
                                    removeFromCart   = { p, unit ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.removeFromCart(p, unit, context = context)
                                    },
                                    increaseCartItem = { p, quantity, unit ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.addToCart(p, quantity, unit, context)
                                    },
                                    height           = productHeight,
                                    width            = productWidth,
                                    isBox            = state.isBox,
                                    addToCartLoading = state.addToCartLoading
                                )
                            }
                            if (state.allProduct[state.currentCategoryIndex].data?.isEmpty() == true) {
                                item(span = { GridItemSpan(2) }) { NoContent() }
                            }
                        }

                        // ── All Products (no category headers) ────────────────────
                        if (state.search.isEmpty()) {
                            items(
                                items = flatProducts,
                                key   = { it.PID_PRODUCT ?: it.hashCode() }
                            ) { product ->
                                ProductItem(
                                    item             = product,
                                    cartIds          = state.cartIds,
                                    cartInfo         = state.cartInfo,
                                    onTap            = { p -> viewModel.updateCurrentItem(p) },
                                    addToCart        = { p ->
                                        if (MainActivity.isLoggedIn.value) {
                                            if (MainActivity.isRestrict.intValue <= 0) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.addToCartSafe(p, 1, salesUnit, context)
                                            } else isRestrict.value = true
                                        } else navAction.navToLogin()
                                    },
                                    removeFromCart   = { p, unit ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.removeFromCart(p, unit, context = context)
                                    },
                                    increaseCartItem = { p, quantity, unit ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.addToCart(p, quantity, unit, context)
                                    },
                                    height           = productHeight,
                                    width            = productWidth,
                                    isBox            = state.isBox,
                                    addToCartLoading = state.addToCartLoading
                                )
                            }
                        }
                    }
                }

                AppName()
            }

        } // end SwipeRefresh

        // ✅ Skeleton overlays everything — outside SwipeRefresh so it's always visible
        if (state.isLoading) HomeSkeletonScreen()

        // ✅ Scroll to Top FAB — appears after scrolling past 3 items
        AnimatedVisibility(
            visible  = showScrollToTop.value,
            enter    = fadeIn() + scaleIn(),
            exit     = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
        ) {
            FloatingActionButton(
                onClick        = { scope.launch { scrollSate.animateScrollToItem(0) } },
                containerColor = Primary,
                contentColor   = Color.White,
                modifier       = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector        = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Scroll to top"
                )
            }
        }
    } // end Box
}

// ─────────────────────────────────────────────────────────────────────────────
// Service Restricted Dialog
// Shown when the user's account is restricted (unpaid bill).
// Phone number is a tappable call button — opens system dialer.
// ─────────────────────────────────────────────────────────────────────────────
private const val RESTRICT_CONTACT = "8801906198740"

@Composable
private fun ServiceRestrictedDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Warning icon ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚠️", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Title ─────────────────────────────────────────────────
            Text(
                text       = "Service Restricted",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF1A1A2E),
                textAlign  = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Body message ──────────────────────────────────────────
            Text(
                text      = "Please pay your previous bill to continue ordering.",
                fontSize  = 13.sp,
                color     = Color(0xFF6E6B80),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ── Tappable call card ────────────────────────────────────
            // ACTION_DIAL opens the dialer pre-filled — user confirms
            // before calling. No permission needed in manifest.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                    .clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$RESTRICT_CONTACT")
                            }
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "📞", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text       = "Call Support",
                        fontSize   = 11.sp,
                        color      = Color(0xFF2E7D32),
                        fontWeight = FontWeight.W500
                    )
                    Text(
                        text       = "+$RESTRICT_CONTACT",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text      = "Tap the card above to call",
                fontSize  = 11.sp,
                color     = Color(0xFFADABB8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── OK button ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Primary, RoundedCornerShape(25.dp))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "OK, Got it",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Register Banner Card — polished gradient card replacing the old image-based
// Register composable. No functionality change; purely UI upgrade.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RegisterBannerCard(
    gradient    : List<Color>,
    icon        : String,
    title       : String,
    subtitle    : String,
    buttonLabel : String,
    onClick     : () -> Unit
) {
    Column(
        modifier = Modifier
            .width(155.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush = Brush.linearGradient(gradient))
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Large free-floating emoji — no wrapping box
        Text(
            text     = icon,
            fontSize = 26.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        // Bold title
        Text(
            text       = title,
            color      = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize   = 13.sp,
            maxLines   = 2,
            overflow   = TextOverflow.Ellipsis,
            lineHeight = 17.sp
        )
        // Muted subtitle
        Text(
            text     = subtitle,
            color    = Color.White.copy(alpha = 0.75f),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Pill CTA — white text, semi-transparent white background
        Text(
            text       = "→  $buttonLabel",
            color      = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize   = 10.sp,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
            modifier   = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(50.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Skeleton screen — shown during product load instead of a blank spinner.
// Mimics the real layout so the user perceives instant response.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HomeSkeletonScreen() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -600f,
        targetValue  = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEEEEEE),
            Color(0xFFF8F8F8),
            Color(0xFFEEEEEE)
        ),
        start = Offset(shimmerX, 0f),
        end   = Offset(shimmerX + 400f, 400f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // ── Banner skeleton ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(shimmerBrush)
        )

        // ── Category label skeleton ───────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(20.dp)
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(shimmerBrush)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Product card grid skeleton — 6 cards ──────────────────
        val cardWidth  = (LocalConfiguration.current.screenWidthDp.dp - 24.dp) / 2
        val cardHeight = cardWidth * 9 / 16

        repeat(3) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(2) {
                    Column(
                        modifier = Modifier
                            .width(cardWidth)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(
                                width = 0.5.dp,
                                color = Color(0xFFEEEEEE),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        // Image area
                        Box(
                            modifier = Modifier
                                .width(cardWidth)
                                .height(cardHeight)
                                .background(shimmerBrush)
                        )
                        // Text lines
                        Column(modifier = Modifier.padding(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(shimmerBrush)
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(shimmerBrush)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(shimmerBrush)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(54.dp)
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(shimmerBrush)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}