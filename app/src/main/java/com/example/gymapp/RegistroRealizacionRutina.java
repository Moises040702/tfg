package com.example.gymapp;

public class RegistroRealizacionRutina {

    private String nombreRutina;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private int series;
    private int repeticiones;
    private double peso;
    private double volumen;

    public RegistroRealizacionRutina() { }

    public RegistroRealizacionRutina(String nombreRutina,
                                     String fecha,
                                     String horaInicio,
                                     String horaFin,
                                     int series,
                                     int repeticiones,
                                     double peso) {
        this.nombreRutina = nombreRutina;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.series = series;
        this.repeticiones = repeticiones;
        this.peso = peso;
        this.volumen = series * repeticiones * peso;
    }

    public String getFecha() { return fecha; }
    public int getSeries() { return series; }
    public int getRepeticiones() { return repeticiones; }
    public double getPeso() { return peso; }

    // 🔥 IMPORTANTE
    public String getHoraInicio() { return horaInicio; }
    public String getHoraFin() { return horaFin; }
}