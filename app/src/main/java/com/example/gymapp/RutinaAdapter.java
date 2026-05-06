package com.example.gymapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class RutinaAdapter extends RecyclerView.Adapter<RutinaAdapter.ViewHolder> {

    private final List<Rutina> listaRutinas;
    private final Context context;
    private final OnRutinaClickListener clickListener;
    private final OnRutinaLongClickListener longClickListener;

    private static final int COLOR_BICEPS = Color.parseColor("#FFB6C1");
    private static final int COLOR_PECHO = Color.parseColor("#FFA07A");
    private static final int COLOR_ESPALDA = Color.parseColor("#20B2AA");
    private static final int COLOR_PIERNAS = Color.parseColor("#9370DB");
    private static final int COLOR_HOMBROS = Color.parseColor("#FFD700");
    private static final int COLOR_ABDOMEN = Color.parseColor("#98FB98");
    private static final int COLOR_DEFAULT = Color.parseColor("#D3D3D3");

    public interface OnRutinaClickListener {
        void onRutinaClick(Rutina rutina);
    }

    public interface OnRutinaLongClickListener {
        void onRutinaLongClick(int position);
    }

    public RutinaAdapter(Context context,
                         List<Rutina> listaRutinas,
                         OnRutinaClickListener clickListener,
                         OnRutinaLongClickListener longClickListener) {
        this.context = context;
        this.listaRutinas = listaRutinas;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public RutinaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_rutina, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutinaAdapter.ViewHolder holder, int position) {
        Rutina rutina = listaRutinas.get(position);

        holder.tvNombre.setText(traducirNombreRutina(rutina.getNombre()));

        String categoriaNormalizada = normalizarCategoria(rutina.getObjetivo());

        holder.tvCategoria.setText(traducirCategoria(categoriaNormalizada));

        holder.itemView.setBackgroundColor(obtenerColorPorCategoria(categoriaNormalizada));

        holder.btnInfo.setOnClickListener(v -> {
            if (rutina.getVideoUrl() == null || rutina.getVideoUrl().trim().isEmpty()) {
                return;
            }

            Intent intent = new Intent(context, VideoRutinaActivity.class);
            intent.putExtra("VIDEO_URL", rutina.getVideoUrl());

            if (!(context instanceof android.app.Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            context.startActivity(intent);
        });

        holder.btnAccion.setOnClickListener(v -> clickListener.onRutinaClick(rutina));

        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onRutinaLongClick(position);
            return true;
        });
    }

    private String normalizarCategoria(String categoria) {
        if (categoria == null) return "";

        String texto = categoria.trim().toLowerCase(Locale.ROOT);

        texto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        texto = texto.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        switch (texto) {
            case "biceps":
                return "biceps";

            case "chest":
            case "pecho":
                return "chest";

            case "back":
            case "espalda":
                return "back";

            case "legs":
            case "piernas":
                return "legs";

            case "shoulders":
            case "hombros":
                return "shoulders";

            case "abs":
            case "abdomen":
            case "abdominales":
                return "abs";

            default:
                return texto;
        }
    }

    private int obtenerColorPorCategoria(String categoria) {
        if (categoria == null) return COLOR_DEFAULT;

        switch (categoria) {
            case "biceps":
                return COLOR_BICEPS;

            case "chest":
                return COLOR_PECHO;

            case "back":
                return COLOR_ESPALDA;

            case "legs":
                return COLOR_PIERNAS;

            case "shoulders":
                return COLOR_HOMBROS;

            case "abs":
                return COLOR_ABDOMEN;

            default:
                return COLOR_DEFAULT;
        }
    }

    private String traducirCategoria(String categoria) {
        if (categoria == null) {
            return context.getString(R.string.categoria_desconocida);
        }

        switch (categoria) {
            case "biceps":
                return context.getString(R.string.biceps);

            case "chest":
                return context.getString(R.string.pecho);

            case "back":
                return context.getString(R.string.espalda);

            case "legs":
                return context.getString(R.string.piernas);

            case "shoulders":
                return context.getString(R.string.hombros);

            case "abs":
                return context.getString(R.string.abdomen);

            default:
                return context.getString(R.string.categoria_desconocida);
        }
    }

    private String traducirNombreRutina(String codigoEjercicio) {
        if (codigoEjercicio == null) {
            return context.getString(R.string.rutina_desconocida);
        }

        switch (codigoEjercicio) {
            case "curl_barra":
                return context.getString(R.string.curl_barra);

            case "curl_martillo":
                return context.getString(R.string.curl_martillo);

            case "press_banca":
                return context.getString(R.string.press_banca);

            case "press_inclinado":
                return context.getString(R.string.press_inclinado);

            case "dominadas":
                return context.getString(R.string.dominadas);

            case "remo_mancuerna":
                return context.getString(R.string.remo_mancuerna);

            case "sentadilla_hack":
                return context.getString(R.string.sentadilla_hack);

            case "zancadas":
                return context.getString(R.string.zancadas);

            case "press_militar":
                return context.getString(R.string.press_militar);

            case "elevaciones_laterales":
                return context.getString(R.string.elevaciones_laterales);

            case "crunch_polea":
                return context.getString(R.string.crunch_polea);

            default:
                return codigoEjercicio;
        }
    }

    @Override
    public int getItemCount() {
        return listaRutinas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombre, tvCategoria;
        ImageButton btnInfo, btnAccion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNombre = itemView.findViewById(R.id.tvNombreRutina);
            tvCategoria = itemView.findViewById(R.id.tvObjetivoRutina);
            btnInfo = itemView.findViewById(R.id.btnInfoRutina);
            btnAccion = itemView.findViewById(R.id.btnAccionRutina);
        }
    }
}