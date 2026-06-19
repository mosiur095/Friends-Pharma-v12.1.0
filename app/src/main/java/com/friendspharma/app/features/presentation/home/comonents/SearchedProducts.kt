package com.friendspharma.app.features.presentation.home.comonents

import com.friendspharma.app.core.util.formatPercent

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.friendspharma.app.MainActivity
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.core.theme.TealColor
import com.friendspharma.app.features.data.remote.model.CartInfoDto
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem

private val DiscountRed    = Color(0xFFE53935)
private val SaveGreen      = Color(0xFF27AE60)
private val OutOfStockGray = Color(0xFFBDBDBD)
private val PriceGray      = Color(0xFFADABB8)
private val DividerColor   = Color(0xFFF0EEF8)
private val PurpleCompany  = Color(0xFFAB8EE0)

@Composable
fun SearchedProductItem(
    item: ProductsDtoItem,
    cartIds: Set<Int>,
    cartInfo: CartInfoDto,
    onTap: (ProductsDtoItem) -> Unit,
    addToCart: (ProductsDtoItem) -> Unit,
    removeFromCart: (ProductsDtoItem, String) -> Unit,
    increaseCartItem: (ProductsDtoItem, Int, String) -> Unit,
    isBox: Boolean,
    addToCartLoading: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) {
    val context      = LocalContext.current
    val currentSalesUnit = if (isBox) "BOX" else "STRIP"

    val inStock = remember(isBox, item.STOCK_QTY_BOX, item.STOCK_QTY_LEAF) {
        (isBox && (item.STOCK_QTY_BOX ?: 0.0) >= 1.0) ||
                (!isBox && (item.STOCK_QTY_LEAF ?: 0.0) > 0.0)
    }

    val isLoading       = item.PID_PRODUCT.toString() == addToCartLoading
    val discountPercent = if (isBox) item.BOX_SALES_PER else item.LEAF_SALES_PER
    val mrpPrice        = if (isBox) item.BOX_MRP_PRICE else item.LEAF_MRP_PRICE
    val salesPrice      = if (isBox) item.BOX_SALES_PRICE else item.LEAF_SALES_PRICE
    val saveAmount      = if (isBox) item.BOX_OFFER_VALUE else item.LEAF_OFFER_VALUE

    val isInCartWithCurrentUnit = remember(cartInfo, item.PID_PRODUCT, isBox) {
        cartInfo.data?.any {
            it.PID_PRODUCT == item.PID_PRODUCT && it.SALES_UNIT == currentSalesUnit
        } == true
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onTap(item) }
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),  // ✅ increased vertical padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Product image ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8F7FC))
            ) {
                // ✅ Optimized: size-constrained + disk/memory cached
                val imageRequest = remember(item.IMAGE_URL) {
                    ImageRequest.Builder(context)
                        .data(item.IMAGE_URL)
                        .size(128, 128)
                        .memoryCacheKey(item.IMAGE_URL)
                        .diskCacheKey(item.IMAGE_URL)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .crossfade(200)
                        .build()
                }
                SubcomposeAsyncImage(
                    model              = imageRequest,
                    contentDescription = null,
                    modifier           = Modifier.size(64.dp)
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            // Simple color placeholder — no animation needed for small 64dp image
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0xFFE8E8E8))
                            )
                        }
                        is AsyncImagePainter.State.Error -> {
                            Box(
                                modifier         = Modifier.size(64.dp).background(Color(0xFFF0F0F0)),
                                contentAlignment = Alignment.Center
                            ) { Text("💊", fontSize = 24.sp) }
                        }
                        else -> SubcomposeAsyncImageContent(contentScale = ContentScale.Crop)
                    }
                }
                if (discountPercent != null && discountPercent > 0.0) {
                    Text(
                        text       = "${discountPercent.formatPercent()}%",
                        color      = Color.White,
                        fontSize   = 8.sp,                        // ✅ increased from 7sp
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier
                            .align(Alignment.TopStart)
                            .background(
                                DiscountRed,
                                RoundedCornerShape(topStart = 10.dp, bottomEnd = 6.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // ── Product info ──────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = item.PRODUCT_NAME ?: "—",
                    fontSize   = 14.sp,                           // ✅ increased from 12sp
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFF2C2C3E),
                    maxLines   = 2,                               // ✅ allow 2 lines
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(2.dp))
                if (!item.COMPANY_NAME.isNullOrEmpty()) {
                    Text(
                        text     = item.COMPANY_NAME,
                        fontSize = 11.sp,                         // ✅ increased from 9sp
                        color    = PurpleCompany,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = buildString {
                        if (MainActivity.userType.value != "1" && item.BOX_SIZE_TITLE != null) {
                            append("Box: ${item.BOX_SIZE_TITLE}  ·  ")
                        }
                        append("Leaf: ${item.STRIP_QTY}")
                    },
                    fontSize = 11.sp,                             // ✅ increased from 9sp
                    color    = PriceGray
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (MainActivity.userType.value != "1" && mrpPrice != null) {
                        Text(
                            text           = "${mrpPrice}৳",
                            fontSize       = 11.sp,               // ✅ increased from 9sp
                            color          = PriceGray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    Text(
                        text       = "${if (MainActivity.userType.value == "1") mrpPrice else salesPrice}৳",
                        fontSize   = 15.sp,                       // ✅ increased from 13sp
                        fontWeight = FontWeight.Bold,
                        color      = TealColor
                    )
                    if (saveAmount != null && saveAmount > 0.0) {
                        Text(
                            text       = "Save ${saveAmount.toInt()}৳",
                            fontSize   = 10.sp,                   // ✅ increased from 8sp
                            color      = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier   = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SaveGreen)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            // ── Cart controls ─────────────────────────────────────────
            Box(modifier = Modifier.widthIn(min = 80.dp), contentAlignment = Alignment.Center) {
                when {
                    isInCartWithCurrentUnit && inStock -> {
                        val unitCartInfo = remember(cartInfo, isBox) {
                            CartInfoDto(
                                data    = cartInfo.data?.filter { it.SALES_UNIT == currentSalesUnit },
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
                    inStock -> {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isLoading) Color.Gray else Primary)
                                .clickable(enabled = !isLoading) { addToCart(item) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),  // ✅ slightly larger tap area
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(16.dp),
                                    color       = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(text="+",color=Color.White,fontWeight=FontWeight.Bold,fontSize=16.sp)
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text       = "ADD",
                                    color      = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 13.sp                // ✅ increased from 12sp
                                )
                            }
                        }
                    }
                    else -> {
                        Text(
                            text       = "Out of\nStock",
                            color      = OutOfStockGray,
                            fontSize   = 11.sp,                       // ✅ increased from 9sp
                            fontWeight = FontWeight.W500,
                            modifier   = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF5F5F5))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color     = DividerColor,
                thickness = 0.5.dp,
                modifier  = Modifier.padding(start = 88.dp)
            )
        }
    }
}