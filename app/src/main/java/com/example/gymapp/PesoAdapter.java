package com.example.gymapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PesoAdapter extends RecyclerView.Adapter<PesoAdapter.ViewHolder> {

    private final List<PesoRegistro> registros;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public PesoAdapter(List<PesoRegistro> registros) {
        this.registros = registros;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PesoRegistro registro = registros.get(position);

        holder.tvPeso.setText(
                String.format(
                        Locale.getDefault(),
                        holder.itemView.getContext().getString(R.string.formato_peso),
                        registro.getPeso()
                )
        );

        holder.tvFecha.setText(dateFormat.format(registro.getFecha()));
    }

    @Override
    public int getItemCount() {
        return registros.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPeso;
        TextView tvFecha;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPeso = itemView.findViewById(android.R.id.text1);
            tvFecha = itemView.findViewById(android.R.id.text2);
        }
    }
}