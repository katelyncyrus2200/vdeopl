package com.example.myvideoplayer

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : ComponentActivity() {

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // VideoRental-like immersive flags
        window.decorView.systemUiVisibility = 0x1307

        val uri: Uri = intent.data ?: run {
            finish()
            return
        }

        val playerView = findViewById<PlayerView>(R.id.playerView)

        player = ExoPlayer.Builder(this).build().also { exo ->
            // Make default controller behave like your rental player (5s seek)
            exo.setSeekBackIncrementMs(5000)
            exo.setSeekForwardIncrementMs(5000)

            playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(uri))
            exo.prepare()
            exo.playWhenReady = true

            // Keep screen UI stable; you can tap to show/hide controls
            playerView.controllerAutoShow = false
            playerView.controllerShowTimeoutMs = 2000

            exo.repeatMode = Player.REPEAT_MODE_OFF
        }

        // Tap toggles controller (VideoRental-like)
        playerView.setOnClickListener {
            if (playerView.isControllerFullyVisible) playerView.hideController()
            else playerView.showController()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) window.decorView.systemUiVisibility = 0x1307
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}
