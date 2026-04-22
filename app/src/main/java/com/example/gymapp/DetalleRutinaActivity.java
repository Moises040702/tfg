package com.example.gymapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class DetalleRutinaActivity extends BaseActivity {

    private RecyclerView recyclerRutinas;
    private RutinaAdapter adaptador;
    private FloatingActionButton botonAgregar;
    private ImageButton botonVolver;

    private String codigoCategoria;
    private List<Rutina> listaRutinasActual;

    private static final String PREFS_RUTINAS = "PreferenciasRutinas";
    private static final String PREFIJO_RUTINA = "rutina_";

    @Override
    protected void attachBaseContext(Context nuevoContexto) {
        SharedPreferences prefs = nuevoContexto.getSharedPreferences("Ajustes", MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "es");

        Locale locale = new Locale(idioma);
        Locale.setDefault(locale);

        Configuration config = nuevoContexto.getResources().getConfiguration();
        config.setLocale(locale);

        Context contexto = nuevoContexto.createConfigurationContext(config);
        super.attachBaseContext(contexto);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutinas);

        botonVolver = findViewById(R.id.btnVolverRutinas);
        recyclerRutinas = findViewById(R.id.recyclerViewRutinas);
        botonAgregar = findViewById(R.id.btnAgregarRutina);

        botonVolver.setOnClickListener(v -> onBackPressed());

        codigoCategoria = getIntent().getStringExtra("CODIGO_CATEGORIA");
        if (codigoCategoria == null) codigoCategoria = "biceps";

        listaRutinasActual = cargarRutinasDesdePrefs(codigoCategoria);

        if (listaRutinasActual.isEmpty()) {
            listaRutinasActual = crearRutinasPredeterminadas(codigoCategoria);
            guardarRutinasEnPrefs(codigoCategoria, listaRutinasActual);
            guardarRutinasEnFirebase(codigoCategoria, listaRutinasActual);
        }

        recyclerRutinas.setLayoutManager(new LinearLayoutManager(this));

        adaptador = new RutinaAdapter(
                this,
                listaRutinasActual,
                this::mostrarDialogoRegistrarRealizacion,
                this::mostrarDialogoEliminarRutina
        );

        recyclerRutinas.setAdapter(adaptador);

        botonAgregar.setVisibility(View.VISIBLE);
        botonAgregar.setOnClickListener(v -> mostrarDialogoNuevaRutina());
    }

    private List<Rutina> crearRutinasPredeterminadas(String categoria) {
        List<Rutina> lista = new ArrayList<>();

        switch (categoria) {

            case "chest":
                lista.add(new Rutina(
                        "press_banca",
                        "chest",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.press_banca,
                        Arrays.asList("4x10")
                ));

                lista.add(new Rutina(
                        "press_inclinado",
                        "chest",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.aberturas,
                        Arrays.asList("3x10")
                ));
                break;

            case "biceps":
                lista.add(new Rutina(
                        "curl_barra",
                        "biceps",
                        "Principiante",
                        "android.resource://" + getPackageName() + "/" + R.raw.curl_barra,
                        Arrays.asList("3x12")
                ));

                lista.add(new Rutina(
                        "curl_martillo",
                        "biceps",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.curl_martillo,
                        Arrays.asList("3x10")
                ));
                break;

            case "back":
                lista.add(new Rutina(
                        "dominadas",
                        "back",
                        "Avanzado",
                        "android.resource://" + getPackageName() + "/" + R.raw.dominadas,
                        Arrays.asList("4x8")
                ));

                lista.add(new Rutina(
                        "remo_mancuerna",
                        "back",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.remounimancuerna,
                        Arrays.asList("4x10")
                ));
                break;

            case "legs":
                lista.add(new Rutina(
                        "sentadilla_hack",
                        "legs",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.sentadilla_hack,
                        Arrays.asList("4x10")
                ));

                lista.add(new Rutina(
                        "zancadas",
                        "legs",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.zancadas,
                        Arrays.asList("3x12")
                ));
                break;

            case "shoulders":
                lista.add(new Rutina(
                        "press_militar",
                        "shoulders",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.press_militar,
                        Arrays.asList("3x10")
                ));

                lista.add(new Rutina(
                        "elevaciones_laterales",
                        "shoulders",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.elevaciones_laterales,
                        Arrays.asList("3x12")
                ));
                break;

            case "abs":
                lista.add(new Rutina(
                        "crunch_polea",
                        "abs",
                        "Principiante",
                        "android.resource://" + getPackageName() + "/" + R.raw.crunch_polea,
                        Arrays.asList("3x15")
                ));
                break;
        }

        return lista;
    }

    private void mostrarDialogoNuevaRutina() {
        View vista = LayoutInflater.from(this)
                .inflate(R.layout.dialogo_nuevo_ejercicio, null);

        EditText editNombre = vista.findViewById(R.id.editNombreEjercicio);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.añadir_ejercicio))
                .setView(vista)
                .setPositiveButton(getString(R.string.añadir), (dialog, which) -> {

                    String nombre = editNombre.getText().toString().trim();

                    if (nombre.isEmpty()) {
                        Toast.makeText(this, getString(R.string.introduce_nombre), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Rutina nueva = new Rutina(
                            nombre,
                            codigoCategoria,
                            "Personalizada",
                            "",
                            Collections.singletonList(getString(R.string.añadido_manual))
                    );

                    listaRutinasActual.add(nueva);
                    adaptador.notifyItemInserted(listaRutinasActual.size() - 1);

                    guardarRutinasEnPrefs(codigoCategoria, listaRutinasActual);
                    guardarRutinasEnFirebase(codigoCategoria, listaRutinasActual);
                })
                .setNegativeButton(getString(R.string.cancelar), null)
                .show();
    }

    private void mostrarDialogoEliminarRutina(int posicion) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.eliminar_ejercicio))
                .setMessage(getString(R.string.confirmar_eliminar))
                .setPositiveButton(getString(R.string.si), (d, w) -> {

                    listaRutinasActual.remove(posicion);
                    adaptador.notifyItemRemoved(posicion);

                    guardarRutinasEnPrefs(codigoCategoria, listaRutinasActual);
                    guardarRutinasEnFirebase(codigoCategoria, listaRutinasActual);
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    private void mostrarDialogoRegistrarRealizacion(Rutina rutina) {

        View vista = LayoutInflater.from(this)
                .inflate(R.layout.dialogo_registro_realizacion, null);

        EditText editFecha = vista.findViewById(R.id.editFechaRealizacion);
        EditText editHoraInicio = vista.findViewById(R.id.editHoraInicio);
        EditText editHoraFin = vista.findViewById(R.id.editHoraFin);
        EditText editSeries = vista.findViewById(R.id.editSeriesRealizadas);
        EditText editReps = vista.findViewById(R.id.editRepsPorSerie);
        EditText editPeso = vista.findViewById(R.id.editPesoUtilizado);

        Calendar calendario = Calendar.getInstance();

        editFecha.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendario.getTime()));
        editHoraInicio.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendario.getTime()));
        editHoraFin.setText(editHoraInicio.getText());

        editFecha.setFocusable(false);
        editFecha.setOnClickListener(v -> {
            DatePickerDialog dp = new DatePickerDialog(this,
                    (view1, y, m, d) ->
                            editFecha.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d)),
                    calendario.get(Calendar.YEAR),
                    calendario.get(Calendar.MONTH),
                    calendario.get(Calendar.DAY_OF_MONTH));

            dp.getDatePicker().setMaxDate(calendario.getTimeInMillis());
            dp.show();
        });

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.registrar_realizacion) + ": " + rutina.getNombre())
                .setView(vista)
                .setPositiveButton(getString(R.string.guardar), (d, w) -> {

                    if (TextUtils.isEmpty(editSeries.getText()) ||
                            TextUtils.isEmpty(editReps.getText()) ||
                            TextUtils.isEmpty(editPeso.getText())) {

                        Toast.makeText(this, getString(R.string.completa_campos), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    RegistroRealizacionRutina registro = new RegistroRealizacionRutina(
                            rutina.getNombre(),
                            editFecha.getText().toString(),
                            editHoraInicio.getText().toString(),
                            editHoraFin.getText().toString(),
                            Integer.parseInt(editSeries.getText().toString()),
                            Integer.parseInt(editReps.getText().toString()),
                            Double.parseDouble(editPeso.getText().toString())
                    );

                    FireStoreManager.guardarRegistro(registro, new FireStoreManager.SaveCallback() {
                        @Override
                        public void onSuccess(@NonNull String documentId) {
                            String fechaEntrenamiento = editFecha.getText().toString();

                            FireStoreManager.marcarDiaEntrenado(
                                    fechaEntrenamiento,
                                    DiaCalendario.ESTADO_CUMPLIDO
                            );

                            Toast.makeText(DetalleRutinaActivity.this,
                                    getString(R.string.sesion_guardada),
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(@NonNull Exception e) {
                            Toast.makeText(DetalleRutinaActivity.this,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton(getString(R.string.cancelar), null)
                .show();
    }

    private List<Rutina> cargarRutinasDesdePrefs(String clave) {
        SharedPreferences prefs = getSharedPreferences(PREFS_RUTINAS, MODE_PRIVATE);
        String json = prefs.getString(PREFIJO_RUTINA + clave, null);

        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<ArrayList<Rutina>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    private void guardarRutinasEnPrefs(String clave, List<Rutina> lista) {
        SharedPreferences prefs = getSharedPreferences(PREFS_RUTINAS, MODE_PRIVATE);
        prefs.edit().putString(PREFIJO_RUTINA + clave, new Gson().toJson(lista)).apply();
    }

    private void guardarRutinasEnFirebase(String clave, List<Rutina> lista) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        HashMap<String, Object> datos = new HashMap<>();
        datos.put("categoria", clave);
        datos.put("rutinas", lista);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("rutinas")
                .document(clave)
                .set(datos);
    }
}