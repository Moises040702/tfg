package com.example.gymapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends BaseActivity {

    private EditText emailInput, passwordInput, confirmPasswordInput;
    private Button registerButton;
    private TextView goLoginText;
    private ImageButton backButton;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);
        goLoginText = findViewById(R.id.goLoginText);
        backButton = findViewById(R.id.backButton);


        setupPasswordToggle(passwordInput);
        setupPasswordToggle(confirmPasswordInput);


        backButton.setOnClickListener(v -> finish());

        registerButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();
            String pass2 = confirmPasswordInput.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty() || pass2.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(pass2)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidPassword(pass)) {
                Toast.makeText(this, "Debe tener 6-12 caracteres, mayúsculas, minúsculas y un carácter especial", Toast.LENGTH_LONG).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(result -> {
                        result.getUser().sendEmailVerification()
                                .addOnSuccessListener(aVoid -> {
                                    auth.signOut();
                                    Toast.makeText(this, "Registro correcto. Revisa tu email para verificar la cuenta.", Toast.LENGTH_LONG).show();
                                    Intent i = new Intent(this, LoginActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error enviando email de verificación: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error registro: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });

        goLoginText.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{6,12}$";
        return password.matches(pattern);
    }
    private void setupPasswordToggle(EditText passwordField) {
        passwordField.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordField.getRight()
                        - passwordField.getCompoundDrawables()[2].getBounds().width()
                        - passwordField.getPaddingEnd())) {

                    int selection = passwordField.getSelectionEnd();

                    if (passwordField.getInputType() ==
                            (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {

                        passwordField.setInputType(
                                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

                        passwordField.setCompoundDrawablesWithIntrinsicBounds(
                                null, null, getResources().getDrawable(R.drawable.ic_eye), null);

                    } else {

                        passwordField.setInputType(
                                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                        passwordField.setCompoundDrawablesWithIntrinsicBounds(
                                null, null, getResources().getDrawable(R.drawable.ic_eye), null);
                    }

                    passwordField.setSelection(selection);
                    return true;
                }
            }
            return false;
        });
    }
}