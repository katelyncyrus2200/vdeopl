package com.example.exoplayerclone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

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
import com.google.android.exoplayer2.util.MimeTypes;

import java.util.Collections;

public class WatchActivity extends AppCompatActivity {

    private StyledPlayerView playerView;
    private ExoPlayer player;

    private int resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;

    private boolean videoPickerShown = false;
    private @Nullable Uri pickedVideoUri = null;

    private boolean controllerVisible = false;

    // Center overlay controls
    private View centerControls;
    private ImageButton centerRew;
    private ImageButton centerPlayPause;
    private ImageButton centerFfwd;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable hideCenterControls = () -> {
        if (centerControls != null) centerControls.setVisibility(View.GONE);
    };

    // Declare subtitle launcher FIRST to avoid "illegal forward reference"
    private final ActivityResultLauncher<String[]> openSubtitleLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), subtitleUri -> {
                playPickedVideoWithOptionalSubtitle(subtitleUri); // cancel => null
            });

    private final ActivityResultLauncher<String[]> openVideoLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) {
                    finish();
                    return;
                }

                try {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (SecurityException ignored) {}

                pickedVideoUri = uri;

                // After selecting video, ask (optional) subtitle file
                openSubtitleLauncher.launch(new String[]{
                        "text/*",
                        "application/x-subrip",
                        "application/octet-stream"
                });
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

        // Tap-to-show controls like original (bottom controller + center overlay)
        playerView.setUseController(true);
        playerView.setControllerAutoShow(false);
        playerView.setControllerHideOnTouch(false);   // we handle touch ourselves
        playerView.setControllerShowTimeoutMs(2500);

        playerView.setClickable(true);
        playerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (controllerVisible) {
                    playerView.hideController();
                    if (centerControls != null) centerControls.setVisibility(View.GONE);
                } else {
                    playerView.showController();
                    showCenterControlsBriefly();
                }
                return true;
            }
            return false;
        });

        // Immersive flags like original
        playerView.setSystemUiVisibility(0x1307);
        playerView.setControllerVisibilityListener(new StyledPlayerView.ControllerVisibilityListener() {
            @Override
            public void onVisibilityChanged(int visibility) {
                controllerVisible = (visibility == View.VISIBLE);
                if (!controllerVisible && centerControls != null) {
                    centerControls.setVisibility(View.GONE);
                }
                if (visibility == View.GONE) {
                    playerView.setSystemUiVisibility(0x1307);
                }
            }
        });

        if (savedInstanceState != null) {
            videoPickerShown = savedInstanceState.getBoolean("videoPickerShown", false);
            String savedVideo = savedInstanceState.getString("pickedVideoUri", null);
            if (savedVideo != null) pickedVideoUri = Uri.parse(savedVideo);
        }

        initPlayerIfNeeded();

        // Center overlay buttons actions
        if (centerRew != null) {
            centerRew.setOnClickListener(v -> {
                if (player != null) player.seekBack();
                showCenterControlsBriefly();
            });
        }
        if (centerFfwd != null) {
            centerFfwd.setOnClickListener(v -> {
                if (player != null) player.seekForward();
                showCenterControlsBriefly();
            });
        }
        if (centerPlayPause != null) {
            centerPlayPause.setOnClickListener(v -> {
                if (player == null) return;
                if (player.isPlaying()) player.pause();
                else player.play();
                showCenterControlsBriefly();
                updateCenterPlayPauseIcon();
            });
        }

        // Aspect ratio toggle
        ImageButton aspectBtn = playerView.findViewById(R.id.exo_aspect_ratio);
        if (aspectBtn != null) {
            aspectBtn.setOnClickListener(v -> {
                if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
                else if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL;
                else
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;

                playerView.setResizeMode(resizeMode);
                showCenterControlsBriefly();
            });
        }

        // Audio track button
        ImageButton audioBtn = playerView.findViewById(R.id.exo_audio_track);
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
        initPlayerIfNeeded();

        // Auto picker on first launch
        if (!videoPickerShown) {
            videoPickerShown = true;
            openVideoLauncher.launch(new String[]{"video/*"});
        }
    }

    private void initPlayerIfNeeded() {
        if (player != null) return;

        // Original app uses 10s jumps
        player = new ExoPlayer.Builder(this)
                .setSeekForwardIncrementMs(10_000)
                .setSeekBackIncrementMs(10_000)
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

    private void showCenterControlsBriefly() {
        if (centerControls == null) return;
        centerControls.setVisibility(View.VISIBLE);
        uiHandler.removeCallbacks(hideCenterControls);
        uiHandler.postDelayed(hideCenterControls, 2500);
    }

    private void updateCenterPlayPauseIcon() {
        if (centerPlayPause == null || player == null) return;

        // Use ExoPlayer UI drawables to avoid missing resources
        if (player.isPlaying()) {
            centerPlayPause.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_pause);
        } else {
            centerPlayPause.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_play);
        }
    }

    private void playPickedVideoWithOptionalSubtitle(@Nullable Uri subtitleUri) {
        if (pickedVideoUri == null) return;

        initPlayerIfNeeded();

        MediaItem mediaItem;
        if (subtitleUri != null) {
            try {
                getContentResolver().takePersistableUriPermission(
                        subtitleUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (SecurityException ignored) {}

            String mime = guessSubtitleMimeType(subtitleUri);

            MediaItem.SubtitleConfiguration sub =
                    new MediaItem.SubtitleConfiguration.Builder(subtitleUri)
                            .setMimeType(mime)
                            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                            .build();

            mediaItem = new MediaItem.Builder()
                    .setUri(pickedVideoUri)
                    .setSubtitleConfigurations(Collections.singletonList(sub))
                    .build();
        } else {
            mediaItem = new MediaItem.Builder()
                    .setUri(pickedVideoUri)
                    .build();
        }

        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
        updateCenterPlayPauseIcon();
        showCenterControlsBriefly();
    }

    private String guessSubtitleMimeType(Uri subtitleUri) {
        String name = subtitleUri.getLastPathSegment();
        if (name != null) name = name.toLowerCase();
        if (name != null && name.endsWith(".vtt")) return MimeTypes.TEXT_VTT;
        return MimeTypes.APPLICATION_SUBRIP; // default SRT
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("videoPickerShown", videoPickerShown);
        if (pickedVideoUri != null) outState.putString("pickedVideoUri", pickedVideoUri.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.removeCallbacks(hideCenterControls);
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
