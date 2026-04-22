package com.example.gymapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordatorioWorker extends Worker {

    private static final String CHANNEL_ID = "recordatorio_rutinas";
    private static final String PREFS_AJUSTES = "Ajustes";
    private static final int NOTIF_ID = 1;

    private static final String KEY_ULTIMA_NOTIF_MS = "ultima_notificacion_ms";
    private static final long INTERVALO_MS = 3L * 60L * 60L * 1000L;

    public RecordatorioWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        mostrarNotificacionSiToca(context, "WORKER");
        return Result.success();
    }

    public static void mostrarNotificacionSiToca(Context context, String origen) {
        SharedPreferences ajustes = context.getSharedPreferences(PREFS_AJUSTES, Context.MODE_PRIVATE);

        boolean notificacionesActivadas = ajustes.getBoolean("notificaciones", true);
        boolean recordatoriosActivados = ajustes.getBoolean("recordatorios", false);

        if (!notificacionesActivadas || !recordatoriosActivados) return;

        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String ultimaFecha = ajustes.getString("fecha_rutina", "");

        if (!fechaHoy.equals(ultimaFecha)) {
            ajustes.edit()
                    .putString("fecha_rutina", fechaHoy)
                    .putBoolean("rutina_hecha", false)
                    .apply();
        }

        boolean rutinaHecha = ajustes.getBoolean("rutina_hecha", false);
        if (rutinaHecha) return;

        long ahora = System.currentTimeMillis();
        long ultimaNotif = ajustes.getLong(KEY_ULTIMA_NOTIF_MS, 0);

        if (ultimaNotif != 0 && (ahora - ultimaNotif) < INTERVALO_MS) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= 33 &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        crearCanalNotificacion(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.nexafit)
                .setContentTitle("Recordatorio de rutina")
                .setContentText("Aún no has hecho tu rutina de hoy. ¡Vamos a entrenar!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(NOTIF_ID, builder.build());

        ajustes.edit().putLong(KEY_ULTIMA_NOTIF_MS, ahora).apply();
    }

    private static void crearCanalNotificacion(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recordatorios Rutinas",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Canal para recordatorios de rutinas no hechas");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) notificationManager.createNotificationChannel(channel);
        }
    }
}