package com.example.gymapp;

import androidx.annotation.NonNull;

import java.util.Date;

public class PesoRegistro {
    private final double peso;
    private final Date fecha;

    public PesoRegistro(double peso, Date fecha) {
        this.peso = peso;
        this.fecha = fecha;
    }

    public double getPeso() {
        return peso;
    }

    public Date getFecha() {
        return fecha;
    }

    @NonNull
    @Override
    public String toString() {
        return "Peso: " + peso + " kg - Fecha: " + fecha.toString();
    }
}