package com.friendspharma.app.features.presentation.profile.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.friendspharma.app.core.theme.Gray
import com.friendspharma.app.core.theme.GrayLight

@Composable
fun UserDataItem(title: String, data: String){
    Text(text = title, color = GrayLight)
    Text(
        text = data,
        color = Gray,
        fontSize = 20.sp
    )
    Spacer(modifier = Modifier.height(15.dp))
}