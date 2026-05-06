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
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                        new ArrayList<>()
                ));

                lista.add(new Rutina(
                        "press_inclinado",
                        "chest",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.aberturas,
                        new ArrayList<>()
                ));
                break;

            case "biceps":
                lista.add(new Rutina(
                        "curl_barra",
                        "biceps",
                        "Principiante",
                        "android.resource://" + getPackageName() + "/" + R.raw.curl_barra,
                        new ArrayList<>()
                ));

                lista.add(new Rutina(
                        "curl_martillo",
                        "biceps",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.curl_martillo,
                        new ArrayList<>()
                ));
                break;

            case "back":
                lista.add(new Rutina(
                        "dominadas",
                        "back",
                        "Avanzado",
                        "android.resource://" + getPackageName() + "/" + R.raw.dominadas,
                        new ArrayList<>()
                ));

                lista.add(new Rutina(
                        "remo_mancuerna",
                        "back",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.remounimancuerna,
                        new ArrayList<>()
                ));
                break;

            case "legs":
                lista.add(new Rutina(
                        "sentadilla_hack",
                        "legs",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.sentadilla_hack,
                        new ArrayList<>()
                ));

                lista.add(new Rutina(
                        "zancadas",
                        "legs",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.zancadas,
                        new ArrayList<>()
                ));
                break;

            case "shoulders":
                lista.add(new Rutina(
                        "press_militar",
                        "shoulders",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.press_militar,
                        new ArrayList<>()
                ));

                lista.add(new Rutina(
                        "elevaciones_laterales",
                        "shoulders",
                        "Intermedio",
                        "android.resource://" + getPackageName() + "/" + R.raw.elevaciones_laterales,
                        new ArrayList<>()
                ));
                break;

            case "abs":
                lista.add(new Rutina(
                        "crunch_polea",
                        "abs",
                        "Principiante",
                        "android.resource://" + getPackageName() + "/" + R.raw.crunch_polea,
                        new ArrayList<>()
                ));
                break;
        }

        return lista;
    }

    private void mostrarDialogoNuevaRutina() {
        View vista = LayoutInflater.from(this)
                .inflate(R.layout.dialogo_nuevo_ejercicio, null);

        EditText editNombre = vista.findViewById(R.id.editNombreEjercicio);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.añadir_ejercicio))
                .setView(vista)
                .setPositiveButton(getString(R.string.añadir), null)
                .setNegativeButton(getString(R.string.cancelar), null)
                .create();

        dialogo.setOnShowListener(dialog ->
                dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

                    String nombre = editNombre.getText().toString().trim();

                    if (nombre.isEmpty()) {
                        Toast.makeText(
                                this,
                                getString(R.string.introduce_nombre),
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    if (existeRutinaConNombre(nombre)) {
                        Toast.makeText(
                                this,
                                getString(R.string.toast_ejercicio_ya_existe),
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    Rutina nueva = new Rutina(
                            nombre,
                            codigoCategoria,
                            "Personalizada",
                            "",
                            new ArrayList<>()
                    );

                    listaRutinasActual.add(nueva);
                    adaptador.notifyItemInserted(listaRutinasActual.size() - 1);

                    guardarRutinasEnPrefs(codigoCategoria, listaRutinasActual);
                    guardarRutinasEnFirebase(codigoCategoria, listaRutinasActual);

                    Toast.makeText(
                            this,
                            getString(R.string.ejercicio_anadido),
                            Toast.LENGTH_SHORT
                    ).show();

                    dialogo.dismiss();
                })
        );

        dialogo.show();
    }

    private boolean existeRutinaConNombre(String nombreNuevo) {
        String nuevoNormalizado = normalizarTexto(nombreNuevo);

        for (Rutina rutina : listaRutinasActual) {
            if (rutina == null || rutina.getNombre() == null) continue;

            String nombreGuardado = rutina.getNombre();
            String nombreVisible = obtenerNombreVisibleRutina(nombreGuardado);

            if (normalizarTexto(nombreGuardado).equals(nuevoNormalizado) ||
                    normalizarTexto(nombreVisible).equals(nuevoNormalizado)) {
                return true;
            }
        }

        return false;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";

        String resultado = texto.trim().toLowerCase(Locale.ROOT);

        resultado = Normalizer.normalize(resultado, Normalizer.Form.NFD);
        resultado = resultado.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        return resultado;
    }

    private String obtenerNombreVisibleRutina(String codigoEjercicio) {
        if (codigoEjercicio == null) {
            return "";
        }

        switch (codigoEjercicio) {
            case "curl_barra":
                return getString(R.string.curl_barra);

            case "curl_martillo":
                return getString(R.string.curl_martillo);

            case "press_banca":
                return getString(R.string.press_banca);

            case "press_inclinado":
                return getString(R.string.press_inclinado);

            case "dominadas":
                return getString(R.string.dominadas);

            case "remo_mancuerna":
                return getString(R.string.remo_mancuerna);

            case "sentadilla_hack":
                return getString(R.string.sentadilla_hack);

            case "zancadas":
                return getString(R.string.zancadas);

            case "press_militar":
                return getString(R.string.press_militar);

            case "elevaciones_laterales":
                return getString(R.string.elevaciones_laterales);

            case "crunch_polea":
                return getString(R.string.crunch_polea);

            default:
                return codigoEjercicio;
        }
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

        EditText editSeriesObjetivo = vista.findViewById(R.id.editSeriesObjetivoRegistro);
        EditText editRepsObjetivo = vista.findViewById(R.id.editRepsObjetivoRegistro);

        EditText editSeries = vista.findViewById(R.id.editSeriesRealizadas);
        EditText editReps = vista.findViewById(R.id.editRepsPorSerie);
        EditText editPeso = vista.findViewById(R.id.editPesoUtilizado);

        Calendar calendario = Calendar.getInstance();

        editFecha.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendario.getTime()));
        editHoraInicio.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendario.getTime()));
        editHoraFin.setText(editHoraInicio.getText());

        ObjetivoRutina objetivoGuardado = obtenerObjetivoRutina(rutina);

        if (objetivoGuardado != null) {
            editSeriesObjetivo.setText(String.valueOf(objetivoGuardado.series));
            editRepsObjetivo.setText(String.valueOf(objetivoGuardado.repeticiones));
        }

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
                .setTitle(getString(R.string.registrar_realizacion) + ": " + obtenerNombreVisibleRutina(rutina.getNombre()))
                .setView(vista)
                .setPositiveButton(getString(R.string.guardar), (d, w) -> {

                    if (TextUtils.isEmpty(editSeriesObjetivo.getText()) ||
                            TextUtils.isEmpty(editRepsObjetivo.getText()) ||
                            TextUtils.isEmpty(editSeries.getText()) ||
                            TextUtils.isEmpty(editReps.getText()) ||
                            TextUtils.isEmpty(editPeso.getText())) {

                        Toast.makeText(this, getString(R.string.completa_campos), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int seriesObjetivo;
                    int repsObjetivo;
                    int seriesRealizadas;
                    int repsRealizadas;
                    double pesoUtilizado;

                    try {
                        seriesObjetivo = Integer.parseInt(editSeriesObjetivo.getText().toString().trim());
                        repsObjetivo = Integer.parseInt(editRepsObjetivo.getText().toString().trim());
                        seriesRealizadas = Integer.parseInt(editSeries.getText().toString().trim());
                        repsRealizadas = Integer.parseInt(editReps.getText().toString().trim());
                        pesoUtilizado = Double.parseDouble(editPeso.getText().toString().trim().replace(",", "."));
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, getString(R.string.valor_invalido), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (seriesObjetivo <= 0 || repsObjetivo <= 0 ||
                            seriesRealizadas <= 0 || repsRealizadas <= 0) {
                        Toast.makeText(this, getString(R.string.valor_invalido), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    guardarObjetivoEnRutina(rutina, seriesObjetivo, repsObjetivo);

                    RegistroRealizacionRutina registro = new RegistroRealizacionRutina(
                            rutina.getNombre(),
                            editFecha.getText().toString(),
                            editHoraInicio.getText().toString(),
                            editHoraFin.getText().toString(),
                            seriesRealizadas,
                            repsRealizadas,
                            pesoUtilizado
                    );

                    final int estadoProgreso = calcularEstadoProgreso(
                            seriesObjetivo,
                            repsObjetivo,
                            seriesRealizadas,
                            repsRealizadas
                    );

                    FireStoreManager.guardarRegistro(registro, new FireStoreManager.SaveCallback() {
                        @Override
                        public void onSuccess(@NonNull String documentId) {
                            String fechaEntrenamiento = editFecha.getText().toString();

                            FireStoreManager.marcarDiaEntrenado(
                                    fechaEntrenamiento,
                                    estadoProgreso
                            );

                            Toast.makeText(
                                    DetalleRutinaActivity.this,
                                    getString(R.string.sesion_guardada),
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                        @Override
                        public void onError(@NonNull Exception e) {
                            Toast.makeText(
                                    DetalleRutinaActivity.this,
                                    getString(R.string.toast_error_con_mensaje, e.getMessage()),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
                })
                .setNegativeButton(getString(R.string.cancelar), null)
                .show();
    }

    private void guardarObjetivoEnRutina(Rutina rutina, int seriesObjetivo, int repsObjetivo) {
        String objetivo = seriesObjetivo + "x" + repsObjetivo;

        if (rutina.getEjercicios() == null) {
            return;
        }

        rutina.getEjercicios().clear();
        rutina.getEjercicios().add(objetivo);

        guardarRutinasEnPrefs(codigoCategoria, listaRutinasActual);
        guardarRutinasEnFirebase(codigoCategoria, listaRutinasActual);

        adaptador.notifyDataSetChanged();
    }

    private int calcularEstadoProgreso(int seriesObjetivo, int repsObjetivo, int seriesRealizadas, int repsRealizadas) {
        int repsTotalesObjetivo = seriesObjetivo * repsObjetivo;
        int repsTotalesRealizadas = seriesRealizadas * repsRealizadas;

        boolean cumpleSeries = seriesRealizadas >= seriesObjetivo;
        boolean cumpleRepeticionesTotales = repsTotalesRealizadas >= repsTotalesObjetivo;

        if (cumpleSeries && cumpleRepeticionesTotales) {
            return DiaCalendario.ESTADO_CUMPLIDO;
        }

        return DiaCalendario.ESTADO_A_MEDIAS;
    }

    private ObjetivoRutina obtenerObjetivoRutina(Rutina rutina) {
        if (rutina == null || rutina.getEjercicios() == null) return null;

        Pattern patronObjetivo = Pattern.compile("(\\d+)\\s*[xX×]\\s*(\\d+)");

        for (String ejercicio : rutina.getEjercicios()) {
            if (ejercicio == null) continue;

            Matcher matcher = patronObjetivo.matcher(ejercicio);
            if (matcher.find()) {
                int seriesObjetivo = Integer.parseInt(matcher.group(1));
                int repsObjetivo = Integer.parseInt(matcher.group(2));

                if (seriesObjetivo > 0 && repsObjetivo > 0) {
                    return new ObjetivoRutina(seriesObjetivo, repsObjetivo);
                }
            }
        }

        return null;
    }

    private static class ObjetivoRutina {
        final int series;
        final int repeticiones;

        ObjetivoRutina(int series, int repeticiones) {
            this.series = series;
            this.repeticiones = repeticiones;
        }
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