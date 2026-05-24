/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.activities;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

import com.foodtracks.app.R;

/**
 * @author Robert
 * @since 16/05
 */
public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        configVideo();
    }

    /**
     * Configuración del Video Player.
     */
    private void configVideo() {
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        videoView = findViewById(R.id.videoViewAlergias);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        String videoPath =
                "android.resource://" + getPackageName() + "/" + R.raw.video_alergias_intolerancias;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);

        videoView.setOnPreparedListener(mp -> videoView.start());

        findViewById(R.id.btnCerrarVideo).setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}
