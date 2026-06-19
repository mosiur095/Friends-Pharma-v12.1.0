package com.friendspharma.app.features.presentation.return_products.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.friendspharma.app.R
import com.friendspharma.app.core.theme.DeepGreen
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.core.theme.TealColor
import com.friendspharma.app.features.data.remote.model.ReturnCartInfoDto
import com.friendspharma.app.features.data.remote.model.ReturnProductDtoItem
import com.friendspharma.app.features.presentation.home.comonents.CartButton

@Composable
fun ReturnProductItem(
    item: ReturnProductDtoItem,
    cartIds: Set<Int>,
    cartInfo: ReturnCartInfoDto,
    onTap: (ReturnProductDtoItem) -> Unit,
    addToCart: (ReturnProductDtoItem) -> Unit,
    removeFromCart: (ReturnProductDtoItem) -> Unit,
    increaseCartItem: (ReturnProductDtoItem) -> Unit,
    height: Dp,
    width: Dp,
    addToCartLoading: String
) {
    Card(
        modifier = Modifier
            .padding(5.dp)
            .clickable { onTap(item) },
        elevation = CardDefaults.cardElevation(5.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                AsyncImage(
                    model = item.IMAGE_URL,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .height(height)
                        .width(width)
                )
            }
            Column(modifier = Modifier.padding(5.dp)) {
                Row {
                    Text(
                        text = item.PRODUCT_NAME ?: "",
                        color = Color.Black,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Quantity: ${item.QUANTITY} " + item.SALES_TYPE,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Total Price: ${item.TOTAL_PRICE} ৳",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.MRP_PRICE.toString() + "৳",
                            color = TealColor,
                            fontWeight = FontWeight.W500
                        )
                        Text(
                            text = item.SALES_PRICE.toString() + "৳",
                            textDecoration = TextDecoration.LineThrough,
                            color = DeepGreen,
                            fontWeight = FontWeight.W500
                        )
                    }
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
        }
    }
}