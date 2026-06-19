package com.friendspharma.app.features.domain.services

import android.content.Context
import android.content.Intent

object Share {
    fun shareData(context: Context, title: String, screen: String, id: String) {
        val url = ""
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_TEXT, url)
        intent.setType("text/plain")
        intent.setAction(Intent.ACTION_SEND)
        context.startActivity(Intent.createChooser(intent, ""))
    }
}