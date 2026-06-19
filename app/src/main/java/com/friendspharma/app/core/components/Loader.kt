package com.friendspharma.app.core.components

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import com.friendspharma.app.R

@Composable
fun Loader(paddingValues: PaddingValues) {

    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            }
        }
        .build()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = rememberAsyncImagePainter(
                model = R.drawable.loader,
                imageLoader = imageLoader
            ),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )
//        CircularProgressIndicator(
//            modifier = Modifier
//                .height(40.dp)
//                .align(Alignment.Center)
//                .testTag("loadingWheel")
//                .padding(paddingValues)
//        )
    }
}