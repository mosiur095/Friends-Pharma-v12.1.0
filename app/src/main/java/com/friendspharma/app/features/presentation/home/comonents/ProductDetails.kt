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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.friendspharma.app.MainActivity
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.core.theme.TealColor
import com.friendspharma.app.features.data.remote.model.CartInfoDto
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem

private val Purple        = Color(0xFF6B4FBB)
private val PurpleLight   = Color(0xFFEDE9FB)
private val SurfaceBg     = Color(0xFFF8F7FC)
private val DividerColor  = Color(0xFFEEEBF5)
private val TextPrimary   = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF6E6B80)
private val TextTertiary  = Color(0xFFADABB8)
private val GreenTeal     = Color(0xFF1D9E75)
private val GreenBg       = Color(0xFFE8F8F3)
private val RedDiscount   = Color(0xFFE53935)
private val SaveGreen     = Color(0xFF27AE60)

@Composable
fun ProductDetails(
    item: ProductsDtoItem,
    cartIds: Set<Int> = HashSet(),
    cartInfo: CartInfoDto,
    isBox: Boolean,
    addToCart: (ProductsDtoItem) -> Unit,
    removeFromCart: (ProductsDtoItem, String) -> Unit,
    increaseCartItem: (ProductsDtoItem, Int, String) -> Unit,
    addToCartLoading: String,
    onDismiss: () -> Unit
) {
    val context   = LocalContext.current
    val width     = LocalConfiguration.current.screenWidthDp.dp
    val inStock   = (isBox && (item.STOCK_QTY_BOX ?: 0.0) >= 1.0) ||
            (!isBox && (item.STOCK_QTY_LEAF ?: 0.0) >= 1.0)
    val isLoading = item.PID_PRODUCT.toString() == addToCartLoading
    val currentSalesUnit = if (isBox) "BOX" else "STRIP"
    // ✅ In-cart check must be unit-aware. A product added as LEAF must NOT show as
    // "already added" when the BOX toggle is selected (and vice versa) — matching the
    // list item, which checks PID_PRODUCT + SALES_UNIT.
    val isInCartWithCurrentUnit = cartInfo.data?.any {
        it.PID_PRODUCT == item.PID_PRODUCT && it.SALES_UNIT == currentSalesUnit
    } == true
    // Optimistically treat as in-cart while an add is in-flight so the stepper
    // appears immediately without waiting for getCartInfo()
    val isInCartOptimistic = isInCartWithCurrentUnit || isLoading
    val stockQty  = if (isBox) item.STOCK_QTY_BOX?.toInt() else item.STOCK_QTY_LEAF?.toInt()

    // ✅ OPTIMIZATION: build image request once — reuses from Coil cache
    // (previously used raw string URL with no cache config)
    val imageRequest = remember(item.IMAGE_URL) {
        ImageRequest.Builder(context)
            .data(item.IMAGE_URL)
            .size(width.value.toInt() * 2, (width.value * 0.5f).toInt() * 2)
            .memoryCacheKey(item.IMAGE_URL)
            .diskCacheKey(item.IMAGE_URL)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(200)
            .build()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier         = Modifier.fillMaxWidth(0.92f),
            contentAlignment = Alignment.TopEnd
        ) {
            // ── Main card ─────────────────────────────────────────────
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Product image ─────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = width * 0.5f)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(SurfaceBg)
                ) {
                    AsyncImage(
                        model              = imageRequest,
                        contentDescription = null,
                        contentScale       = ContentScale.Fit,
                        modifier           = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp, max = width * 0.5f)
                    )

                    // Discount badge
                    val discount = if (isBox) item.BOX_SALES_PER else item.LEAF_SALES_PER
                    if (discount != null && discount > 0.0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(RedDiscount)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text       = "${discount.formatPercent()}% OFF",
                                color      = Color.White,
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Stock badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (inStock) GreenBg else Color(0xFFFFEBEE))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text       = if (inStock) "In Stock ($stockQty)" else "Out of Stock",
                            color      = if (inStock) GreenTeal else RedDiscount,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Product name & info ───────────────────────────────
                Column(
                    modifier            = Modifier.padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = item.PRODUCT_NAME ?: "",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                        textAlign  = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text      = item.COMPANY_NAME ?: "",
                        fontSize  = 12.sp,
                        color     = Purple,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        if (MainActivity.userType.value != "1" && item.BOX_SIZE_TITLE != null) {
                            SpecChip(label = "Box: ${item.BOX_SIZE_TITLE}")
                            Spacer(Modifier.width(8.dp))
                        }
                        SpecChip(label = "Leaf: ${item.STRIP_QTY}")
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                Spacer(Modifier.height(16.dp))

                // ── Price cards ───────────────────────────────────────
                Row(
                    modifier              = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ✅ Show only the price card for the currently selected unit:
                    // Box toggle → Box card only, Leaf toggle → Leaf card only — never both.
                    // (Retail users have isBox = false, so they always see Leaf only.)
                    if (isBox) {
                        PriceCard(
                            label      = "Box",
                            mrp        = item.BOX_MRP_PRICE,
                            salesPer   = item.BOX_SALES_PER,
                            salesPrice = item.BOX_SALES_PRICE,
                            save       = item.BOX_OFFER_VALUE,
                            modifier   = Modifier.weight(1f)
                        )
                    } else {
                        PriceCard(
                            label      = "Leaf",
                            mrp        = item.LEAF_MRP_PRICE,
                            salesPer   = item.LEAF_SALES_PER,
                            salesPrice = item.LEAF_SALES_PRICE,
                            save       = item.LEAF_OFFER_VALUE,
                            modifier   = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── ADD / IncreaseDecrease button ─────────────────────
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    if (isInCartOptimistic && inStock) {
                        // Stepper must operate on the current unit's cart row only.
                        val unitCartInfo = CartInfoDto(
                            data    = cartInfo.data?.filter { it.SALES_UNIT == currentSalesUnit },
                            message = cartInfo.message
                        )
                        IncreaseDecrease(
                            item             = item,
                            removeFromCart   = removeFromCart,
                            increaseCartItem = increaseCartItem,
                            cartInfo         = unitCartInfo,
                            addToCartLoading = addToCartLoading
                        )
                    } else if (inStock) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isLoading) Color.Gray else Primary)
                                .clickable(enabled = !isLoading) { addToCart(item) }
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(18.dp),
                                    color       = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(text="+",color=Color.White,fontWeight=FontWeight.Bold,fontSize=20.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text       = "ADD TO CART",
                                    color      = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 15.sp
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier         = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF5F5F5))
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = "Out of Stock",
                                color      = TextTertiary,
                                fontWeight = FontWeight.Medium,
                                fontSize   = 14.sp
                            )
                        }
                    }
                }
            }

            // ── Close button ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(text="✕",color=TextPrimary,fontWeight=FontWeight.Bold,fontSize=16.sp)
            }
        }
    }
}

