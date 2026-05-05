package com.example.gymapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;

import android.content.res.Configuration;
import android.view.MotionEvent;
import android.graphics.drawable.Drawable;

public class LoginActivity extends BaseActivity {

    EditText emailInput, passwordInput;
    Button loginButton;
    TextView forgotPassword, goRegister;

    private FirebaseAuth auth;

    private boolean isPasswordVisible = false;

    private static final String PREFS_AJUSTES = "Ajustes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        aplicarIdiomaYModoOscuro();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

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
                Toast.makeText(this, "Completa email y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        FirebaseUser user = auth.getCurrentUser();

                        if (user != null && user.isEmailVerified()) {

                            prepararAjustesUsuarioYEntrar(user.getUid());

                        } else {

                            Toast.makeText(this,
                                    "Debes verificar tu email antes de iniciar sesión",
                                    Toast.LENGTH_LONG).show();

                            auth.signOut();
                        }

                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(this, "Error " + e.getMessage(), Toast.LENGTH_LONG).show();
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

        FirebaseUser user = auth.getCurrentUser();

        if (user != null && user.isEmailVerified()) {
            prepararAjustesUsuarioYEntrar(user.getUid());
        }
    }

    private void prepararAjustesUsuarioYEntrar(String uid) {
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
                .apply();
    }

    private void entrarAlMain() {
        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);
        boolean modoOscuro = prefs.getBoolean("modo_oscuro", false);

        AppCompatDelegate.setDefaultNightMode(
                modoOscuro ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void aplicarIdiomaYModoOscuro() {

        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);

        String idioma = prefs.getString("idioma", "es");

        Locale locale = new Locale(idioma);
        Locale.setDefault(locale);

        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        boolean modoOscuro = prefs.getBoolean("modo_oscuro", false);

        AppCompatDelegate.setDefaultNightMode(
                modoOscuro ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}