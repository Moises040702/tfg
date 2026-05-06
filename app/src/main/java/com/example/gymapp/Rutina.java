package com.example.gymapp;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Rutina {
    private final String id;
    private final String nombre;
    private final String objetivo;
    private final String nivel;
    private final String videoUrl;
    private Date fechaCreacion;
    private final List<String> ejercicios;
    private boolean completada;

    public Rutina(String nombre, String objetivo, String nivel, String videoUrl, List<String> ejercicios) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.objetivo = objetivo;
        this.nivel = nivel;
        this.videoUrl = videoUrl;
        this.fechaCreacion = new Date();
        this.ejercicios = new ArrayList<>(ejercicios);
        this.completada = false;
    }

    public String getId() { return id; }

    public String getNombre() { return nombre; }

    public String getObjetivo() { return objetivo; }

    public String getVideoUrl() {
        return videoUrl;
    }

    public List<String> getEjercicios() { return ejercicios; }

    @NonNull
    @Override
    public String toString() {
        return nombre + " (" + nivel + ") - " + objetivo;
    }
}