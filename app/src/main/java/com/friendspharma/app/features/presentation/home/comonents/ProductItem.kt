package com.friendspharma.app.features.presentation.home.comonents

import com.friendspharma.app.core.util.formatPercent

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.compose.ui.graphics.painter.ColorPainter
import com.friendspharma.app.MainActivity
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.core.theme.TealColor
import com.friendspharma.app.features.data.remote.model.CartInfoDto
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem
import kotlinx.coroutines.delay

// ── Colour palette ─────────────────────────────────────────────────────────────
private val DiscountRed    = Color(0xFFE53935)
private val SaveGreen      = Color(0xFF2E7D32)
private val OutOfStockGray = Color(0xFF9E9E9E)
private val PriceGray      = Color(0xFF757575)
private val ImageBg        = Color(0xFFF5F5F5)

@Composable
fun ProductItem(
    item: ProductsDtoItem,
    cartIds: Set<Int>,
    cartInfo: CartInfoDto,
    onTap: (ProductsDtoItem) -> Unit,
    addToCart: (ProductsDtoItem) -> Unit,
    removeFromCart: (ProductsDtoItem, String) -> Unit,
    increaseCartItem: (ProductsDtoItem, Int, String) -> Unit,
    height: Dp,
    width: Dp,
    isBox: Boolean,
    addToCartLoading: String
) {
    val context       = LocalContext.current
    val haptic        = LocalHapticFeedback.current

    val inStock = remember(isBox, item.STOCK_QTY_BOX, item.STOCK_QTY_LEAF) {
        (isBox && (item.STOCK_QTY_BOX ?: 0.0) >= 1.0) ||
                (!isBox && (item.STOCK_QTY_LEAF ?: 0.0) >= 1.0)
    }

    val isLoading        = item.PID_PRODUCT.toString() == addToCartLoading
    val discountPercent  = if (isBox) item.BOX_SALES_PER  else item.LEAF_SALES_PER
    val saveAmount       = if (isBox) item.BOX_OFFER_VALUE else item.LEAF_OFFER_VALUE
    val mrpPrice         = if (isBox) item.BOX_MRP_PRICE   else item.LEAF_MRP_PRICE
    val salesPrice       = if (isBox) item.BOX_SALES_PRICE else item.LEAF_SALES_PRICE
    val currentSalesUnit = if (isBox) "BOX" else "STRIP"

    val isInCartWithCurrentUnit = remember(cartInfo, item.PID_PRODUCT, isBox) {
        cartInfo.data?.any {
            it.PID_PRODUCT == item.PID_PRODUCT && it.SALES_UNIT == currentSalesUnit
        } == true
    }

    val imageRequest = remember(item.IMAGE_URL) {
        ImageRequest.Builder(context)
            .data(item.IMAGE_URL)
            .size(width.value.toInt() * 2, height.value.toInt() * 2)
            .memoryCacheKey(item.IMAGE_URL)
            .diskCacheKey(item.IMAGE_URL)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(200)
            .build()
    }

    val buttonColor by animateColorAsState(
        targetValue   = if (isLoading) Color.Gray else Primary,
        animationSpec = tween(durationMillis = 300),
        label         = "btn_${item.PID_PRODUCT}"
    )

    // Flash card border ONLY when stock drops to 0 while user is watching
    var prevInStock        by remember { mutableStateOf(inStock) }
    var justWentOutOfStock by remember { mutableStateOf(false) }
    LaunchedEffect(inStock) {
        if (prevInStock && !inStock) {
            justWentOutOfStock = true
            delay(2500)
            justWentOutOfStock = false
        }
        prevInStock = inStock
    }

    Card(
        modifier  = Modifier
            .padding(5.dp)
            .then(
                if (justWentOutOfStock)
                    Modifier.border(1.5.dp, DiscountRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                else Modifier
            )
            .clickable { onTap(item) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ╔══════════════════════════════════════════════════════════╗
            // ║  IMAGE AREA  — fixed height, all badges overlay inside   ║
            // ╚══════════════════════════════════════════════════════════╝
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)                              // ← fixed height, never grows
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(ImageBg)
            ) {
                // Product photo with shimmer placeholder + error fallback
                SubcomposeAsyncImage(
                    model              = imageRequest,
                    contentDescription = item.PRODUCT_NAME,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(height)
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            // ✅ Shimmer placeholder while image loads
                            val transition = rememberInfiniteTransition(label = "img_shimmer")
                            val shimmerX by transition.animateFloat(
                                initialValue   = -300f,
                                targetValue    = 300f,
                                animationSpec  = infiniteRepeatable(
                                    animation  = tween(900, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "shimmer_x"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(height)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFE0E0E0),
                                                Color(0xFFF5F5F5),
                                                Color(0xFFE0E0E0)
                                            ),
                                            start = Offset(shimmerX, 0f),
                                            end   = Offset(shimmerX + 300f, 0f)
                                        )
                                    )
                            )
                        }
                        is AsyncImagePainter.State.Error -> {
                            // ✅ Medicine icon fallback on error
                            Box(
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .height(height)
                                    .background(ImageBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "💊", fontSize = 36.sp)
                            }
                        }
                        else -> SubcomposeAsyncImageContent(
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // ── TOP-LEFT: Discount % pill ──────────────────────────
                if (discountPercent != null && discountPercent > 0.0) {
                    Text(
                        text       = "${discountPercent.formatPercent()}% OFF",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 9.sp,
                        modifier   = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .background(DiscountRed, RoundedCornerShape(5.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                // ── TOP-RIGHT: Save amount pill ────────────────────────
                if (saveAmount != null && saveAmount > 0.0) {
                    Text(
                        text       = "Save ${saveAmount.toInt()}৳",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 9.sp,
                        modifier   = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(SaveGreen, RoundedCornerShape(5.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                // Stock numbers are intentionally NOT shown to the customer.
                // Protection is handled silently in HomeViewModel.
            }
            // ╚══════════════ END IMAGE AREA ════════════════════════════╝

            // ╔══════════════════════════════════════════════════════════╗
            // ║  PRODUCT INFO                                            ║
            // ╚══════════════════════════════════════════════════════════╝
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(top = 7.dp, bottom = 8.dp)
            ) {

                // Product name
                Text(
                    text       = item.PRODUCT_NAME ?: "",
                    color      = Color(0xFF1A1A2E),
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 12.sp,
                    maxLines   = 2,
                    lineHeight = 16.sp,
                    overflow   = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Box · Leaf spec line
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (MainActivity.userType.value != "1" && item.BOX_SIZE_TITLE != null) {
                        Text(
                            text     = "Box: ${item.BOX_SIZE_TITLE}",
                            fontSize = 10.sp,
                            color    = PriceGray
                        )
                        Text(text = "  ·  ", fontSize = 10.sp, color = PriceGray)
                    }
                    Text(
                        text     = "Leaf: ${item.STRIP_QTY}",
                        fontSize = 10.sp,
                        color    = PriceGray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Price + cart button row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // ── Prices ─────────────────────────────────────────
                    Column(modifier = Modifier.weight(1f)) {
                        if (MainActivity.userType.value == "1") {
                            Text(
                                text       = "${mrpPrice}৳",
                                color      = TealColor,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp
                            )
                        } else {
                            Text(
                                text           = "${mrpPrice}৳",
                                textDecoration = TextDecoration.LineThrough,
                                color          = PriceGray,
                                fontSize       = 10.sp
                            )
                            Text(
                                text       = "${salesPrice}৳",
                                color      = TealColor,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp
                            )
                        }
                    }

                    // ── Cart controls ──────────────────────────────────
                    when {

                        // Already in cart → +/- stepper
                        isInCartWithCurrentUnit && inStock -> {
                            val unitCartInfo = remember(cartInfo, isBox) {
                                CartInfoDto(
                                    data    = cartInfo.data?.filter {
                                        it.SALES_UNIT == currentSalesUnit
                                    },
                                    message = cartInfo.message
                                )
                            }
                            IncreaseDecrease(
                                item             = item,
                                removeFromCart   = removeFromCart,
                                increaseCartItem = increaseCartItem,
                                cartInfo         = unitCartInfo,
                                addToCartLoading = addToCartLoading
                            )
                        }

                        // In stock → ADD button
                        inStock -> {
                            Box(
                                modifier = Modifier
                                    .height(32.dp)
                                    .width(66.dp)
                                    .background(buttonColor, RoundedCornerShape(8.dp))
                                    .clickable(enabled = !isLoading) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        addToCart(item)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(16.dp),
                                        color       = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(text="+",color=Color.White,fontWeight=FontWeight.Bold,fontSize=16.sp)
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text       = "ADD",
                                            color      = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize   = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Out of stock → muted label
                        else -> {
                            Box(
                                modifier = Modifier
                                    .height(32.dp)
                                    .width(66.dp)
                                    .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                                    .border(
                                        0.5.dp,
                                        OutOfStockGray.copy(alpha = 0.25f),
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = "Out of\nStock",
                                    color      = OutOfStockGray,
                                    fontWeight = FontWeight.W500,
                                    fontSize   = 9.sp,
                                    lineHeight = 12.sp,
                                    textAlign  = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            // ╚══════════════ END PRODUCT INFO ══════════════════════════╝
        }
    }
}