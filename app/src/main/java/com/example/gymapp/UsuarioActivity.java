package com.example.gymapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
        btnDarseBaja.setOnClickListener(v -> confirmarDarseDeBaja());

        configurarFormatoFecha();
        configurarFormatoTelefono();
        configurarFormatoDocumento();
    }

    private void configurarFormatoFecha() {
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
    }

    private void configurarFormatoTelefono() {
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
                    if (input.length() > 9) {
                        input = input.substring(0, 9);
                    }

                    current = input;

                    etTelefono.removeTextChangedListener(this);
                    etTelefono.setText(current);
                    etTelefono.setSelection(current.length());
                    etTelefono.addTextChangedListener(this);
                }
            }
        });
    }

    private void configurarFormatoDocumento() {
        etDNI.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString()
                        .toUpperCase()
                        .replaceAll("[^0-9A-Z]", "");

                if (input.length() > 9) {
                    input = input.substring(0, 9);
                }

                if (!input.equals(current)) {
                    current = input;

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
                        Toast.makeText(
                                this,
                                getString(R.string.toast_error_cargando_datos),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void guardarCambios() {
        if (user == null) return;

        String nombre = etNombre.getText() != null
                ? etNombre.getText().toString().trim()
                : "";

        String apellidos = etApellidos.getText() != null
                ? etApellidos.getText().toString().trim()
                : "";

        String fechaNacimiento = etFechaNacimiento.getText() != null
                ? etFechaNacimiento.getText().toString().trim()
                : "";

        String telefono = etTelefono.getText() != null
                ? etTelefono.getText().toString().trim()
                : "";

        String dni = etDNI.getText() != null
                ? etDNI.getText().toString().trim().toUpperCase()
                : "";

        if (nombre.isEmpty() || apellidos.isEmpty()) {
            Toast.makeText(
                    this,
                    getString(R.string.toast_usuario_nombre_apellidos_obligatorios),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!fechaNacimiento.isEmpty() && !fechaNacimientoValida(fechaNacimiento)) {
            return;
        }

        if (!dni.isEmpty() && !dniNieValido(dni)) {
            Toast.makeText(
                    this,
                    getString(R.string.toast_dni_nie_invalido),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!telefono.isEmpty() && !telefono.matches("\\d{9}")) {
            Toast.makeText(
                    this,
                    getString(R.string.toast_telefono_invalido),
                    Toast.LENGTH_SHORT
            ).show();
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
                    Toast.makeText(
                            this,
                            getString(R.string.toast_datos_actualizados),
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                getString(R.string.toast_error_guardar_datos),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private boolean fechaNacimientoValida(String fechaTexto) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            sdf.setLenient(false);

            Date fecha = sdf.parse(fechaTexto);

            if (fecha == null) {
                Toast.makeText(
                        this,
                        getString(R.string.toast_fecha_invalida),
                        Toast.LENGTH_SHORT
                ).show();
                return false;
            }

            Calendar nacimiento = Calendar.getInstance();
            nacimiento.setTime(fecha);

            Calendar hoy = Calendar.getInstance();

            if (nacimiento.after(hoy)) {
                Toast.makeText(
                        this,
                        getString(R.string.toast_fecha_futura),
                        Toast.LENGTH_SHORT
                ).show();
                return false;
            }

            return true;

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    getString(R.string.toast_fecha_invalida),
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }
    }

    private boolean dniNieValido(String documento) {
        if (documento == null) return false;

        String texto = documento.trim().toUpperCase();

        final String letras = "TRWAGMYFPDXBNJZSQVHLCKE";

        try {
            if (texto.matches("\\d{8}[A-Z]")) {
                int numero = Integer.parseInt(texto.substring(0, 8));
                char letraCorrecta = letras.charAt(numero % 23);
                return texto.charAt(8) == letraCorrecta;
            }

            if (texto.matches("[XYZ]\\d{7}[A-Z]")) {
                char inicial = texto.charAt(0);
                String numeroTexto;

                if (inicial == 'X') {
                    numeroTexto = "0" + texto.substring(1, 8);
                } else if (inicial == 'Y') {
                    numeroTexto = "1" + texto.substring(1, 8);
                } else {
                    numeroTexto = "2" + texto.substring(1, 8);
                }

                int numero = Integer.parseInt(numeroTexto);
                char letraCorrecta = letras.charAt(numero % 23);
                return texto.charAt(8) == letraCorrecta;
            }

            return false;

        } catch (Exception e) {
            return false;
        }
    }

    private void cambiarContrasena() {
        if (user == null || user.getEmail() == null) return;

        String email = user.getEmail();

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(
                            this,
                            getString(R.string.toast_correo_cambio_contrasena_enviado),
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                getString(R.string.toast_error_enviar_correo),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void confirmarDarseDeBaja() {
        if (user == null) return;

        String palabraConfirmacion = getString(R.string.palabra_confirmacion_eliminar);

        final EditText input = new EditText(this);
        input.setHint(getString(R.string.eliminacion_cuenta_confirmacion_hint, palabraConfirmacion));
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        int padding = (int) (20 * getResources().getDisplayMetrics().density);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(padding, 0, padding, 0);
        layout.addView(input);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.eliminacion_cuenta_titulo))
                .setMessage(getString(R.string.eliminacion_cuenta_mensaje, palabraConfirmacion))
                .setView(layout)
                .setPositiveButton(getString(R.string.eliminar_definitivamente), null)
                .setNegativeButton(getString(R.string.cancelar), null)
                .create();

        dialogo.setOnShowListener(dialog ->
                dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String escrito = input.getText() != null
                            ? input.getText().toString().trim()
                            : "";

                    if (!escrito.equalsIgnoreCase(palabraConfirmacion)) {
                        Toast.makeText(
                                this,
                                getString(R.string.toast_escribe_confirmacion_eliminar),
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    dialogo.dismiss();
                    darseDeBaja();
                })
        );

        dialogo.show();
    }

    private void darseDeBaja() {
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    user.delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(
                                        this,
                                        getString(R.string.toast_cuenta_eliminada),
                                        Toast.LENGTH_SHORT
                                ).show();

                                FirebaseAuth.getInstance().signOut();
                                finishAffinity();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            getString(R.string.toast_error_eliminar_cuenta),
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                getString(R.string.toast_error_eliminar_datos),
                                Toast.LENGTH_SHORT
                        ).show()
                );
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
        if (storedValue == null || storedValue.trim().isEmpty()) {
            return;
        }

        String[] cityCodes = getResources().getStringArray(R.array.city_codes);

        for (int i = 0; i < cityCodes.length; i++) {
            if (cityCodes[i].equalsIgnoreCase(storedValue)) {
                spinnerCiudad.setSelection(i);
                return;
            }
        }

        ArrayAdapter<CharSequence> adapter =
                (ArrayAdapter<CharSequence>) spinnerCiudad.getAdapter();

        int position = adapter.getPosition(storedValue);

        if (position >= 0) {
            spinnerCiudad.setSelection(position);
        }
    }
}