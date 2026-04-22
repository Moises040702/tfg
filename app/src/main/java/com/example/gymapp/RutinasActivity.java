package com.example.gymapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class RutinasActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private CategoriaAdapter adapter;
    private ArrayList<String> categoriasCodigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutinas);

        ImageButton btnVolver = findViewById(R.id.btnVolverRutinas);
        btnVolver.setOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerViewRutinas);
        FloatingActionButton fab = findViewById(R.id.btnAgregarRutina);
        fab.setVisibility(View.GONE);

        categoriasCodigo = new ArrayList<>();
        categoriasCodigo.add("biceps");
        categoriasCodigo.add("chest");
        categoriasCodigo.add("back");
        categoriasCodigo.add("legs");
        categoriasCodigo.add("shoulders");
        categoriasCodigo.add("abs");

        adapter = new CategoriaAdapter(categoriasCodigo, codigo -> {
            Intent intent = new Intent(RutinasActivity.this, DetalleRutinaActivity.class);
            intent.putExtra("CODIGO_CATEGORIA", codigo);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}