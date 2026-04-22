package com.example.gymapp;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalendarioProgresoAdapter extends RecyclerView.Adapter<CalendarioProgresoAdapter.DiaViewHolder> {

    public interface OnDiaClickListener {
        void onDiaClick(int position, DiaCalendario dia);
    }

    private final List<DiaCalendario> dias;
    private final OnDiaClickListener listener;

    public CalendarioProgresoAdapter(List<DiaCalendario> dias, OnDiaClickListener listener) {
        this.dias = dias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView tv = new TextView(parent.getContext());

        int height = (int) (72 * parent.getContext().getResources().getDisplayMetrics().density);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
        );
        params.setMargins(8, 8, 8, 8);
        tv.setLayoutParams(params);

        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(18f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);

        return new DiaViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaViewHolder holder, int position) {
        DiaCalendario dia = dias.get(position);
        TextView tv = holder.textView;

        if (!dia.esDelMesActual() || dia.getNumeroDia() <= 0) {
            tv.setText("");
            tv.setBackground(null);
            tv.setClickable(false);
            tv.setAlpha(0.12f);
            return;
        }

        tv.setAlpha(1f);
        tv.setClickable(true);
        tv.setText(String.valueOf(dia.getNumeroDia()));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(24f);
        bg.setStroke(2, Color.parseColor("#33FFFFFF"));

        switch (dia.getEstado()) {
            case DiaCalendario.ESTADO_CUMPLIDO:
                bg.setColor(Color.parseColor("#2E7D32"));
                tv.setTextColor(Color.WHITE);
                break;
            case DiaCalendario.ESTADO_A_MEDIAS:
                bg.setColor(Color.parseColor("#FB8C00"));
                tv.setTextColor(Color.WHITE);
                break;
            case DiaCalendario.ESTADO_NO_FUI:
                bg.setColor(Color.parseColor("#C62828"));
                tv.setTextColor(Color.WHITE);
                break;
            case DiaCalendario.ESTADO_PENDIENTE:
            default:
                bg.setColor(Color.TRANSPARENT);
                tv.setTextColor(Color.BLACK);
                break;
        }

        tv.setBackground(bg);

        tv.setOnClickListener(v -> {
            if (listener != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onDiaClick(pos, dia);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dias.size();
    }

    static class DiaViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public DiaViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}