package com.friendspharma.app.features.presentation.order_details.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.friendspharma.app.R
import com.friendspharma.app.core.theme.TextFieldBackGround

@Composable
fun CourierOptionsDialog(
    title: String,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
    types: List<CourierModel> = listOf<CourierModel>(
        CourierModel("Pathao", R.drawable.pathao_logo),
        CourierModel("SteadFast", R.drawable.stead_fast_logo)
    )
) {

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Companion.White, shape = RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Companion.W500
            )
            Spacer(modifier = Modifier.height(20.dp))

            repeat(types.size) { index ->
                val type = types[index]
                Card(modifier = Modifier
                    .clickable {
                        onSelected(type.type)
                    }
                    .fillMaxWidth(),
                    colors = CardDefaults.cardColors(TextFieldBackGround)) {
                    Row {
                        Image(
                            painter = painterResource(type.icon), contentDescription = null,
                            modifier = Modifier
                                .height(50.dp).width(125.dp).padding(5.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = type.type, modifier = Modifier.padding(15.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

data class CourierModel(val type: String, val icon: Int)