package com.friendspharma.app.features.presentation.return_products.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.friendspharma.app.R
import com.friendspharma.app.core.theme.BackGroundDark
import com.friendspharma.app.core.theme.DeepGreen
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.core.theme.TealColor
import com.friendspharma.app.features.data.remote.model.ReturnCartInfoDto
import com.friendspharma.app.features.data.remote.model.ReturnProductDtoItem
import com.friendspharma.app.features.presentation.home.comonents.CartButton

@Composable
fun ReturnProductDetails(
    item: ReturnProductDtoItem,
    cartIds: Set<Int> = HashSet(),
    cartInfo: ReturnCartInfoDto,
    addToCart: (ReturnProductDtoItem) -> Unit,
    removeFromCart: (ReturnProductDtoItem) -> Unit,
    increaseCartItem: (ReturnProductDtoItem) -> Unit,
    addToCartLoading: String,
    onDismiss: () -> Unit
) {
    val width = LocalConfiguration.current.screenWidthDp.dp

    Dialog(onDismissRequest = { onDismiss() }) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(Modifier.padding(13.dp)) {
                Column(
                    modifier = Modifier
                        .background(Color.White, shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        AsyncImage(
                            model = item.IMAGE_URL,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(0.dp, width / 2)
                                .clip(RoundedCornerShape(15.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = item.PRODUCT_NAME ?: "",
                        color = Color.Black,
                        fontWeight = FontWeight.W500
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Quantity: ${item.QUANTITY} " + item.SALES_TYPE,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "MRP Price: " + item.MRP_PRICE.toString() + "৳",
                        color = TealColor,
                        fontWeight = FontWeight.W500
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Sales Price: " + item.SALES_PRICE.toString() + "৳",
                        textDecoration = TextDecoration.LineThrough,
                        color = DeepGreen,
                        fontWeight = FontWeight.W500
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Total Price: ${item.TOTAL_PRICE} ৳",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Expire date: ${item.EXPIRY_DATE?.substring(0, 10)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    if (cartIds.contains(item.PID_PRODUCT))
                        CartButton(cartItemQuantity = getQuantity(item.PID_PRODUCT, cartInfo))
                    else
                        Row {
                            Box(
                                modifier = Modifier
                                    .clickable { addToCart(item) }
                                    .size(30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.PID_PRODUCT.toString() == addToCartLoading)
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(5.dp),
                                        color = Primary,
                                        strokeWidth = 3.dp
                                    )
                                else
                                    Image(
                                        painter = painterResource(R.drawable.add),
                                        contentDescription = null,
                                        modifier = Modifier.size(30.dp)
                                    )
                            }
                        }
                }
            }
            Box(
                Modifier
                    .background(BackGroundDark, CircleShape)
                    .clickable { onDismiss() }
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    Modifier.padding(5.dp)
                )
            }
        }
    }
}