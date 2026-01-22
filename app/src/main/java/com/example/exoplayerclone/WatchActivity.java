package com.example.exoplayerclone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class WatchActivity extends AppCompatActivity {

    private StyledPlayerView playerView;
    private StyledPlayerControlView centerOverlayControls;
    private ExoPlayer player;

    private boolean pickerShown = false;
    private @Nullable Uri pickedVideoUri = null;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable hideCenterOverlay = () -> {
        if (centerOverlayControls != null) centerOverlayControls.setVisibility(View.GONE);
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
                showControls();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        playerView = findViewById(R.id.player_view);
        centerOverlayControls = findViewById(R.id.center_overlay_controls);

        // Full default ExoPlayer controller (CC/overflow/settings etc.)
        playerView.setUseController(true);
        playerView.setControllerAutoShow(false);
        playerView.setControllerHideOnTouch(false);
        playerView.setControllerShowTimeoutMs(2500);

        playerView.setOnClickListener(v -> showControls());

        initPlayer();
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

        // 15s skip behavior (icons are still Exo styled)
        player = new ExoPlayer.Builder(this)
                .setSeekBackIncrementMs(15_000)
                .setSeekForwardIncrementMs(15_000)
                .build();

        playerView.setPlayer(player);
        centerOverlayControls.setPlayer(player);
    }

    private void play(Uri uri) {
        if (player == null) initPlayer();
        player.setMediaItem(MediaItem.fromUri(uri));
        player.prepare();
        player.play();
    }

    private void showControls() {
        playerView.showController();

        centerOverlayControls.setVisibility(View.VISIBLE);
        uiHandler.removeCallbacks(hideCenterOverlay);
        uiHandler.postDelayed(hideCenterOverlay, 2500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.removeCallbacks(hideCenterOverlay);
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
