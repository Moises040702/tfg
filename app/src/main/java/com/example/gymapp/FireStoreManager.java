package com.example.gymapp;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class FireStoreManager {

    @SuppressLint("StaticFieldLeak")
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();


    public interface SaveCallback {
        void onSuccess(@NonNull String documentId);
        void onError(@NonNull Exception e);
    }

    public interface ListCallback {
        void onSuccess(@NonNull List<RegistroRealizacionRutina> registros);
        void onError(@NonNull Exception e);
    }

    public interface PesoCallback {
        void onSuccess(List<PesoRegistro> lista);
        void onError(Exception e);
    }

    public interface ProgresoCallback {
        void onSuccess(HashMap<String, Integer> mapa);
        void onError(Exception e);
    }

    private static String getUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Usuario no autenticado");
        }
        return user.getUid();
    }




    public static void guardarRegistro(RegistroRealizacionRutina registro, SaveCallback callback) {
        String uid = getUid();
        db.collection("users")
                .document(uid)
                .collection("historial_rutinas")
                .add(registro)
                .addOnSuccessListener(doc -> {
                    if (callback != null) callback.onSuccess(doc.getId());
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    public static void obtenerTodosLosRegistros(ListCallback callback) {
        String uid = getUid();
        db.collection("users")
                .document(uid)
                .collection("historial_rutinas")
                .get()
                .addOnSuccessListener(query -> {
                    List<RegistroRealizacionRutina> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        RegistroRealizacionRutina r = doc.toObject(RegistroRealizacionRutina.class);
                        lista.add(r);
                    }
                    callback.onSuccess(lista);
                })
                .addOnFailureListener(callback::onError);
    }


    public static void marcarDiaEntrenado(String fecha, int estado) {
        String uid = getUid();

        db.collection("users")
                .document(uid)
                .collection("progreso")
                .document(fecha)
                .get()
                .addOnSuccessListener(doc -> {
                    int estadoAGuardar = estado;
                    Long estadoActual = doc.getLong("estado");

                    if (estadoActual != null) {
                        estadoAGuardar = resolverEstadoProgreso(estadoActual.intValue(), estado);
                    }

                    guardarEstadoDia(uid, fecha, estadoAGuardar);
                })
                .addOnFailureListener(e -> guardarEstadoDia(uid, fecha, estado));
    }

    private static void guardarEstadoDia(String uid, String fecha, int estado) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("fecha", fecha);
        data.put("estado", estado);

        db.collection("users")
                .document(uid)
                .collection("progreso")
                .document(fecha)
                .set(data);
    }

    private static int resolverEstadoProgreso(int estadoActual, int estadoNuevo) {
        if (estadoActual == DiaCalendario.ESTADO_CUMPLIDO ||
                estadoNuevo == DiaCalendario.ESTADO_CUMPLIDO) {
            return DiaCalendario.ESTADO_CUMPLIDO;
        }

        if (estadoActual == DiaCalendario.ESTADO_A_MEDIAS ||
                estadoNuevo == DiaCalendario.ESTADO_A_MEDIAS) {
            return DiaCalendario.ESTADO_A_MEDIAS;
        }

        return estadoNuevo;
    }

    public static void obtenerProgreso(ProgresoCallback callback) {
        String uid = getUid();
        db.collection("users")
                .document(uid)
                .collection("progreso")
                .get()
                .addOnSuccessListener(query -> {
                    HashMap<String, Integer> mapa = new HashMap<>();
                    for (QueryDocumentSnapshot doc : query) {
                        String fecha = doc.getString("fecha");
                        Long estadoLong = doc.getLong("estado");
                        if (fecha != null && estadoLong != null) {
                            mapa.put(fecha, estadoLong.intValue());
                        }
                    }
                    callback.onSuccess(mapa);
                })
                .addOnFailureListener(callback::onError);
    }


    public static void guardarPeso(double peso) {
        String uid = getUid();
        HashMap<String, Object> data = new HashMap<>();
        data.put("peso", peso);
        data.put("fecha", new Date());
        db.collection("users")
                .document(uid)
                .collection("pesos")
                .add(data);
    }

    public static void obtenerPesos(PesoCallback callback) {
        String uid = getUid();
        db.collection("users")
                .document(uid)
                .collection("pesos")
                .get()
                .addOnSuccessListener(query -> {
                    List<PesoRegistro> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Double peso = doc.getDouble("peso");
                        Date fecha = doc.getDate("fecha");
                        if (peso != null && fecha != null) {
                            lista.add(new PesoRegistro(peso, fecha));
                        }
                    }
                    callback.onSuccess(lista);
                })
                .addOnFailureListener(callback::onError);
    }
}