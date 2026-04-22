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

import java.util.List;
import java.util.Locale;

public class RutinaAdapter extends RecyclerView.Adapter<RutinaAdapter.ViewHolder> {

    private List<Rutina> listaRutinas;
    private Context context;
    private OnRutinaClickListener clickListener;
    private OnRutinaLongClickListener longClickListener;


    private static final int COLOR_BICEPS = Color.parseColor("#FFB6C1");
    private static final int COLOR_PECHO = Color.parseColor("#FFA07A");
    private static final int COLOR_ESPALDA = Color.parseColor("#20B2AA");
    private static final int COLOR_PIERNAS = Color.parseColor("#9370DB");
    private static final int COLOR_HOMBROS = Color.parseColor("#FFD700");
    private static final int COLOR_ABDOMEN = Color.parseColor("#98FB98");
    private static final int COLOR_DEFAULT = Color.parseColor("#D3D3D3");


    public interface OnRutinaClickListener { void onRutinaClick(Rutina rutina); }
    public interface OnRutinaLongClickListener { void onRutinaLongClick(int position); }

    public RutinaAdapter(Context context, List<Rutina> listaRutinas,
                         OnRutinaClickListener clickListener,
                         OnRutinaLongClickListener longClickListener) {
        this.context = context;
        this.listaRutinas = listaRutinas;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_rutina, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rutina rutina = listaRutinas.get(position);

        String idioma = Locale.getDefault().getLanguage();

        holder.tvNombre.setText(traducirNombreRutina(rutina.getNombre()));

        holder.tvCategoria.setText(traducirCategoria(rutina.getObjetivo(), idioma));

        holder.itemView.setBackgroundColor(obtenerColorPorCategoria(rutina.getObjetivo()));

        holder.btnInfo.setOnClickListener(v -> {
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

    private int obtenerColorPorCategoria(String categoria) {
        if (categoria == null) return COLOR_DEFAULT;
        switch (categoria) {
            case "biceps": return COLOR_BICEPS;
            case "chest": return COLOR_PECHO;
            case "back": return COLOR_ESPALDA;
            case "legs": return COLOR_PIERNAS;
            case "shoulders": return COLOR_HOMBROS;
            case "abs": return COLOR_ABDOMEN;
            default: return COLOR_DEFAULT;
        }
    }

    private String traducirCategoria(String categoria, String idioma) {
        if (categoria == null) return idioma.equals("es") ? "Desconocida" : "Unknown";
        switch (categoria) {
            case "biceps": return idioma.equals("es") ? "Bíceps" : "Biceps";
            case "chest": return idioma.equals("es") ? "Pecho" : "Chest";
            case "back": return idioma.equals("es") ? "Espalda" : "Back";
            case "legs": return idioma.equals("es") ? "Piernas" : "Legs";
            case "shoulders": return idioma.equals("es") ? "Hombros" : "Shoulders";
            case "abs": return idioma.equals("es") ? "Abdomen" : "Abs";
            default: return idioma.equals("es") ? "Desconocida" : "Unknown";
        }
    }


    private String traducirNombreRutina(String codigoEjercicio) {
        switch (codigoEjercicio) {
            case "curl_barra": return context.getString(R.string.curl_barra);
            case "curl_martillo": return context.getString(R.string.curl_martillo);
            case "press_banca": return context.getString(R.string.press_banca);
            case "press_inclinado": return context.getString(R.string.press_inclinado);
            case "dominadas": return context.getString(R.string.dominadas);
            case "remo_mancuerna": return context.getString(R.string.remo_mancuerna);
            case "sentadilla_hack": return context.getString(R.string.sentadilla_hack);
            case "zancadas": return context.getString(R.string.zancadas);
            case "press_militar": return context.getString(R.string.press_militar);
            case "elevaciones_laterales": return context.getString(R.string.elevaciones_laterales);
            case "crunch_polea": return context.getString(R.string.crunch_polea);
            default: return codigoEjercicio; // personalizado
        }
    }

    @Override
    public int getItemCount() { return listaRutinas.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
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