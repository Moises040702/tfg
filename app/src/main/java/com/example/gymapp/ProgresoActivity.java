package com.example.gymapp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class ProgresoActivity extends BaseActivity {

    private RecyclerView recyclerCalendario;
    private TextView txtMesAnio;
    private ImageButton btnMesAnterior, btnMesSiguiente, btnVolver;

    private CalendarioProgresoAdapter adapter;
    private ArrayList<DiaCalendario> diasMes = new ArrayList<>();

    private Calendar mesActual;
    private HashMap<String, Integer> progresoFirestore = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        boolean oscuro = getSharedPreferences("Ajustes", MODE_PRIVATE)
                .getBoolean("modo_oscuro", false);

        AppCompatDelegate.setDefaultNightMode(
                oscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progreso);

        recyclerCalendario = findViewById(R.id.recyclerCalendario);
        txtMesAnio = findViewById(R.id.txtMesAnio);
        btnMesAnterior = findViewById(R.id.btnMesAnterior);
        btnMesSiguiente = findViewById(R.id.btnMesSiguiente);
        btnVolver = findViewById(R.id.btnVolver);

        mesActual = Calendar.getInstance();

        recyclerCalendario.setLayoutManager(new GridLayoutManager(this, 7));

        adapter = new CalendarioProgresoAdapter(diasMes, (position, dia) -> {});
        recyclerCalendario.setAdapter(adapter);

        btnMesAnterior.setOnClickListener(v -> {
            mesActual.add(Calendar.MONTH, -1);
            generarCalendario();
        });

        btnMesSiguiente.setOnClickListener(v -> {
            mesActual.add(Calendar.MONTH, 1);
            generarCalendario();
        });

        btnVolver.setOnClickListener(v -> finish());

        cargarProgresoYGenerarCalendario();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarProgresoYGenerarCalendario();
    }

    private void cargarProgresoYGenerarCalendario() {
        FireStoreManager.obtenerProgreso(new FireStoreManager.ProgresoCallback() {
            @Override
            public void onSuccess(HashMap<String, Integer> mapa) {
                progresoFirestore = mapa;
                generarCalendario();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                generarCalendario();
            }
        });
    }

    private void generarCalendario() {
        diasMes.clear();

        Calendar cal = (Calendar) mesActual.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        Locale locale = Locale.getDefault();
        SimpleDateFormat formatoTitulo = new SimpleDateFormat("MMMM yyyy", locale);
        txtMesAnio.setText(capitalizar(formatoTitulo.format(cal.getTime()), locale));

        int diaSemana = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (diaSemana == Calendar.SUNDAY) ? 6 : (diaSemana - Calendar.MONDAY);

        for (int i = 0; i < offset; i++) {
            diasMes.add(DiaCalendario.vacio());
        }

        int maxDiasMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int dia = 1; dia <= maxDiasMes; dia++) {
            Calendar c = (Calendar) mesActual.clone();
            c.set(Calendar.DAY_OF_MONTH, dia);
            ponerInicioDelDia(c);

            String fechaKey = keyFormat.format(c.getTime());
            int estado = obtenerEstadoEfectivoDia(c, fechaKey);

            diasMes.add(new DiaCalendario(dia, estado, true, fechaKey));
        }

        while (diasMes.size() % 7 != 0) {
            diasMes.add(DiaCalendario.vacio());
        }

        adapter.notifyDataSetChanged();
    }

    private int obtenerEstadoEfectivoDia(Calendar fechaDia, String fechaKey) {
        if (progresoFirestore.containsKey(fechaKey)) {
            return progresoFirestore.get(fechaKey);
        }

        Calendar hoy = Calendar.getInstance();
        ponerInicioDelDia(hoy);

        if (fechaDia.before(hoy)) return DiaCalendario.ESTADO_NO_FUI;
        else return DiaCalendario.ESTADO_PENDIENTE;
    }

    private void ponerInicioDelDia(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    private String capitalizar(String s, Locale locale) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(locale) + s.substring(1);
    }
}