package com.friendspharma.app.core.components

import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.friendspharma.app.core.theme.BackGroundColor

@Composable
fun PDFViewer(url: String, paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BackGroundColor).padding(paddingValues)
    ) {
        println(url)
        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.builtInZoomControls = true
                settings.javaScriptEnabled = true
                settings.setSupportZoom(true)
                settings.userAgentString
                settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK;
                webChromeClient = WebChromeClient()
                //webViewClient = object : WebChromeClient() {}

                loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=$url")
            }
        }, update = {

        })

    }
}