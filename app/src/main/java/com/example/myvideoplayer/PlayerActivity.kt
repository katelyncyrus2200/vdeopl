package com.example.myvideoplayer

import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TrackSelectionDialogBuilder

class PlayerActivity : ComponentActivity() {

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        window.decorView.systemUiVisibility = 0x1307

        val uri: Uri = intent.data ?: run {
            finish()
            return
        }

        val playerView = findViewById<PlayerView>(R.id.playerView)

        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(uri))
            exo.prepare()
            exo.playWhenReady = true
        }

        val back = findViewById<ImageButton>(R.id.btnBack15)
        val play = findViewById<ImageButton>(R.id.btnPlayPause)
        val fwd = findViewById<ImageButton>(R.id.btnFwd15)
        val subs = findViewById<ImageButton>(R.id.btnSubtitles)
        val audio = findViewById<ImageButton>(R.id.btnAudio)

        back.setOnClickListener {
            player?.seekTo((player!!.currentPosition - 15000).coerceAtLeast(0))
        }

        fwd.setOnClickListener {
            player?.seekTo(player!!.currentPosition + 15000)
        }

        play.setOnClickListener {
            if (player!!.isPlaying) player!!.pause() else player!!.play()
            play.setImageResource(if (player!!.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        }

        subs.setOnClickListener {
            TrackSelectionDialogBuilder(this, "Subtitles", player!!, C.TRACK_TYPE_TEXT).build().show()
        }

        audio.setOnClickListener {
            TrackSelectionDialogBuilder(this, "Audio", player!!, C.TRACK_TYPE_AUDIO).build().show()
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}
