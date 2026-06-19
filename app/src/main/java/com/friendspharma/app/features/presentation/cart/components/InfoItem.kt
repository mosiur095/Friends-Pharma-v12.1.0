package com.friendspharma.app.features.presentation.cart.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.friendspharma.app.core.theme.Primary

@Composable
fun InfoItem (text: String, amount: String){
    Row(modifier = Modifier.padding(vertical = 5.dp)) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = Primary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = amount,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
    }
}