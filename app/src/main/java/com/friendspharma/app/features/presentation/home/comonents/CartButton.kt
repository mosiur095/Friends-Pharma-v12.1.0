package com.friendspharma.app.features.presentation.home.comonents

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.friendspharma.app.core.theme.Primary

@Composable
fun CartButton(
    cartItemQuantity: Int,
    navToCart: (() -> Unit)? = null
) {
    var prevQuantity by remember { mutableIntStateOf(cartItemQuantity) }
    var targetScale by remember { mutableFloatStateOf(1f) }

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "cart_badge_scale"
    )

    LaunchedEffect(cartItemQuantity) {
        if (cartItemQuantity != prevQuantity) {
            targetScale = 1.4f
            kotlinx.coroutines.delay(80)
            targetScale = 1f
            prevQuantity = cartItemQuantity
        }
    }

    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .clickable { navToCart?.invoke() }
    ) {
        Icon(
            imageVector = Icons.Outlined.ShoppingCart,
            contentDescription = "Cart",
            modifier = Modifier.size(24.dp),
            tint = Primary,
        )

        if (cartItemQuantity > 0) {
            Box(
                modifier = Modifier
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .background(Color.Red, CircleShape)
                    .padding(2.dp)
                    .defaultMinSize(12.dp, 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cartItemQuantity.toString(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
    }
}