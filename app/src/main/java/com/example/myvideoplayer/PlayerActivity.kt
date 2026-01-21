package com.example.myvideoplayer

import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

class PlayerActivity : ComponentActivity() {

    private var player: ExoPlayer? = null
    private var aspectMode = 0

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

        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(5000)
            .setSeekForwardIncrementMs(5000)
            .build()
            .also { exo ->
                playerView.player = exo
                exo.setMediaItem(MediaItem.fromUri(uri))
                exo.prepare()
                exo.playWhenReady = true
                exo.repeatMode = Player.REPEAT_MODE_OFF

                playerView.controllerAutoShow = false
                playerView.controllerShowTimeoutMs = 2000
            }

        // Tap toggles controller
        playerView.setOnClickListener {
            if (playerView.isControllerFullyVisible) playerView.hideController()
            else playerView.showController()
        }

        // Extra "menu-like" buttons (same spots as VideoRental)
        findViewById<ImageButton>(R.id.exo_aspect_ratio)?.setOnClickListener {
            // Cycle: FIT -> ZOOM -> FILL
            aspectMode = (aspectMode + 1) % 3
            playerView.resizeMode = when (aspectMode) {
                0 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                1 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                else -> AspectRatioFrameLayout.RESIZE_MODE_FILL
            }
        }

        // Subtitles & Audio buttons: Media3 already shows track UI on long-press in some skins,
        // but here we just show/hide subtitles toggle by enabling/disabling text renderer.
        findViewById<ImageButton>(R.id.exo_subtitle)?.setOnClickListener {
            val exo = player ?: return@setOnClickListener
            val current = exo.trackSelectionParameters
            val newParams = current.buildUpon().setIgnoredTextSelectionFlags(0).build()
            exo.trackSelectionParameters = newParams
            // If you want a full track picker dialog, tell me and I’ll add it using Media3 UI dialog builder.
        }

        findViewById<ImageButton>(R.id.exo_audio_track)?.setOnClickListener {
            // Placeholder for full audio track selection (VideoRental-style).
            // Tell me if you want Audio/Subtitles selection dialogs; I’ll add them.
            playerView.showController()
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
