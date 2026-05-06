package com.example.gymapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity {

    private static final String PREFS_AJUSTES = "Ajustes";
    private static final String WORK_NAME = "recordatorio_rutinas";
    private static final int REQUEST_CODE_NOTIFICATIONS = 100;
    private static final int REQUEST_CODE_AJUSTES = 1;

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "es");
        Locale locale = new Locale(idioma);
        Locale.setDefault(locale);

        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.setLocale(locale);
        super.attachBaseContext(newBase.createConfigurationContext(config));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        aplicarModoOscuro();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardView cardRutinas = findViewById(R.id.cardRutinas);
        CardView cardProgreso = findViewById(R.id.cardProgreso);
        CardView cardAjustes = findViewById(R.id.cardAjustes);
        CardView cardPeso = findViewById(R.id.cardPeso);
        CardView cardEstadisticas = findViewById(R.id.cardEstadisticas);
        ImageButton cerrarSesion = findViewById(R.id.btnCerrarSesion);
        ImageButton btnUsuario = findViewById(R.id.btnUsuario);

        btnUsuario.setOnClickListener(v ->
                startActivity(new Intent(this, UsuarioActivity.class))
        );

        cardRutinas.setOnClickListener(v ->
                startActivity(new Intent(this, RutinasActivity.class)));

        cardProgreso.setOnClickListener(v ->
                startActivity(new Intent(this, ProgresoActivity.class)));

        cardPeso.setOnClickListener(v ->
                startActivity(new Intent(this, RegistroPesoActivity.class)));

        cardAjustes.setOnClickListener(v -> {
            Intent intent = new Intent(this, AjustesActivity.class);
            startActivityForResult(intent, REQUEST_CODE_AJUSTES);
        });

        if (cardEstadisticas != null) {
            cardEstadisticas.setOnClickListener(v ->
                    startActivity(new Intent(this, EstadisticasActivity.class)));
        }

        cerrarSesion.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(getString(R.string.cerrar_sesion))
                    .setMessage(getString(R.string.confirmar_cerrar_sesion))
                    .setIcon(android.R.drawable.ic_lock_power_off)
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent i = new Intent(this, LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    })
                    .setNegativeButton(getString(R.string.no_cancel), null);

            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.day_green));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.red));
        });

        aplicarPoliticaRecordatorios();

        RecordatorioWorker.mostrarNotificacionSiToca(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarModoOscuro();
        aplicarPoliticaRecordatorios();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_AJUSTES && resultCode == RESULT_OK) {
            SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);
            guardarAjustesEnFirebase(
                    prefs.getBoolean("modo_oscuro", false),
                    prefs.getBoolean("notificaciones", true),
                    prefs.getBoolean("recordatorios", false),
                    prefs.getString("idioma", "es")
            );

            if (data != null && data.getBooleanExtra("idioma_cambiado", false)) {
                recreate();
            } else {
                aplicarPoliticaRecordatorios();
            }
        }
    }

    private void aplicarModoOscuro() {
        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);
        boolean modoOscuro = prefs.getBoolean("modo_oscuro", false);

        AppCompatDelegate.setDefaultNightMode(
                modoOscuro ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void aplicarPoliticaRecordatorios() {

        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);

        boolean recordatorios = prefs.getBoolean("recordatorios", false);
        boolean notificaciones = prefs.getBoolean("notificaciones", true);

        if (!recordatorios || !notificaciones) {
            cancelarRecordatorio();
            return;
        }

        boolean granted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED;

        if (!granted) {

            boolean yaPreguntado =
                    prefs.getBoolean("permiso_notificaciones_preguntado", false);

            if (!yaPreguntado) {

                prefs.edit()
                        .putBoolean("permiso_notificaciones_preguntado", true)
                        .apply();

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATIONS
                );

            } else {
                cancelarRecordatorio();
            }

            return;
        }

        programarRecordatorio();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {

            SharedPreferences prefs =
                    getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);

            boolean granted =
                    grantResults.length > 0 &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (granted) {

                programarRecordatorio();

                RecordatorioWorker.mostrarNotificacionSiToca(
                        getApplicationContext()
                );

            } else {

                prefs.edit()
                        .putBoolean("notificaciones", false)
                        .putBoolean("recordatorios", false)
                        .apply();

                cancelarRecordatorio();

                boolean noVolverAPreguntar =
                        !ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.POST_NOTIFICATIONS
                        );

                if (noVolverAPreguntar) {
                    mostrarDialogoIrAjustesNotificaciones();
                }
            }
        }
    }

    private void programarRecordatorio() {
        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(
                        RecordatorioWorker.class,
                        3,
                        TimeUnit.HOURS
                ).build();

        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        workRequest
                );
    }

    private void cancelarRecordatorio() {
        WorkManager.getInstance(this)
                .cancelUniqueWork(WORK_NAME);
    }

    private void mostrarDialogoIrAjustesNotificaciones() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_notificaciones_bloqueadas_titulo))
                .setMessage(getString(R.string.dialog_notificaciones_bloqueadas_mensaje))
                .setPositiveButton(getString(R.string.ir_a_ajustes), (d, w) -> abrirAjustesApp())
                .setNegativeButton(getString(R.string.cancelar), null)
                .show();
    }

    private void abrirAjustesApp() {
        Intent intent =
                new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(android.net.Uri.fromParts(
                "package",
                getPackageName(),
                null
        ));
        startActivity(intent);
    }


    private void guardarAjustesEnFirebase(boolean modoOscuro, boolean notificaciones, boolean recordatorios, String idioma) {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        HashMap<String, Object> data = new HashMap<>();
        data.put("modoOscuro", modoOscuro);
        data.put("notificaciones", notificaciones);
        data.put("recordatorios", recordatorios);
        data.put("idioma", idioma);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("ajustes")
                .document("configuracion")
                .set(data);
    }
}