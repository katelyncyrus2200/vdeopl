package com.example.myvideoplayer

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : ComponentActivity() {

    private var player: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var timeView: TextView? = null
    private var playPauseBtn: ImageButton? = null

    private val timeTicker = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 500)
        }
    }

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
        timeView = findViewById(R.id.txtTime)
        playPauseBtn = findViewById(R.id.btnPlayPause)

        val btnRew = findViewById<ImageButton>(R.id.btnRew5)
        val btnFwd = findViewById<ImageButton>(R.id.btnFwd5)
        val btnStop = findViewById<ImageButton>(R.id.btnStop)

        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(uri))
            exo.prepare()
            exo.playWhenReady = true

            exo.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlayPauseIcon(isPlaying)
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    updatePlayPauseIcon(exo.isPlaying)
                    updateTime()
                }
            })
        }

        btnRew.setOnClickListener {
            val exo = player ?: return@setOnClickListener
            exo.seekTo((exo.currentPosition - 5000).coerceAtLeast(0))
        }

        btnFwd.setOnClickListener {
            val exo = player ?: return@setOnClickListener
            val dur = exo.duration
            val target = exo.currentPosition + 5000
            exo.seekTo(if (dur > 0) target.coerceAtMost(dur) else target)
        }

        playPauseBtn?.setOnClickListener {
            val exo = player ?: return@setOnClickListener
            if (exo.isPlaying) exo.pause() else exo.play()
        }

        btnStop.setOnClickListener {
            val exo = player ?: return@setOnClickListener
            exo.pause()
            exo.seekTo(0)
            updatePlayPauseIcon(false)
            updateTime()
        }

        handler.post(timeTicker)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) window.decorView.systemUiVisibility = 0x1307
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(timeTicker)
        player?.release()
        player = null
    }

    private fun updatePlayPauseIcon(isPlaying: Boolean) {
        val btn = playPauseBtn ?: return
        btn.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
    }

    private fun updateTime() {
        val exo = player ?: return
        val pos = exo.currentPosition.coerceAtLeast(0)
        val dur = exo.duration.takeIf { it > 0 } ?: 0L
        timeView?.text = "${fmt(pos)} / ${fmt(dur)}"
    }

    private fun fmt(ms: Long): String {
        val totalSec = (ms / 1000).toInt()
        val m = totalSec / 60
        val s = totalSec % 60
        return String.format("%02d:%02d", m, s)
    }
}
