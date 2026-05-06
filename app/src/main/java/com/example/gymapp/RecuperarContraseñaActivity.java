package com.example.gymapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperarContraseñaActivity extends BaseActivity {

    private EditText emailRecoverInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_contrasenia);

        emailRecoverInput = findViewById(R.id.emailRecoverInput);
        Button sendRecoveryButton = findViewById(R.id.sendRecoveryButton);
        ImageButton btnVolver = findViewById(R.id.btnVolver);


        btnVolver.setOnClickListener(v -> finish());

        sendRecoveryButton.setOnClickListener(v -> {
            String email = emailRecoverInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_introduce_correo), Toast.LENGTH_SHORT).show();
            } else {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, getString(R.string.toast_enlace_recuperacion_enviado), Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(this, getString(R.string.toast_error_enviar_enlace), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}