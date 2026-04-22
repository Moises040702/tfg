package com.example.gymapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RecuperarContraseñaActivity extends BaseActivity {

    private EditText emailRecoverInput;
    private Button sendRecoveryButton;
    private ImageButton btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_contrasenia);

        emailRecoverInput = findViewById(R.id.emailRecoverInput);
        sendRecoveryButton = findViewById(R.id.sendRecoveryButton);
        btnVolver = findViewById(R.id.btnVolver);


        btnVolver.setOnClickListener(v -> finish());

        sendRecoveryButton.setOnClickListener(v -> {
            String email = emailRecoverInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(RecuperarContraseñaActivity.this,
                        "Por favor, introduce tu correo electrónico",
                        Toast.LENGTH_SHORT).show();
            } else {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(RecuperarContraseñaActivity.this,
                                        "Se ha enviado un enlace de recuperación a " + email,
                                        Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(RecuperarContraseñaActivity.this,
                                        "Error al enviar el correo: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
}