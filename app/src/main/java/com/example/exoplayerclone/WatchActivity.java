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

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class WatchActivity extends AppCompatActivity {

    private StyledPlayerView playerView;
    private ExoPlayer player;

    private boolean pickerShown = false;
    private @Nullable Uri pickedVideoUri = null;

    // Center overlay buttons
    private LinearLayout centerControls;
    private ImageButton centerRew, centerPlayPause, centerFfwd;

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
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {}

                pickedVideoUri = uri;
                play(uri);
                showCenterOverlay();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        playerView = findViewById(R.id.player_view);

        // Bottom controller
        playerView.setUseController(true);
        playerView.setControllerAutoShow(false);
        playerView.setControllerHideOnTouch(false);
        playerView.setControllerShowTimeoutMs(2500);

        // Center overlay
        centerControls = findViewById(R.id.center_controls);
        centerRew = findViewById(R.id.center_rew);
        centerPlayPause = findViewById(R.id.center_play_pause);
        centerFfwd = findViewById(R.id.center_ffwd);

        // Tap video = show overlay
        playerView.setOnClickListener(v -> showCenterOverlay());

        initPlayer();

        // Rewind
        centerRew.setOnClickListener(v -> {
            if (player != null) player.seekBack();
            showCenterOverlay();
        });

        // Fast forward
        centerFfwd.setOnClickListener(v -> {
            if (player != null) player.seekForward();
            showCenterOverlay();
        });

        // Play / Pause
        centerPlayPause.setOnClickListener(v -> {
            if (player == null) return;
            if (player.isPlaying()) player.pause();
            else player.play();
            updateCenterPlayPauseIcon();
            showCenterOverlay();
        });
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
                .setSeekBackIncrementMs(15000)
                .setSeekForwardIncrementMs(15000)
                .build();

        playerView.setPlayer(player);
        updateCenterPlayPauseIcon();
    }

    private void play(Uri uri) {
        if (player == null) initPlayer();

        MediaItem item = MediaItem.fromUri(uri);
        player.setMediaItem(item);
        player.prepare();
        player.play();

        updateCenterPlayPauseIcon();
    }

    private void showCenterOverlay() {
        playerView.showController();
        centerControls.setVisibility(View.VISIBLE);

        uiHandler.removeCallbacks(hideCenter);
        uiHandler.postDelayed(hideCenter, 2500);
    }

    private void updateCenterPlayPauseIcon() {
        if (player == null) return;

        if (player.isPlaying()) {
            centerPlayPause.setImageResource(R.drawable.exo_controls_pause);
        } else {
            centerPlayPause.setImageResource(R.drawable.exo_controls_play);
        }
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
