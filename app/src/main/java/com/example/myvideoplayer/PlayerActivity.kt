package com.example.myvideoplayer

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TrackSelectionDialogBuilder

class PlayerActivity : ComponentActivity() {

    private var player: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private var bottomBar: View? = null
    private var hideRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // Fullscreen immersive like VideoRental
        window.decorView.systemUiVisibility = 0x1307

        val uri: Uri = intent.data ?: run {
            finish()
            return
        }

        val playerView = findViewById<PlayerView>(R.id.playerView)
        bottomBar = findViewById(R.id.bottomBar)

        // Setup player
        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(uri))
            exo.prepare()
            exo.playWhenReady = true
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack15)
        val btnPlayPause = findViewById<ImageButton>(R.id.btnPlayPause)
        val btnFwd = findViewById<ImageButton>(R.id.btnFwd15)
        val btnSubs = findViewById<ImageButton>(R.id.btnSubtitles)
        val btnAudio = findViewById<ImageButton>(R.id.btnAudio)

        fun updatePlayIcon() {
            val exo = player ?: return
            btnPlayPause.setImageResource(
                if (exo.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        // Back 15 seconds
        btnBack.setOnClickListener {
            val exo = player ?: return@setOnClickListener
            exo.seekTo((exo.currentPosition - 15_000).coerceAtLeast(0))
            showControlsTemporarily()
        }

        // Forward 15 seconds
        btnFwd.setOnClickListener {
            val exo = player ?: return@setOnClickListener
            val dur = exo.duration
            val target = exo.currentPosition + 15_000
            exo.seekTo(if (dur > 0) target.coerceAtMost(dur) else target)
            showControlsTemporarily()
        }

        // Play / Pause
        btnPlayPause.setOnClickListener {
            val exo = player ?: return@setOnClickListener
            if (exo.isPlaying) exo.pause() else exo.play()
            updatePlayIcon()
            showControlsTemporarily()
        }

        // Subtitles menu
        btnSubs.setOnClickListener {
            val exo = player ?: return@setOnClickListener
            TrackSelectionDialogBuilder(this, "Subtitles", exo, C.TRACK_TYPE_TEXT)
                .build()
                .show()
            showControlsTemporarily()
        }

        // Audio menu
        btnAudio.setOnClickListener {
            val exo = player ?: return@setOnClickListener
            TrackSelectionDialogBuilder(this, "Audio", exo, C.TRACK_TYPE_AUDIO)
                .build()
                .show()
            showControlsTemporarily()
        }

        // Tap video → show/hide controls
        playerView.setOnClickListener {
            toggleControls()
        }

        updatePlayIcon()
    }

    private fun toggleControls() {
        val bar = bottomBar ?: return
        if (bar.visibility == View.VISIBLE) {
            bar.visibility = View.GONE
            cancelAutoHide()
        } else {
            showControlsTemporarily()
        }
    }

    private fun showControlsTemporarily() {
        val bar = bottomBar ?: return
        bar.visibility = View.VISIBLE
        cancelAutoHide()
        hideRunnable = Runnable { bar.visibility = View.GONE }
        handler.postDelayed(hideRunnable!!, 2500) // auto-hide after 2.5s
    }

    private fun cancelAutoHide() {
        hideRunnable?.let { handler.removeCallbacks(it) }
        hideRunnable = null
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) window.decorView.systemUiVisibility = 0x1307
    }

    override fun onStop() {
        super.onStop()
        cancelAutoHide()
        player?.release()
        player = null
    }
}
