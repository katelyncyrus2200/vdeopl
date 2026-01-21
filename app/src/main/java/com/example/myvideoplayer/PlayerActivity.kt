package com.example.myvideoplayer

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

class PlayerActivity : ComponentActivity() {

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // VideoRental uses 0x1307 flags (immersive + hide nav/fullscreen + low profile + layout stable/hide-nav)
        window.decorView.systemUiVisibility = 0x1307

        val uri: Uri = intent.data ?: run {
            finish()
            return
        }

        val playerView = findViewById<PlayerView>(R.id.playerView)

        // Similar to VideoRental behavior
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        playerView.useController = true
        playerView.controllerAutoShow = false

        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(uri))
            exo.prepare()
            exo.playWhenReady = true
        }

        // Tap toggles controller (VideoRental-style)
        playerView.setOnClickListener {
            if (playerView.isControllerFullyVisible) playerView.hideController() else playerView.showController()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = 0x1307
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}
