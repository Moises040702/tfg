package com.example.gymapp;

import android.content.res.Configuration;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class VideoRutinaActivity extends BaseActivity implements TextureView.SurfaceTextureListener {

    private TextureView textureView;
    private ImageButton btnVolver;

    private MediaPlayer mediaPlayer;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private int videoRotation = 0;
    private String videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_video_rutina);

        textureView = findViewById(R.id.videoViewRutina);
        btnVolver = findViewById(R.id.btnVolverVideo);

        videoUrl = getIntent().getStringExtra("VIDEO_URL");
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            Log.e("VIDEO", "URL vacía");
            finish();
            return;
        }

        textureView.setSurfaceTextureListener(this);

        btnVolver.setOnClickListener(v -> {
            if (mediaPlayer != null) mediaPlayer.stop();
            finish();
        });

        obtenerRotacionVideo(videoUrl);
    }

    private void obtenerRotacionVideo(String videoUrl) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            Uri uri = Uri.parse(videoUrl);
            String scheme = uri.getScheme();
            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                retriever.setDataSource(videoUrl, new HashMap<>());
            } else {
                retriever.setDataSource(this, uri);
            }
            String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            videoRotation = (rotation != null) ? Integer.parseInt(rotation) : 0;
        } catch (Exception e) {
            Log.e("VIDEO_ROTATION", "Error rotación: " + e.getMessage());
            videoRotation = 0;
        } finally {
            try { retriever.release(); } catch (Exception ignored) {}
        }
    }

    private void prepareMediaPlayer(Surface surface) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, Uri.parse(videoUrl));
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(false);
            mediaPlayer.setOnPreparedListener(mp -> {
                videoWidth = mp.getVideoWidth();
                videoHeight = mp.getVideoHeight();
                ajustarVideo(); // Ajustamos al tamaño de pantalla
                mp.start();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("VIDEO_ERROR", "Error: " + what + " " + extra);
                return false;
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e("VIDEO_ERROR", e.getMessage());
        }
    }

    private void ajustarVideo() {
        if (videoWidth == 0 || videoHeight == 0) return;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        boolean rotado = (videoRotation == 90 || videoRotation == 270);
        int realVideoWidth = rotado ? videoHeight : videoWidth;
        int realVideoHeight = rotado ? videoWidth : videoHeight;

        float videoRatio = (float) realVideoWidth / realVideoHeight;
        float screenRatio = (float) screenWidth / screenHeight;

        float scaleX = 1f;
        float scaleY = 1f;

        if (videoRatio > screenRatio) {
            scaleY = Math.min(videoRatio / screenRatio, 1.05f);
        } else {
            scaleX = Math.min(screenRatio / videoRatio, 1.05f);
        }

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY, screenWidth / 2f, screenHeight / 2f);
        textureView.setTransform(matrix);
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull android.graphics.SurfaceTexture surfaceTexture, int width, int height) {
        prepareMediaPlayer(new Surface(surfaceTexture));
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull android.graphics.SurfaceTexture surface, int width, int height) {
        ajustarVideo();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull android.graphics.SurfaceTexture surface) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull android.graphics.SurfaceTexture surface) {}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        textureView.postDelayed(this::ajustarVideo, 100);
    }
}