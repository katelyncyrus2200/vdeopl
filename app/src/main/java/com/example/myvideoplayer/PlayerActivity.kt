package com.example.myvideoplayer

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : ComponentActivity() {

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // Fullscreen immersive like your rental app
        window.decorView.systemUiVisibility = 0x1307

        val uri: Uri = intent.data ?: run {
            finish()
            return
        }

        val playerView = findViewById<PlayerView>(R.id.playerView)

        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(15_000)    // 15 seconds back
            .setSeekForwardIncrementMs(15_000) // 15 seconds forward
            .build()
            .also { exo ->
                playerView.player = exo
                exo.setMediaItem(MediaItem.fromUri(uri))
                exo.prepare()
                exo.playWhenReady = true

                // Always show controls
                playerView.useController = true
                playerView.controllerAutoShow = true
                playerView.controllerShowTimeoutMs = 0
                playerView.showController()
            }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = 0x1307
            findViewById<PlayerView>(R.id.playerView).showController()
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}
