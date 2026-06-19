package com.friendspharma.app.features.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.friendspharma.app.core.theme.Gray
import com.friendspharma.app.core.theme.Primary

@Composable
fun ActionItem(modifier: Modifier, text: Int, icon: ImageVector, tint: Color = Color.White){
    Card(
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(5.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Icon(
                icon,
                tint = tint,
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .background(Primary, shape = CircleShape)
                    .padding(5.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = stringResource(id = text),
                color = Gray,
                fontWeight = FontWeight.W600
            )
        }
    }
}