package com.example.exoplayerclone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder;

public class WatchActivity extends AppCompatActivity {

    private StyledPlayerView playerView;
    private ExoPlayer player;

    private int resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;

    private boolean pickerShown = false;
    private @Nullable Uri pickedVideoUri = null;

    // Center overlay
    private LinearLayout centerControls;
    private ImageButton centerRew;
    private ImageButton centerPlayPause;
    private ImageButton centerFfwd;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable hideCenter = () -> {
        if (centerControls != null) centerControls.setVisibility(View.GONE);
    };

    private final ActivityResultLauncher<String[]> picker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) {
                    finish();
                    return;
                }
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {}

                pickedVideoUri = uri;
                play(uri);

                // show controls once after pick
                showAllControlsBriefly();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        playerView = findViewById(R.id.player_view);

        centerControls = findViewById(R.id.center_controls);
        centerRew = findViewById(R.id.center_rew);
        centerPlayPause = findViewById(R.id.center_play_pause);
        centerFfwd = findViewById(R.id.center_ffwd);

        // Controller behavior (bottom)
        playerView.setUseController(true);
        playerView.setControllerAutoShow(false);
        playerView.setControllerHideOnTouch(false);
        playerView.setControllerShowTimeoutMs(2500);

        // Tap video to show BOTH bottom + center overlay
        playerView.setClickable(true);
        playerView.setOnClickListener(v -> showAllControlsBriefly());

        initPlayer();

        // Center overlay buttons
        if (centerRew != null) {
            centerRew.setOnClickListener(v -> {
                if (player != null) player.seekBack();
                showAllControlsBriefly();
            });
        }

        if (centerFfwd != null) {
            centerFfwd.setOnClickListener(v -> {
                if (player != null) player.seekForward();
                showAllControlsBriefly();
            });
        }

        if (centerPlayPause != null) {
            centerPlayPause.setOnClickListener(v -> {
                if (player == null) return;
                if (player.isPlaying()) player.pause();
                else player.play();
                updateCenterPlayPauseIcon();
                showAllControlsBriefly();
            });
        }

        // Aspect ratio
        ImageButton aspectBtn = findViewById(R.id.exo_aspect_ratio);
        if (aspectBtn != null) {
            aspectBtn.setOnClickListener(v -> {
                if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
                else if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL;
                else
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;

                playerView.setResizeMode(resizeMode);
                showAllControlsBriefly();
            });
        }

        // Audio track dialog
        ImageButton audioBtn = findViewById(R.id.exo_audio_track);
        if (audioBtn != null) {
            audioBtn.setOnClickListener(v -> {
                if (player == null) return;
                new TrackSelectionDialogBuilder(
                        WatchActivity.this,
                        "Audio",
                        player,
                        C.TRACK_TYPE_AUDIO
                ).build().show();
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!pickerShown) {
            pickerShown = true;
            picker.launch(new String[]{"video/*"});
        } else if (pickedVideoUri != null) {
            play(pickedVideoUri);
        }
    }

    private void initPlayer() {
        if (player != null) return;

        player = new ExoPlayer.Builder(this)
                .setSeekBackIncrementMs(10_000)
                .setSeekForwardIncrementMs(10_000)
                .build();

        playerView.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updateCenterPlayPauseIcon();
            }
        });

        updateCenterPlayPauseIcon();
    }

    private void updateCenterPlayPauseIcon() {
        if (centerPlayPause == null || player == null) return;
        if (player.isPlaying()) {
            centerPlayPause.setImageResource(R.drawable.exo_controls_pause);
        } else {
            centerPlayPause.setImageResource(R.drawable.exo_controls_play);
        }
    }

    private void showAllControlsBriefly() {
        // bottom controller
        playerView.showController();

        // center overlay
        if (centerControls != null) centerControls.setVisibility(View.VISIBLE);

        uiHandler.removeCallbacks(hideCenter);
        uiHandler.postDelayed(hideCenter, 2500);
    }

    private void play(Uri uri) {
        if (player == null) initPlayer();
        MediaItem item = MediaItem.fromUri(uri);
        player.setMediaItem(item);
        player.prepare();
        player.play();
        updateCenterPlayPauseIcon();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.removeCallbacks(hideCenter);
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
