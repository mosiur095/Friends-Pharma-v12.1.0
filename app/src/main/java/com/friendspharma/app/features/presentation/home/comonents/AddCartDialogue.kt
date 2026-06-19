package com.friendspharma.app.features.presentation.home.comonents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.friendspharma.app.MainActivity
import com.friendspharma.app.R
import com.friendspharma.app.core.components.TextFieldK
import com.friendspharma.app.core.theme.BackGroundDark
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.core.util.Common
import com.friendspharma.app.features.data.remote.model.CartInfoDto
import com.friendspharma.app.features.data.remote.model.ProductsDtoItem

@Composable
fun AddCartDialogue(
    isBox: Boolean,
    item: ProductsDtoItem,
    cartInfo: CartInfoDto,
    addToCart: (ProductsDtoItem, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = FocusRequester()
    val quantity = remember {
        mutableIntStateOf(getQuantity(item.PID_PRODUCT, cartInfo))
    }

    val selectedType = remember { mutableStateOf(if (isBox) "BOX" else "STRIP") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(20.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(modifier = Modifier.padding(10.dp)) {
                if (MainActivity.userType.value != "1")
                    Text(text = "Box Size: " + item.BOX_SIZE_TITLE + ",", fontWeight = FontWeight.Bold)
                if (MainActivity.userType.value != "1")
                    Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Leaf Pieces: " + item.STRIP_QTY, fontWeight = FontWeight.Bold)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),

                shape = RoundedCornerShape(8)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondary),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (MainActivity.userType.value != "1")
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    if (selectedType.value == "BOX")
                                        BackGroundDark else Color.Transparent,
                                contentColor =
                                    if (selectedType.value == "BOX") Primary else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        selectedType.value = "BOX"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "BOX",
                                    style = LocalTextStyle.current.copy(
                                        fontSize = 14.sp,
                                        fontWeight =
                                            LocalTextStyle.current.fontWeight,
                                        color = if (selectedType.value == "BOX")
                                            MaterialTheme.colorScheme.scrim else Color.White
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor =
                                if (selectedType.value == "STRIP")
                                    BackGroundDark else Color.Transparent,
                            contentColor =
                                if (selectedType.value == "STRIP") Primary else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    selectedType.value = "STRIP"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "STRIP",
                                style = LocalTextStyle.current.copy(
                                    fontSize = 14.sp,
                                    fontWeight =
                                        LocalTextStyle.current.fontWeight,
                                    color = if (selectedType.value == "STRIP")
                                        MaterialTheme.colorScheme.scrim else Color.White
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(
                            BackGroundDark,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .size(36.dp)
                        .clickable {
                            if (quantity.intValue > 1)
                                quantity.intValue--
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "-", fontSize = 30.sp, fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    TextFieldK(
                        value = if (quantity.intValue > 0) quantity.intValue.toString() else "",
                        label = R.string.quantity,
                        onValueChange = {
                            try {
                                if (Common.isNumeric(it)) {
                                    quantity.intValue = if (it.isNotEmpty()) it.toInt() else 0
                                } else if (it.isEmpty()) {
                                    quantity.intValue = 0
                                }
                            } catch (e: Exception) {
                                println(e.message)
                            }

                        },
                        focusRequester = focusRequester,
                        keyboardType = KeyboardType.Number
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .background(
                            BackGroundDark,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .size(36.dp)
                        .clickable { quantity.intValue++ },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(36.dp)
                        .width(100.dp)
                        .background(color = Primary, shape = RoundedCornerShape(25.dp))
                        .clickable {
                            onDismiss()
                        }
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        color = Color.White,
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(36.dp)
                        .width(100.dp)
                        .background(color = Color(0xFFDB5A3C), shape = RoundedCornerShape(25.dp))
                        .clickable {
                            if (quantity.intValue > 0) {
                                addToCart(
                                    item,
                                    quantity.intValue,
                                    selectedType.value
                                )
                            }
                        }
                ) {
                    Text(
                        text = stringResource(id = R.string.apply),
                        color = Color.White,
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

fun getQuantity(id: Int?, cartInfo: CartInfoDto): Int {
    for (item in cartInfo.data ?: emptyList()) {
        if (item.PID_PRODUCT == id) {
            return item.QUANTITY?.toInt() ?: 1
        }
    }
    return 1
}
