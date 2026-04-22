package com.example.gymapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AjustesActivity extends BaseActivity {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchNotificaciones, switchModoOscuro, switchRecordatorios;
    private ImageButton btnEspaniol, btnIngles;

    private static final String PREFS_AJUSTES = "Ajustes";
    private static final String WORK_NAME = "recordatorio_rutinas";
    private static final int REQUEST_CODE_NOTIFICATIONS = 100;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);


        switchNotificaciones = findViewById(R.id.switchNotificaciones);
        switchModoOscuro = findViewById(R.id.switchModoOscuro);
        switchRecordatorios = findViewById(R.id.switchRecordatorios);
        btnEspaniol = findViewById(R.id.btnEspaniol);
        btnIngles = findViewById(R.id.btnIngles);

        ImageButton btnVolver = findViewById(R.id.btnVolverAjustes);
        btnVolver.setOnClickListener(v -> finish());

        cargarAjustesDesdePreferencias();

        switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            actualizarPreferencias("modo_oscuro", isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
            recreate();
        });

        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            actualizarPreferencias("notificaciones", isChecked);
            aplicarPoliticaRecordatorios();
        });

        switchRecordatorios.setOnCheckedChangeListener((buttonView, isChecked) -> {
            actualizarPreferencias("recordatorios", isChecked);
            aplicarPoliticaRecordatorios();
        });


        btnEspaniol.setOnClickListener(v -> seleccionarIdioma("es"));
        btnIngles.setOnClickListener(v -> seleccionarIdioma("en"));


        cargarAjustesDesdeFirebase();
    }

    private void seleccionarIdioma(String idioma) {
        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);
        prefs.edit().putString("idioma", idioma).apply();
        guardarAjustesEnFirebase();


        btnEspaniol.setAlpha(1f);
        btnIngles.setAlpha(1f);

        recreate();
    }


    private void cargarAjustesDesdePreferencias() {
        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);

        switchModoOscuro.setOnCheckedChangeListener(null);
        switchNotificaciones.setOnCheckedChangeListener(null);
        switchRecordatorios.setOnCheckedChangeListener(null);

        switchModoOscuro.setChecked(prefs.getBoolean("modo_oscuro", false));
        switchNotificaciones.setChecked(prefs.getBoolean("notificaciones", true));
        switchRecordatorios.setChecked(prefs.getBoolean("recordatorios", false));

        switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            actualizarPreferencias("modo_oscuro", isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
            recreate();
        });

        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            actualizarPreferencias("notificaciones", isChecked);
            aplicarPoliticaRecordatorios();
        });

        switchRecordatorios.setOnCheckedChangeListener((buttonView, isChecked) -> {
            actualizarPreferencias("recordatorios", isChecked);
            aplicarPoliticaRecordatorios();
        });
    }

    private void actualizarPreferencias(String key, boolean valor) {
        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);
        prefs.edit().putBoolean(key, valor).apply();
        guardarAjustesEnFirebase();
    }

    private void guardarAjustesEnFirebase() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);

        HashMap<String, Object> data = new HashMap<>();
        data.put("modoOscuro", switchModoOscuro.isChecked());
        data.put("notificaciones", switchNotificaciones.isChecked());
        data.put("recordatorios", switchRecordatorios.isChecked());
        data.put("idioma", prefs.getString("idioma", "es"));

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("ajustes")
                .document("configuracion")
                .set(data);
    }

    private void cargarAjustesDesdeFirebase() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("ajustes")
                .document("configuracion")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        boolean modoOscuro = document.getBoolean("modoOscuro") != null && Boolean.TRUE.equals(document.getBoolean("modoOscuro"));
                        boolean notificaciones = document.getBoolean("notificaciones") == null || Boolean.TRUE.equals(document.getBoolean("notificaciones"));
                        boolean recordatorios = document.getBoolean("recordatorios") != null && Boolean.TRUE.equals(document.getBoolean("recordatorios"));
                        String idioma = document.getString("idioma") != null
                                ? document.getString("idioma")
                                : "es";

                        prefs.edit()
                                .putBoolean("modo_oscuro", modoOscuro)
                                .putBoolean("notificaciones", notificaciones)
                                .putBoolean("recordatorios", recordatorios)
                                .putString("idioma", idioma)
                                .apply();

                        switchModoOscuro.setOnCheckedChangeListener(null);
                        switchNotificaciones.setOnCheckedChangeListener(null);
                        switchRecordatorios.setOnCheckedChangeListener(null);

                        switchModoOscuro.setChecked(modoOscuro);
                        switchNotificaciones.setChecked(notificaciones);
                        switchRecordatorios.setChecked(recordatorios);

                        switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            actualizarPreferencias("modo_oscuro", isChecked);
                            AppCompatDelegate.setDefaultNightMode(
                                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                                            : AppCompatDelegate.MODE_NIGHT_NO
                            );
                            recreate();
                        });

                        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            actualizarPreferencias("notificaciones", isChecked);
                            aplicarPoliticaRecordatorios();
                        });

                        switchRecordatorios.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            actualizarPreferencias("recordatorios", isChecked);
                            aplicarPoliticaRecordatorios();
                        });

                        assert idioma != null;
                        btnEspaniol.setAlpha(1f);
                        btnIngles.setAlpha(1f);
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }


    private void aplicarPoliticaRecordatorios() {
        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);
        boolean recordatorios = prefs.getBoolean("recordatorios", false);
        boolean notificaciones = prefs.getBoolean("notificaciones", true);

        if (!recordatorios || !notificaciones) {
            cancelarRecordatorio();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_CODE_NOTIFICATIONS
            );
            return;
        }

        programarRecordatorio();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (granted) {
                programarRecordatorio();
                Toast.makeText(this, "Permiso de notificaciones concedido ✅", Toast.LENGTH_SHORT).show();
            } else {
                prefs.edit()
                        .putBoolean("notificaciones", false)
                        .putBoolean("recordatorios", false)
                        .apply();

                switchNotificaciones.setChecked(false);
                switchRecordatorios.setChecked(false);

                cancelarRecordatorio();

                boolean noVolverAPreguntar = !ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.POST_NOTIFICATIONS
                );

                if (noVolverAPreguntar) {
                    mostrarDialogoIrAjustesNotificaciones();
                } else {
                    Toast.makeText(this, "Permiso de notificaciones denegado ❌", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void programarRecordatorio() {
        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(RecordatorioWorker.class, 3, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }

    private void cancelarRecordatorio() {
        WorkManager.getInstance(this).cancelUniqueWork(WORK_NAME);
    }

    private void mostrarDialogoIrAjustesNotificaciones() {
        new AlertDialog.Builder(this)
                .setTitle("Permiso bloqueado")
                .setMessage("Has bloqueado las notificaciones. Actívalas desde Ajustes para que los recordatorios funcionen.")
                .setPositiveButton("Ir a Ajustes", (d, w) -> abrirAjustesApp())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirAjustesApp() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(android.net.Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }
}