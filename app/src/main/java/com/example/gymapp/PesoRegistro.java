package com.example.gymapp;

import java.util.Date;

public class PesoRegistro {
    private double peso;
    private Date fecha;

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

    @Override
    public String toString() {
        return "Peso: " + peso + " kg - Fecha: " + fecha.toString();
    }
}