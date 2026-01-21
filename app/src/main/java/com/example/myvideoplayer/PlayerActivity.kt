package com.example.myvideoplayer

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : ComponentActivity() {

    private var player: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private var centerControls: View? = null
    private var hideRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        window.decorView.systemUiVisibility = 0x1307

        val uri: Uri = intent.data ?: run {
            finish()
            return
        }

        val playerView = findViewById<PlayerView>(R.id.playerView)
        centerControls = findViewById(R.id.centerControls)

        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(uri))
            exo.prepare()
            exo.playWhenReady = true
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack15)
        val btnPlayPause = findViewById<ImageButton>(R.id.btnPlayPause)
        val btnFwd = findViewById<ImageButton>(R.id.btnFwd15)

        fun updatePlayIcon() {
            val exo = player ?: return
            btnPlayPause.setImageResource(if (exo.isPlaying) R.drawable.ic_pause_circle else R.drawable.ic_play_circle)
        }

        btnBack.setOnClickListener {
            val exo = player ?: return@setOnClickListener
            exo.seekTo((exo.currentPosition - 15000).coerceAtLeast(0))
            showControlsTemporarily()
        }

        btnFwd.setOnClickListener {
            val exo = player ?: return@setOnClickListener
            exo.seekTo(exo.currentPosition + 15000)
            showControlsTemporarily()
        }

        btnPlayPause.setOnClickListener {
            val exo = player ?: return@setOnClickListener
            if (exo.isPlaying) exo.pause() else exo.play()
            updatePlayIcon()
            showControlsTemporarily()
        }

        playerView.setOnClickListener { toggleControls() }

        updatePlayIcon()
    }

    private fun toggleControls() {
        val center = centerControls ?: return
        if (center.visibility == View.VISIBLE) {
            center.visibility = View.GONE
            cancelAutoHide()
        } else {
            showControlsTemporarily()
        }
    }

    private fun showControlsTemporarily() {
        val center = centerControls ?: return
        center.visibility = View.VISIBLE
        cancelAutoHide()
        hideRunnable = Runnable { center.visibility = View.GONE }
        handler.postDelayed(hideRunnable!!, 2500)
    }

    private fun cancelAutoHide() {
        hideRunnable?.let { handler.removeCallbacks(it) }
        hideRunnable = null
    }

    override fun onStop() {
        super.onStop()
        cancelAutoHide()
        player?.release()
        player = null
    }
}
