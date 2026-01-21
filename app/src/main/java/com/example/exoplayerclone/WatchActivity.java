package com.example.exoplayerclone;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class WatchActivity extends AppCompatActivity {

    private ExoPlayer player;
    private int resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        StyledPlayerView playerView = findViewById(R.id.player_view);

        // Immersive flags similar to the reference app
        playerView.setSystemUiVisibility(0x1307);

        playerView.setControllerVisibilityListener(visibility -> {
            if (visibility == View.GONE) {
                playerView.setSystemUiVisibility(0x1307);
            }
        });

        ImageButton aspectBtn = playerView.findViewById(R.id.exo_aspect_ratio);
        aspectBtn.setOnClickListener(v -> {
            if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT)
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
            else if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL;
            else
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;

            playerView.setResizeMode(resizeMode);
        });

        player = new ExoPlayer.Builder(this)
                .setSeekForwardIncrementMs(10_000)
                .setSeekBackIncrementMs(10_000)
                .build();

        playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        );
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
