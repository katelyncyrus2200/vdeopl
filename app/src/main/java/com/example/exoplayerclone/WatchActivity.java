package com.example.exoplayerclone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class WatchActivity extends AppCompatActivity {

    private StyledPlayerView playerView;
    private ExoPlayer player;
    private boolean pickerShown = false;

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

                playUri(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        playerView = findViewById(R.id.player_view);

        playerView.setSystemUiVisibility(0x1307);
        playerView.setControllerVisibilityListener(new StyledPlayerView.ControllerVisibilityListener() {
            @Override public void onVisibilityChanged(int visibility) {
                if (visibility == View.GONE) playerView.setSystemUiVisibility(0x1307);
            }
        });

        if (savedInstanceState != null) {
            pickerShown = savedInstanceState.getBoolean("pickerShown", false);
        }

        initPlayer();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!pickerShown) {
            pickerShown = true;
            openVideoLauncher.launch(new String[]{"video/*"});
        }
    }

    private void initPlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
    }

    private void playUri(Uri uri) {
        MediaItem item = new MediaItem.Builder().setUri(uri).build();
        player.setMediaItem(item);
        player.prepare();
        player.play();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("pickerShown", pickerShown);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
