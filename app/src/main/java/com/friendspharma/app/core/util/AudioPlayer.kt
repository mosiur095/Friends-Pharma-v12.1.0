package com.friendspharma.app.core.util

import android.media.MediaPlayer
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

private val mediaPlayer: MediaPlayer = MediaPlayer()
class AudioPlayer @Inject constructor() {
    private var currentUrl: String = ""
    private var timer = Timer()

    fun play(url: String, onFinished: (String) -> Unit, percentage: ((Double) -> Unit?)? = null) {

        if (currentUrl == url && mediaPlayer.isPlaying) {
            pause()
            onFinished("")
        } else if (currentUrl == url && !mediaPlayer.isPlaying) {
            resume()
        } else {
            timer.cancel()
            timer = Timer()

            currentUrl = url
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener {
                    currentUrl = ""
                    onFinished("")
                    timer.cancel()
                }

                try {
                    timer.scheduleAtFixedRate(object : TimerTask() {
                        override fun run() {
                            if (percentage != null && mediaPlayer.isPlaying) {
                                val percent =
                                    ((mediaPlayer.currentPosition.toDouble() / mediaPlayer.duration.toDouble()) * 100)
                                if (percent > 0) {
                                    percentage(percent)
                                }
                            }
                        }
                    }, 0, 1000)
                }catch (e: Exception){
                    println(e.message)
                }
            }

        }
    }

    private fun pause() {
        mediaPlayer.pause()
    }

    private fun resume() {
        mediaPlayer.start()
    }

    fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    fun close(){
        mediaPlayer.release()
    }
}