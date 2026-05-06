package com.example.gymapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;

public class LoginActivity extends BaseActivity {

    EditText emailInput, passwordInput;
    Button loginButton;
    TextView forgotPassword, goRegister;

    private FirebaseAuth auth;

    private boolean isPasswordVisible = false;

    private static final String PREFS_AJUSTES = "Ajustes";
    private static final String PREFS_INSTALACION = "Instalacion";
    private static final String KEY_INSTALL_TIME = "install_time";

    private boolean entradaMainEnCurso = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        aplicarIdioma();

        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        cerrarSesionSiEsNuevaInstalacion();

        FirebaseUser user = auth.getCurrentUser();

        if (user != null && user.isEmailVerified()) {
            mostrarPantallaCarga();
            prepararAjustesUsuarioYEntrar(user.getUid());
            return;
        }

        if (user != null && !user.isEmailVerified()) {
            auth.signOut();
        }

        entradaMainEnCurso = false;

        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        goRegister = findViewById(R.id.goRegister);

        configurarOjitoPassword();

        loginButton.setOnClickListener(v -> {

            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                        this,
                        getString(R.string.toast_completa_email_password),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (entradaMainEnCurso) {
                return;
            }

            mostrarCargaEnBotonLogin(true);

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        FirebaseUser usuarioLogin = auth.getCurrentUser();

                        if (usuarioLogin != null && usuarioLogin.isEmailVerified()) {

                            prepararAjustesUsuarioYEntrar(usuarioLogin.getUid());

                        } else {

                            Toast.makeText(
                                    this,
                                    getString(R.string.toast_verifica_email),
                                    Toast.LENGTH_LONG
                            ).show();

                            auth.signOut();
                            entradaMainEnCurso = false;
                            mostrarCargaEnBotonLogin(false);
                        }

                    })
                    .addOnFailureListener(e -> {
                        entradaMainEnCurso = false;
                        mostrarCargaEnBotonLogin(false);
                        e.printStackTrace();

                        Toast.makeText(
                                this,
                                getString(R.string.toast_error_con_mensaje, e.getMessage()),
                                Toast.LENGTH_LONG
                        ).show();
                    });
        });

        forgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, RecuperarContraseñaActivity.class))
        );

        goRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configurarOjitoPassword() {

        passwordInput.setOnTouchListener((v, event) -> {

            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {

                Drawable drawableEnd = passwordInput.getCompoundDrawables()[DRAWABLE_RIGHT];

                if (drawableEnd != null &&
                        event.getRawX() >= (passwordInput.getRight()
                                - drawableEnd.getBounds().width()
                                - passwordInput.getPaddingEnd())) {

                    isPasswordVisible = !isPasswordVisible;

                    if (isPasswordVisible) {
                        passwordInput.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        );
                    } else {
                        passwordInput.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                        );
                    }

                    passwordInput.setSelection(passwordInput.getText().length());
                    return true;
                }
            }

            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth != null ? auth.getCurrentUser() : null;

        if (user == null) {
            entradaMainEnCurso = false;
        }
    }

    private void mostrarPantallaCarga() {
        setContentView(R.layout.activity_loading);
    }

    private void mostrarCargaEnBotonLogin(boolean cargando) {
        if (loginButton == null) return;

        loginButton.setEnabled(!cargando);

        if (cargando) {
            loginButton.setText(getString(R.string.cargando_datos));
        } else {
            loginButton.setText(getString(R.string.login));
        }
    }

    private void prepararAjustesUsuarioYEntrar(String uid) {

        if (entradaMainEnCurso) {
            return;
        }

        entradaMainEnCurso = true;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("ajustes")
                .document("configuracion")
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        boolean modoOscuro = Boolean.TRUE.equals(document.getBoolean("modoOscuro"));
                        boolean notificaciones = Boolean.TRUE.equals(document.getBoolean("notificaciones"));
                        boolean recordatorios = Boolean.TRUE.equals(document.getBoolean("recordatorios"));

                        String idioma = document.getString("idioma") != null
                                ? document.getString("idioma")
                                : "es";

                        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);

                        prefs.edit()
                                .putBoolean("modo_oscuro", modoOscuro)
                                .putBoolean("notificaciones", notificaciones)
                                .putBoolean("recordatorios", recordatorios)
                                .putString("idioma", idioma)
                                .putBoolean("permiso_notificaciones_preguntado", false)
                                .apply();

                        entrarAlMain();

                    } else {

                        guardarAjustesInicialesDesactivados(uid);
                    }
                })
                .addOnFailureListener(e -> {
                    aplicarAjustesLocalesDesactivados();
                    entrarAlMain();
                });
    }

    private void guardarAjustesInicialesDesactivados(String uid) {
        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);

        prefs.edit()
                .putBoolean("modo_oscuro", false)
                .putBoolean("notificaciones", false)
                .putBoolean("recordatorios", false)
                .putString("idioma", "es")
                .putBoolean("permiso_notificaciones_preguntado", false)
                .apply();

        HashMap<String, Object> data = new HashMap<>();
        data.put("modoOscuro", false);
        data.put("notificaciones", false);
        data.put("recordatorios", false);
        data.put("idioma", "es");

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("ajustes")
                .document("configuracion")
                .set(data)
                .addOnCompleteListener(task -> entrarAlMain());
    }

    private void aplicarAjustesLocalesDesactivados() {
        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);

        prefs.edit()
                .putBoolean("modo_oscuro", false)
                .putBoolean("notificaciones", false)
                .putBoolean("recordatorios", false)
                .putString("idioma", "es")
                .putBoolean("permiso_notificaciones_preguntado", false)
                .apply();
    }

    private void entrarAlMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void aplicarIdioma() {

        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);

        String idioma = prefs.getString("idioma", "es");

        Locale locale = new Locale(idioma);
        Locale.setDefault(locale);

        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void cerrarSesionSiEsNuevaInstalacion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            long installTimeActual = packageInfo.firstInstallTime;

            SharedPreferences prefsInstalacion =
                    getSharedPreferences(PREFS_INSTALACION, MODE_PRIVATE);

            long installTimeGuardado = prefsInstalacion.getLong(KEY_INSTALL_TIME, -1);

            if (installTimeGuardado != installTimeActual) {
                FirebaseAuth.getInstance().signOut();

                getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply();

                prefsInstalacion.edit()
                        .putLong(KEY_INSTALL_TIME, installTimeActual)
                        .apply();
            }

        } catch (Exception e) {
            e.printStackTrace();
            FirebaseAuth.getInstance().signOut();
        }
    }
}