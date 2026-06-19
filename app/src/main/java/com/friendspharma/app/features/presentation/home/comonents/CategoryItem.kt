package com.friendspharma.app.features.presentation.home.comonents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.friendspharma.app.core.theme.BackGroundDark
import com.friendspharma.app.core.theme.Primary
import com.friendspharma.app.features.data.remote.model.AllCategoryDtoItem

@Composable
fun CategoryItem(category: AllCategoryDtoItem, isSelected: Boolean, categorySelected: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(if (isSelected) BackGroundDark else Color.White),
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .clickable {
                categorySelected()
            },
        elevation = CardDefaults.cardElevation(5.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .width(110.dp)
                .height(80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = category?.IMAGE_URL ?: "",
                contentDescription = null,
                modifier = Modifier
                    .height(60.dp)
                    .width(110.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp
                        )
                    ),
                contentScale = ContentScale.FillBounds
            )

            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = category?.CATEGORY_NAME ?: "", color = Primary,
                fontSize = 14.sp, fontWeight = FontWeight.W500,
                maxLines = 1,
                modifier = Modifier.padding(2.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}