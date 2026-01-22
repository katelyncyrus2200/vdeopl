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

    // Center overlay (ONLY 3 buttons)
    private LinearLayout centerControls;
    private ImageButton centerRew15, centerPlayPause, centerFfwd15;

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

        // Bottom controller behavior
        playerView.setUseController(true);
        playerView.setControllerAutoShow(false);
        playerView.setControllerHideOnTouch(false);
        playerView.setControllerShowTimeoutMs(2500);

        // Center overlay views
        centerControls = findViewById(R.id.center_controls);
        centerRew15 = findViewById(R.id.center_rew_15);
        centerPlayPause = findViewById(R.id.center_play_pause);
        centerFfwd15 = findViewById(R.id.center_ffwd_15);

        // Tap video = show overlay + controller
        playerView.setClickable(true);
        playerView.setOnClickListener(v -> showCenterOverlay());

        initPlayer();

        // REW 15s
        if (centerRew15 != null) {
            centerRew15.setOnClickListener(v -> {
                if (player != null) {
                    long pos = player.getCurrentPosition();
                    player.seekTo(Math.max(0, pos - 15_000));
                }
                showCenterOverlay();
            });
        }

        // PLAY / PAUSE
        if (centerPlayPause != null) {
            centerPlayPause.setOnClickListener(v -> {
                if (player == null) return;
                if (player.isPlaying()) player.pause();
                else player.play();
                updateCenterPlayPauseIcon();
                showCenterOverlay();
            });
        }

        // FFWD 15s
        if (centerFfwd15 != null) {
            centerFfwd15.setOnClickListener(v -> {
                if (player != null) {
                    long pos = player.getCurrentPosition();
                    long dur = player.getDuration();
                    long target = pos + 15_000;
                    if (dur > 0) target = Math.min(dur, target);
                    player.seekTo(target);
                }
                showCenterOverlay();
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

        player = new ExoPlayer.Builder(this).build();
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
        if (playerView != null) playerView.showController();
        if (centerControls != null) centerControls.setVisibility(View.VISIBLE);

        uiHandler.removeCallbacks(hideCenter);
        uiHandler.postDelayed(hideCenter, 2500);
    }

    private void updateCenterPlayPauseIcon() {
        if (centerPlayPause == null || player == null) return;

        if (player.isPlaying()) {
            centerPlayPause.setImageResource(R.drawable.ic_pause_circle);
        } else {
            centerPlayPause.setImageResource(R.drawable.ic_play_circle);
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
