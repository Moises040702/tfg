package com.example.gymapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RegistroPesoActivity extends BaseActivity {

    private EditText etPeso;
    private Button btnGuardarPeso;
    private ImageButton btnVolver;
    private RecyclerView recyclerView;
    private PesoAdapter adapter;
    private List<PesoRegistro> registros = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_peso);

        etPeso = findViewById(R.id.etPeso);
        btnGuardarPeso = findViewById(R.id.btnGuardarPeso);
        btnVolver = findViewById(R.id.btnVolver);
        recyclerView = findViewById(R.id.recyclerViewPesos);

        adapter = new PesoAdapter(registros);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        cargarPesosFirebase();

        btnGuardarPeso.setOnClickListener(v -> guardarPeso());

        btnVolver.setOnClickListener(v -> finish());
    }

    private void guardarPeso() {

        String pesoStr = etPeso.getText().toString();

        if (pesoStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.ingresa_peso), Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            double peso = Double.parseDouble(pesoStr);

            // Verificar si ya hay un peso registrado esta semana
            if (yaRegistradoEstaSemana()) {
                Toast.makeText(this, "Ya has registrado tu peso esta semana", Toast.LENGTH_SHORT).show();
                return;
            }

            PesoRegistro registro = new PesoRegistro(peso, new Date());

            FireStoreManager.guardarPeso(peso);

            registros.add(0, registro);

            adapter.notifyItemInserted(0);
            recyclerView.scrollToPosition(0);

            etPeso.setText("");

            Toast.makeText(this, getString(R.string.peso_guardado), Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {

            Toast.makeText(this, getString(R.string.valor_invalido), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean yaRegistradoEstaSemana() {

        if (registros.isEmpty()) return false;

        Calendar calActual = Calendar.getInstance();
        int semanaActual = calActual.get(Calendar.WEEK_OF_YEAR);
        int anioActual = calActual.get(Calendar.YEAR);

        for (PesoRegistro r : registros) {
            Calendar calRegistro = Calendar.getInstance();
            calRegistro.setTime(r.getFecha());
            int semanaRegistro = calRegistro.get(Calendar.WEEK_OF_YEAR);
            int anioRegistro = calRegistro.get(Calendar.YEAR);

            if (semanaRegistro == semanaActual && anioRegistro == anioActual) {
                return true; // Ya hay registro esta semana
            }
        }
        return false;
    }

    private void cargarPesosFirebase() {

        FireStoreManager.obtenerPesos(new FireStoreManager.PesoCallback() {
            @Override
            public void onSuccess(List<PesoRegistro> lista) {

                registros.clear();
                registros.addAll(lista);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {

                Toast.makeText(RegistroPesoActivity.this,
                        "Error cargando pesos",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}