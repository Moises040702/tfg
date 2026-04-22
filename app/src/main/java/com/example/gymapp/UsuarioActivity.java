package com.example.gymapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class UsuarioActivity extends BaseActivity {

    private FirebaseUser user;
    private FirebaseFirestore db;

    private TextView tvCorreo;
    private TextInputEditText etNombre, etApellidos, etFechaNacimiento, etTelefono, etDNI;
    private Spinner spinnerCiudad;
    private MaterialButton btnGuardarDatos, btnCambiarContrasena, btnDarseBaja;
    private ImageButton btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);


        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();


        tvCorreo = findViewById(R.id.tvCorreo);
        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etTelefono = findViewById(R.id.etTelefono);
        etDNI = findViewById(R.id.etDNI);
        spinnerCiudad = findViewById(R.id.spinnerCiudad);

        btnGuardarDatos = findViewById(R.id.btnGuardarDatos);
        btnCambiarContrasena = findViewById(R.id.btnCambiarContrasena);
        btnDarseBaja = findViewById(R.id.btnDarseBaja);
        btnVolver = findViewById(R.id.btnVolver);

        // Configurar Spinner de ciudades (texto visible traducido según idioma)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.ciudades_espana,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCiudad.setAdapter(adapter);

        if (user != null) {
            tvCorreo.setText(user.getEmail());
            cargarDatosUsuario();
        }

        btnVolver.setOnClickListener(v -> finish());
        btnGuardarDatos.setOnClickListener(v -> guardarCambios());
        btnCambiarContrasena.setOnClickListener(v -> cambiarContrasena());
        btnDarseBaja.setOnClickListener(v -> darseDeBaja());


        etFechaNacimiento.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d]", "");
                    StringBuilder formatted = new StringBuilder();

                    int len = clean.length();
                    for (int i = 0; i < len && i < 8; i++) {
                        formatted.append(clean.charAt(i));
                        if ((i == 1 || i == 3) && i != len - 1) {
                            formatted.append("/");
                        }
                    }

                    current = formatted.toString();
                    etFechaNacimiento.removeTextChangedListener(this);
                    etFechaNacimiento.setText(current);
                    etFechaNacimiento.setSelection(current.length());
                    etFechaNacimiento.addTextChangedListener(this);
                }
            }
        });


        etTelefono.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().replaceAll("[^0-9]", "");
                if (!input.equals(current)) {
                    if (input.length() > 9) input = input.substring(0, 9);
                    current = input;
                    etTelefono.removeTextChangedListener(this);
                    etTelefono.setText(current);
                    etTelefono.setSelection(current.length());
                    etTelefono.addTextChangedListener(this);
                }
            }
        });


        etDNI.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().toUpperCase().replaceAll("[^0-9A-Z]", "");

                if (!input.equals(current)) {
                    StringBuilder formatted = new StringBuilder();
                    int len = input.length();

                    for (int i = 0; i < len && i < 9; i++) {
                        if (i < 8 && Character.isDigit(input.charAt(i))) {
                            formatted.append(input.charAt(i));
                        } else if (i == 8 && Character.isLetter(input.charAt(i))) {
                            formatted.append(input.charAt(i));
                        }
                    }

                    current = formatted.toString();
                    etDNI.removeTextChangedListener(this);
                    etDNI.setText(current);
                    etDNI.setSelection(current.length());
                    etDNI.addTextChangedListener(this);
                }
            }
        });
    }

    private void cargarDatosUsuario() {
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etNombre.setText(documentSnapshot.getString("nombre"));
                        etApellidos.setText(documentSnapshot.getString("apellidos"));
                        etFechaNacimiento.setText(documentSnapshot.getString("fechaNacimiento"));
                        etTelefono.setText(documentSnapshot.getString("telefono"));
                        etDNI.setText(documentSnapshot.getString("dni"));

                        String ciudadGuardada = documentSnapshot.getString("ciudad");
                        setSpinnerCityFromStoredValue(ciudadGuardada);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando datos", Toast.LENGTH_SHORT).show()
                );
    }

    private void guardarCambios() {
        if (user == null) return;

        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String apellidos = etApellidos.getText() != null ? etApellidos.getText().toString().trim() : "";
        String fechaNacimiento = etFechaNacimiento.getText() != null ? etFechaNacimiento.getText().toString().trim() : "";
        String telefono = etTelefono.getText() != null ? etTelefono.getText().toString().trim() : "";
        String dni = etDNI.getText() != null ? etDNI.getText().toString().trim() : "";

        if (nombre.isEmpty() || apellidos.isEmpty()) {
            Toast.makeText(this, "Rellena al menos nombre y apellidos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dni.matches("\\d{8}[A-Z]")) {
            Toast.makeText(this, "DNI debe ser 8 números y 1 letra", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!telefono.matches("\\d{9}")) {
            Toast.makeText(this, "El teléfono debe tener 9 cifras", Toast.LENGTH_SHORT).show();
            return;
        }

        String ciudadCodigo = getSelectedCityCode();

        HashMap<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre);
        datos.put("apellidos", apellidos);
        datos.put("fechaNacimiento", fechaNacimiento);
        datos.put("telefono", telefono);
        datos.put("dni", dni);
        datos.put("ciudad", ciudadCodigo);

        db.collection("users")
                .document(user.getUid())
                .set(datos)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                );
    }

    private void cambiarContrasena() {
        if (user == null || user.getEmail() == null) return;

        String email = user.getEmail();
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Correo enviado para cambiar contraseña", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al enviar correo", Toast.LENGTH_SHORT).show()
                );
    }

    private void darseDeBaja() {
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        user.delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(this, "Cuenta eliminada", Toast.LENGTH_SHORT).show();
                                    finishAffinity();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error al eliminar cuenta", Toast.LENGTH_SHORT).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al eliminar datos", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private String getSelectedCityCode() {
        int position = spinnerCiudad.getSelectedItemPosition();
        String[] cityCodes = getResources().getStringArray(R.array.city_codes);

        if (position >= 0 && position < cityCodes.length) {
            return cityCodes[position];
        }

        return "";
    }

    private void setSpinnerCityFromStoredValue(String storedValue) {
        if (storedValue == null || storedValue.trim().isEmpty()) return;

        String[] cityCodes = getResources().getStringArray(R.array.city_codes);

        for (int i = 0; i < cityCodes.length; i++) {
            if (cityCodes[i].equalsIgnoreCase(storedValue)) {
                spinnerCiudad.setSelection(i);
                return;
            }
        }

        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerCiudad.getAdapter();
        int position = adapter.getPosition(storedValue);
        if (position >= 0) {
            spinnerCiudad.setSelection(position);
        }
    }
}