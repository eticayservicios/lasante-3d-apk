package com.lasante.tvkiosk.media

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class Media3AudioPlayer : AudioPlayer {
    private var player: ExoPlayer? = null

    fun attach(context: Context) {
        if (player != null) return
        player = ExoPlayer.Builder(context).build()
    }

    override fun play(url: String) {
        val currentPlayer = player ?: return
        currentPlayer.setMediaItem(MediaItem.fromUri(url))
        currentPlayer.prepare()
        currentPlayer.playWhenReady = true
    }

    override fun stop() {
        player?.stop()
        player?.clearMediaItems()
    }

    fun release() {
        player?.release()
        player = null
    }
}
