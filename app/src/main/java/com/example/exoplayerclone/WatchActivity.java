package com.example.exoplayerclone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class WatchActivity extends AppCompatActivity {

    private ExoPlayer player;
    private int resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;
    private StyledPlayerView playerView;

    private final ActivityResultLauncher<String[]> openVideoLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;

                try {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {}

                playUri(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        playerView = findViewById(R.id.player_view);

        playerView.setSystemUiVisibility(0x1307);

        playerView.setControllerVisibilityListener(new StyledPlayerView.ControllerVisibilityListener() {
            @Override
            public void onVisibilityChanged(int visibility) {
                if (visibility == View.GONE) {
                    playerView.setSystemUiVisibility(0x1307);
                }
            }
        });

        ImageButton aspectBtn = playerView.findViewById(R.id.exo_aspect_ratio);
        if (aspectBtn != null) {
            aspectBtn.setOnClickListener(v -> {
                if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
                } else if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL;
                } else {
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;
                }
                playerView.setResizeMode(resizeMode);
            });
        }

        ImageButton openBtn = playerView.findViewById(R.id.btn_open_video);
        if (openBtn != null) {
            openBtn.setOnClickListener(v -> openVideoPicker());
        }

        player = new ExoPlayer.Builder(this)
                .setSeekForwardIncrementMs(10_000)
                .setSeekBackIncrementMs(10_000)
                .build();

        playerView.setPlayer(player);
    }

    private void openVideoPicker() {
        openVideoLauncher.launch(new String[]{"video/*"});
    }

    private void playUri(Uri uri) {
        MediaItem item = new MediaItem.Builder()
                .setUri(uri)
                .build();

        player.setMediaItem(item);
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
