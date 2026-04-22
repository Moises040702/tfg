package com.example.gymapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.ViewHolder> {

    private final List<String> listaCategorias;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String categoriaCodigo);
    }


    public CategoriaAdapter(List<String> listaCategorias, OnItemClickListener listener) {
        this.listaCategorias = listaCategorias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String codigo = listaCategorias.get(position);


        Context context = holder.itemView.getContext();
        SharedPreferences prefs = context.getSharedPreferences("Ajustes", Context.MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "es");

        holder.txtCategoria.setText(traducirCategoria(codigo, idioma));

        switch (codigo) {
            case "biceps":
                holder.cardView.setBackgroundResource(R.drawable.fondo_biceps);
                break;
            case "chest":
                holder.cardView.setBackgroundResource(R.drawable.fondo_pecho);
                break;
            case "back":
                holder.cardView.setBackgroundResource(R.drawable.fondo_espalda);
                break;
            case "legs":
                holder.cardView.setBackgroundResource(R.drawable.fondo_piernas);
                break;
            case "shoulders":
                holder.cardView.setBackgroundResource(R.drawable.fondo_hombros);
                break;
            case "abs":
                holder.cardView.setBackgroundResource(R.drawable.fondo_abdomen);
                break;
            default:
                holder.cardView.setBackgroundResource(R.drawable.fondo_generico);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(codigo));
    }

    private String traducirCategoria(String codigo, String idioma) {
        if (codigo == null) return idioma.equals("es") ? "Desconocida" : "Unknown";
        switch (codigo) {
            case "biceps":
                return idioma.equals("es") ? "Bíceps" : "Biceps";
            case "chest":
                return idioma.equals("es") ? "Pecho" : "Chest";
            case "back":
                return idioma.equals("es") ? "Espalda" : "Back";
            case "legs":
                return idioma.equals("es") ? "Piernas" : "Legs";
            case "shoulders":
                return idioma.equals("es") ? "Hombros" : "Shoulders";
            case "abs":
                return idioma.equals("es") ? "Abdomen" : "Abs";
            default:
                return idioma.equals("es") ? "Desconocida" : "Unknown";
        }
    }

    @Override
    public int getItemCount() {
        return listaCategorias.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoria;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoria = itemView.findViewById(R.id.txtCategoria);
            cardView = itemView.findViewById(R.id.cardCategoria);
        }
    }
}