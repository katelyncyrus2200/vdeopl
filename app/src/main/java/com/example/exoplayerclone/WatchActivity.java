package com.example.exoplayerclone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class WatchActivity extends AppCompatActivity {

    private StyledPlayerView playerView;
    private StyledPlayerControlView centerOverlayControls;

    private ExoPlayer player;
    private DefaultTrackSelector trackSelector;

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

        // Bottom controller behavior
        playerView.setUseController(true);
        playerView.setControllerAutoShow(false);
        playerView.setControllerHideOnTouch(false);
        playerView.setControllerShowTimeoutMs(2500);

        // Tap video: show bottom + center overlay
        playerView.setOnClickListener(v -> showControls());

        initPlayer();
        applyCenterOverlayIcons();
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

        trackSelector = new DefaultTrackSelector(this);

        player = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setSeekBackIncrementMs(15_000)
                .setSeekForwardIncrementMs(15_000)
                .build();

        // Attach to both controllers
        playerView.setPlayer(player);
        centerOverlayControls.setPlayer(player);
    }

    private void play(Uri uri) {
        if (player == null) initPlayer();

        MediaItem item = MediaItem.fromUri(uri);
        player.setMediaItem(item);
        player.prepare();
        player.play();
    }

    private void showControls() {
        // bottom controller
        playerView.showController();

        // center overlay controller
        centerOverlayControls.setVisibility(View.VISIBLE);

        uiHandler.removeCallbacks(hideCenterOverlay);
        uiHandler.postDelayed(hideCenterOverlay, 2500);
    }

    /**
     * Apply ExoPlayer styled icons to the center overlay buttons.
     * Using ExoPlayer UI R avoids "R.drawable not found".
     */
    private void applyCenterOverlayIcons() {
        if (centerOverlayControls == null) return;

        ImageButton prev = centerOverlayControls.findViewById(com.google.android.exoplayer2.ui.R.id.exo_prev);
        ImageButton rew  = centerOverlayControls.findViewById(com.google.android.exoplayer2.ui.R.id.exo_rew);
        ImageButton play = centerOverlayControls.findViewById(com.google.android.exoplayer2.ui.R.id.exo_play_pause);
        ImageButton ffwd = centerOverlayControls.findViewById(com.google.android.exoplayer2.ui.R.id.exo_ffwd);
        ImageButton next = centerOverlayControls.findViewById(com.google.android.exoplayer2.ui.R.id.exo_next);

        if (prev != null) prev.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_styled_controls_previous);
        if (rew  != null) rew.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_styled_controls_rewind);
        if (play != null) play.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_styled_controls_play);
        if (ffwd != null) ffwd.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_styled_controls_fastforward);
        if (next != null) next.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_styled_controls_next);

        // Force visible white icons
        if (prev != null) prev.setColorFilter(0xFFFFFFFF);
        if (rew  != null) rew.setColorFilter(0xFFFFFFFF);
        if (play != null) play.setColorFilter(0xFFFFFFFF);
        if (ffwd != null) ffwd.setColorFilter(0xFFFFFFFF);
        if (next != null) next.setColorFilter(0xFFFFFFFF);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.removeCallbacks(hideCenterOverlay);

        if (player != null) {
            player.release();
            player = null;
        }
        trackSelector = null;
    }
}
