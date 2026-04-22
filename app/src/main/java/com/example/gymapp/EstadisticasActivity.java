package com.example.gymapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.List;

public class EstadisticasActivity extends BaseActivity {

    private TextView tvHoySeries, tvHoyReps, tvHoyVolumen, tvHoySesiones, tvHoyTiempo;
    private TextView tvSemanaSeries, tvSemanaReps, tvSemanaVolumen, tvSemanaSesiones, tvSemanaTiempo;
    private TextView tvMesSeries, tvMesReps, tvMesVolumen, tvMesSesiones, tvMesTiempo;
    private TextView tvTotalSeries, tvTotalReps, tvTotalVolumen, tvTotalSesiones, tvTotalTiempo;

    private String idiomaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        SharedPreferences prefs = getSharedPreferences("Ajustes", MODE_PRIVATE);
        idiomaActual = prefs.getString("idioma", "es");

        ImageButton btnVolver = findViewById(R.id.btnVolverEstadisticas);
        btnVolver.setOnClickListener(v -> finish());

        // HOY
        tvHoySeries = findViewById(R.id.tvHoySeries);
        tvHoyReps = findViewById(R.id.tvHoyReps);
        tvHoyVolumen = findViewById(R.id.tvHoyVolumen);
        tvHoySesiones = findViewById(R.id.tvHoySesiones);
        tvHoyTiempo = findViewById(R.id.tvHoyTiempo);

        // SEMANA
        tvSemanaSeries = findViewById(R.id.tvSemanaSeries);
        tvSemanaReps = findViewById(R.id.tvSemanaReps);
        tvSemanaVolumen = findViewById(R.id.tvSemanaVolumen);
        tvSemanaSesiones = findViewById(R.id.tvSemanaSesiones);
        tvSemanaTiempo = findViewById(R.id.tvSemanaTiempo);

        // MES
        tvMesSeries = findViewById(R.id.tvMesSeries);
        tvMesReps = findViewById(R.id.tvMesReps);
        tvMesVolumen = findViewById(R.id.tvMesVolumen);
        tvMesSesiones = findViewById(R.id.tvMesSesiones);
        tvMesTiempo = findViewById(R.id.tvMesTiempo);

        // TOTAL
        tvTotalSeries = findViewById(R.id.tvTotalSeries);
        tvTotalReps = findViewById(R.id.tvTotalReps);
        tvTotalVolumen = findViewById(R.id.tvTotalVolumen);
        tvTotalSesiones = findViewById(R.id.tvTotalSesiones);
        tvTotalTiempo = findViewById(R.id.tvTotalTiempo);

        cargarEstadisticas();
    }

    private void cargarEstadisticas() {

        FireStoreManager.obtenerTodosLosRegistros(new FireStoreManager.ListCallback() {

            @Override
            public void onSuccess(@NonNull List<RegistroRealizacionRutina> registros) {

                int hoySeries = 0, semanaSeries = 0, mesSeries = 0, totalSeries = 0;
                int hoyReps = 0, semanaReps = 0, mesReps = 0, totalReps = 0;

                double hoyVol = 0, semanaVol = 0, mesVol = 0, totalVol = 0;

                int hoySesiones = 0, semanaSesiones = 0, mesSesiones = 0, totalSesiones = 0;

                int hoyTiempo = 0, semanaTiempo = 0, mesTiempo = 0, totalTiempo = 0;

                Calendar hoy = Calendar.getInstance();

                for (RegistroRealizacionRutina r : registros) {

                    // DEBUG (puedes quitar luego)
                    System.out.println("Registro: " + r.getFecha() + " | " + r.getHoraInicio() + " - " + r.getHoraFin());

                    int series = r.getSeries();
                    int reps = r.getRepeticiones();
                    double peso = r.getPeso();

                    double volumen = peso * reps * series;
                    int minutos = calcularMinutos(r.getHoraInicio(), r.getHoraFin());

                    Calendar fecha = parseFecha(r.getFecha());

                    // TOTAL
                    totalSeries += series;
                    totalReps += reps * series;
                    totalVol += volumen;
                    totalSesiones++;
                    totalTiempo += minutos;

                    // HOY
                    if (mismaFecha(hoy, fecha)) {
                        hoySeries += series;
                        hoyReps += reps * series;
                        hoyVol += volumen;
                        hoySesiones++;
                        hoyTiempo += minutos;
                    }

                    // SEMANA
                    if (mismaSemana(hoy, fecha)) {
                        semanaSeries += series;
                        semanaReps += reps * series;
                        semanaVol += volumen;
                        semanaSesiones++;
                        semanaTiempo += minutos;
                    }

                    // MES
                    if (mismoMes(hoy, fecha)) {
                        mesSeries += series;
                        mesReps += reps * series;
                        mesVol += volumen;
                        mesSesiones++;
                        mesTiempo += minutos;
                    }
                }

                // MOSTRAR
                tvHoySeries.setText(String.valueOf(hoySeries));
                tvHoyReps.setText(String.valueOf(hoyReps));
                tvHoyVolumen.setText(hoyVol + " kg");
                tvHoySesiones.setText(String.valueOf(hoySesiones));
                tvHoyTiempo.setText(hoyTiempo + " min");

                tvSemanaSeries.setText(String.valueOf(semanaSeries));
                tvSemanaReps.setText(String.valueOf(semanaReps));
                tvSemanaVolumen.setText(semanaVol + " kg");
                tvSemanaSesiones.setText(String.valueOf(semanaSesiones));
                tvSemanaTiempo.setText(semanaTiempo + " min");

                tvMesSeries.setText(String.valueOf(mesSeries));
                tvMesReps.setText(String.valueOf(mesReps));
                tvMesVolumen.setText(mesVol + " kg");
                tvMesSesiones.setText(String.valueOf(mesSesiones));
                tvMesTiempo.setText(mesTiempo + " min");

                tvTotalSeries.setText(String.valueOf(totalSeries));
                tvTotalReps.setText(String.valueOf(totalReps));
                tvTotalVolumen.setText(totalVol + " kg");
                tvTotalSesiones.setText(String.valueOf(totalSesiones));
                tvTotalTiempo.setText(totalTiempo + " min");
            }

            @Override
            public void onError(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    // 🔥 MÉTODO CLAVE (ARREGLADO)
    private int calcularMinutos(String inicio, String fin) {

        if (inicio == null || fin == null || inicio.isEmpty() || fin.isEmpty()) {
            return 0;
        }

        try {
            String[] h1 = inicio.split(":");
            String[] h2 = fin.split(":");

            if (h1.length < 2 || h2.length < 2) return 0;

            int minInicio = Integer.parseInt(h1[0]) * 60 + Integer.parseInt(h1[1]);
            int minFin = Integer.parseInt(h2[0]) * 60 + Integer.parseInt(h2[1]);

            if (minFin < minInicio) return 0;

            return minFin - minInicio;

        } catch (Exception e) {
            return 0;
        }
    }

    private Calendar parseFecha(String fecha) {

        Calendar c = Calendar.getInstance();

        try {
            String[] p = fecha.split("-");

            c.set(Calendar.YEAR, Integer.parseInt(p[0]));
            c.set(Calendar.MONTH, Integer.parseInt(p[1]) - 1);
            c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(p[2]));

        } catch (Exception e) {}

        return c;
    }

    private boolean mismaFecha(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private boolean mismaSemana(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.WEEK_OF_YEAR) == b.get(Calendar.WEEK_OF_YEAR);
    }

    private boolean mismoMes(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.MONTH) == b.get(Calendar.MONTH);
    }
}