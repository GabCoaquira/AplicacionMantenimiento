package com.unsa.tickets;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.unsa.tickets.database.DatabaseHelper;
import com.unsa.tickets.estudiantes.EstudiantesActivity;
import com.unsa.tickets.facultades.FacultadesActivity;
import com.unsa.tickets.programas.ProgramasActivity;
import com.unsa.tickets.tickets.TicketsActivity;

public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.getReadableDatabase();

        Button btnTickets = findViewById(R.id.btnMenuTickets);
        Button btnEstudiantes = findViewById(R.id.btnMenuEstudiantes);
        Button btnFacultades = findViewById(R.id.btnMenuFacultades);
        Button btnProgramas = findViewById(R.id.btnMenuProgramas);
        Button btnSalir = findViewById(R.id.btnSalirApp);

        btnTickets.setOnClickListener(v -> startActivity(new Intent(this, TicketsActivity.class)));
        btnEstudiantes.setOnClickListener(v -> startActivity(new Intent(this, EstudiantesActivity.class)));
        btnFacultades.setOnClickListener(v -> startActivity(new Intent(this, FacultadesActivity.class)));
        btnProgramas.setOnClickListener(v -> startActivity(new Intent(this, ProgramasActivity.class)));

        btnSalir.setOnClickListener(v -> finishAffinity());
    }
}