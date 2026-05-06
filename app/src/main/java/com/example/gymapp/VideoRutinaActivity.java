package com.example.gymapp;

import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class VideoRutinaActivity extends BaseActivity implements TextureView.SurfaceTextureListener {

    private TextureView textureView;

    private MediaPlayer mediaPlayer;
    private Surface surface;

    private int videoWidth = 0;
    private int videoHeight = 0;
    private int videoRotation = 0;

    private String videoUrl;

    private boolean videoPreparado = false;
    private boolean pausadoPorUsuario = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activarPantallaCompleta();

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_video_rutina);

        textureView = findViewById(R.id.videoViewRutina);
        ImageButton btnVolver = findViewById(R.id.btnVolverVideo);

        videoUrl = getIntent().getStringExtra("VIDEO_URL");

        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            Log.e("VIDEO", "URL vacía");
            Toast.makeText(this, getString(R.string.toast_video_no_cargado), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        obtenerRotacionVideo(videoUrl);

        textureView.setSurfaceTextureListener(this);

        textureView.setOnClickListener(v -> alternarPlayPause());

        btnVolver.setOnClickListener(v -> {
            liberarMediaPlayer();
            finish();
        });
    }

    private void activarPantallaCompleta() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
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

            String rotation = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION
            );

            videoRotation = rotation != null ? Integer.parseInt(rotation) : 0;

        } catch (Exception e) {
            Log.e("VIDEO_ROTATION", "Error rotación: " + e.getMessage());
            videoRotation = 0;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
    }

    private void prepararMediaPlayer(Surface surface) {
        try {
            liberarSoloMediaPlayer();

            mediaPlayer = new MediaPlayer();

            Uri uri = Uri.parse(videoUrl);
            String scheme = uri.getScheme();

            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                mediaPlayer.setDataSource(videoUrl);
            } else {
                mediaPlayer.setDataSource(this, uri);
            }

            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(false);

            mediaPlayer.setOnPreparedListener(mp -> {
                videoPreparado = true;

                videoWidth = mp.getVideoWidth();
                videoHeight = mp.getVideoHeight();

                ajustarVideoEquilibrado();

                pausadoPorUsuario = false;
                mp.start();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                pausadoPorUsuario = true;

                try {
                    mp.seekTo(0);
                    ajustarVideoEquilibrado();
                } catch (Exception e) {
                    Log.e("VIDEO_COMPLETION", "Error al reiniciar vídeo: " + e.getMessage());
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("VIDEO_ERROR", "Error MediaPlayer: " + what + " " + extra);
                Toast.makeText(this, getString(R.string.toast_error_reproducir_video), Toast.LENGTH_SHORT).show();
                return true;
            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            Log.e("VIDEO_ERROR", "Error preparando vídeo: " + e.getMessage());
            Toast.makeText(this, getString(R.string.toast_video_no_reproducir), Toast.LENGTH_SHORT).show();
        }
    }

    private void alternarPlayPause() {
        if (mediaPlayer == null || !videoPreparado) {
            return;
        }

        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                pausadoPorUsuario = true;
            } else {
                mediaPlayer.start();
                pausadoPorUsuario = false;
            }
        } catch (Exception e) {
            Log.e("VIDEO_PAUSE", "Error pausando/reanudando: " + e.getMessage());
        }
    }

    private void ajustarVideoEquilibrado() {
        if (textureView == null || videoWidth == 0 || videoHeight == 0) {
            return;
        }

        int viewWidth = textureView.getWidth();
        int viewHeight = textureView.getHeight();

        if (viewWidth == 0 || viewHeight == 0) {
            return;
        }

        boolean videoRotado = videoRotation == 90 || videoRotation == 270;

        int anchoRealVideo = videoRotado ? videoHeight : videoWidth;
        int altoRealVideo = videoRotado ? videoWidth : videoHeight;

        float viewRatio = (float) viewWidth / (float) viewHeight;
        float videoRatio = (float) anchoRealVideo / (float) altoRealVideo;

        float scaleX = 1f;
        float scaleY = 1f;
        float zoomSuave = 1.12f;

        if (viewRatio > videoRatio) {
            scaleX = videoRatio / viewRatio;
            scaleY = 1f;
        } else {
            scaleX = 1f;
            scaleY = viewRatio / videoRatio;
        }

        scaleX *= zoomSuave;
        scaleY *= zoomSuave;

        Matrix matrix = new Matrix();

        float centroX = viewWidth / 2f;
        float centroY = viewHeight / 2f;

        matrix.setScale(scaleX, scaleY, centroX, centroY);

        if (videoRotation != 0) {
            matrix.postRotate(videoRotation, centroX, centroY);
        }

        textureView.setTransform(matrix);
    }

    @Override
    public void onSurfaceTextureAvailable(
            @NonNull SurfaceTexture surfaceTexture,
            int width,
            int height
    ) {
        surface = new Surface(surfaceTexture);
        prepararMediaPlayer(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(
            @NonNull SurfaceTexture surfaceTexture,
            int width,
            int height
    ) {
        textureView.post(this::ajustarVideoEquilibrado);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        liberarMediaPlayer();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        activarPantallaCompleta();

        if (textureView != null) {
            textureView.postDelayed(this::ajustarVideoEquilibrado, 150);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mediaPlayer != null && videoPreparado && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        activarPantallaCompleta();

        if (mediaPlayer != null && videoPreparado && !pausadoPorUsuario) {
            try {
                mediaPlayer.start();
            } catch (Exception e) {
                Log.e("VIDEO_RESUME", "Error reanudando vídeo: " + e.getMessage());
            }
        }

        if (textureView != null) {
            textureView.postDelayed(this::ajustarVideoEquilibrado, 100);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            activarPantallaCompleta();

            if (textureView != null) {
                textureView.postDelayed(this::ajustarVideoEquilibrado, 100);
            }
        }
    }

    @Override
    protected void onDestroy() {
        liberarMediaPlayer();
        super.onDestroy();
    }

    private void liberarSoloMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (Exception ignored) {
            }

            try {
                mediaPlayer.release();
            } catch (Exception ignored) {
            }

            mediaPlayer = null;
        }

        videoPreparado = false;
    }

    private void liberarMediaPlayer() {
        liberarSoloMediaPlayer();

        if (surface != null) {
            try {
                surface.release();
            } catch (Exception ignored) {
            }

            surface = null;
        }
    }
}