// ── Spec chip ─────────────────────────────────────────────────────────────────
@Composable
private fun SpecChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceBg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
    }
}

// ── Price card ────────────────────────────────────────────────────────────────
@Composable
private fun PriceCard(
    label: String,
    mrp: Double?,
    salesPer: Double?,
    salesPrice: Double?,
    save: Double?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceBg)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(PurpleLight)
                .padding(horizontal = 12.dp, vertical = 3.dp)
        ) {
            Text(
                text       = label,
                color      = Purple,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 12.sp
            )
        }

        Spacer(Modifier.height(4.dp))

        if (mrp != null) {
            Text(
                text           = "MRP ${mrp}৳",
                fontSize       = 11.sp,
                color          = TextTertiary,
                textDecoration = TextDecoration.LineThrough
            )
        }
        if (salesPer != null) {
            Text(
                text       = "${salesPer.formatPercent()}% OFF",
                fontSize   = 12.sp,
                color      = RedDiscount,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (salesPrice != null) {
            Text(
                text       = "${salesPrice}৳",
                fontSize   = 16.sp,
                color      = TealColor,
                fontWeight = FontWeight.Bold
            )
        }
        if (save != null && save > 0.0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(SaveGreen)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text       = "Save ${save.toInt()}৳",
                    fontSize   = 10.sp,
                    color      = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}