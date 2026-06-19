package com.friendspharma.app.features.presentation.home.comonents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun Register(image: Int, modifier: Modifier) {
    Image(
        painter = painterResource(id = image),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = modifier.width(150.dp).clip(shape = RoundedCornerShape(10.dp))
    )

}