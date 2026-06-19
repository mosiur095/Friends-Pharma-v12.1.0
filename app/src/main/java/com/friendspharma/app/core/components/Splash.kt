package com.friendspharma.app.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.friendspharma.app.R
import com.friendspharma.app.core.theme.BackGroundColor
import com.friendspharma.app.core.theme.Primary

@Composable
fun Splash(isSplash: Boolean) {

    AnimatedVisibility(
        visible = isSplash,
        enter = fadeIn(animationSpec = tween(1000)),
        exit = fadeOut(animationSpec = tween(1000))
    ) {
        Box(
            modifier = Modifier
                .background(color = Color.White)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .background(color = BackGroundColor)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_small),
                        contentDescription = null,
                        modifier = Modifier.size(130.dp)
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(text = buildAnnotatedString {
                        withStyle(style = SpanStyle(Color.Black, fontSize = 24.sp)) {
                            append("мυѕℓιм")
                        }
                        withStyle(style = SpanStyle(Primary, fontSize = 24.sp)) {
                            append("в")
                        }
                        withStyle(style = SpanStyle(Color.Red, fontSize = 24.sp)) {
                            append("ᴅ")
                        }
                    })

                }
            }
        }
    }

}