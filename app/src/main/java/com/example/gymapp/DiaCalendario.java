package com.example.gymapp;

public class DiaCalendario {

    public static final int ESTADO_PENDIENTE = 0;
    public static final int ESTADO_A_MEDIAS = 1;
    public static final int ESTADO_CUMPLIDO = 2;
    public static final int ESTADO_NO_FUI = 3;

    private final int numeroDia;
    private final int estado;
    private final boolean delMesActual;

    public DiaCalendario(int numeroDia, int estado, boolean delMesActual) {
        this.numeroDia = numeroDia;
        this.estado = estado;
        this.delMesActual = delMesActual;
    }

    public static DiaCalendario vacio() {
        return new DiaCalendario(0, ESTADO_PENDIENTE, false);
    }

    public int getNumeroDia() {
        return numeroDia;
    }
    public int getEstado() {
        return estado;
    }
    public boolean esDelMesActual() {
        return delMesActual;
    }

}