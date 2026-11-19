package com.unsa.tickets.estudiantes;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.unsa.tickets.R;
import com.unsa.tickets.database.DatabaseHelper;
import com.unsa.tickets.estudiantes.EstudianteAdapter.EstudianteItem;

import java.util.ArrayList;
import java.util.List;

public class EstudiantesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView lvEstudiantes;
    private EditText etBuscar;
    private RadioGroup rgFiltroEstado;

    private final List<EstudianteItem> listaEstudiantes = new ArrayList<>();
    private EstudianteAdapter adapter;

    private String filtroEstadoActual = "A"; // Por defecto Activos
    private String textoBusqueda = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estudiantes);

        dbHelper = new DatabaseHelper(this);
        lvEstudiantes = findViewById(R.id.lvEstudiantes);
        etBuscar = findViewById(R.id.etBuscarEstudiante);
        rgFiltroEstado = findViewById(R.id.rgFiltroEstadoEstudiante);
        Button btnSalir = findViewById(R.id.btnSalirEstudiantes);
        Button btnAdicionar = findViewById(R.id.btnAdicionarEstudiante);

        adapter = new EstudianteAdapter(this, listaEstudiantes);
        lvEstudiantes.setAdapter(adapter);

        cargarEstudiantes();

        rgFiltroEstado.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbActivosEst) filtroEstadoActual = "A";
            else if (checkedId == R.id.rbInactivosEst) filtroEstadoActual = "I";
            else if (checkedId == R.id.rbEliminadosEst) filtroEstadoActual = "*";
            cargarEstudiantes();
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoBusqueda = s.toString().trim();
                cargarEstudiantes();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        lvEstudiantes.setOnItemClickListener((parent, view, position, id) -> {
            EstudianteItem seleccionado = listaEstudiantes.get(position);
            abrirBottomSheet(seleccionado);
        });

        btnAdicionar.setOnClickListener(v -> abrirBottomSheet(null));
        btnSalir.setOnClickListener(v -> finish());
    }

    private void cargarEstudiantes() {
        listaEstudiantes.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT codigo, nombre, estado FROM estudiantes " +
                "WHERE estado = ? AND (codigo LIKE ? OR nombre LIKE ?) " +
                "ORDER BY codigo ASC";

        String likeParam = "%" + textoBusqueda + "%";
        Cursor cursor = db.rawQuery(query, new String[]{filtroEstadoActual, likeParam, likeParam});

        while (cursor.moveToNext()) {
            listaEstudiantes.add(new EstudianteItem(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2)
            ));
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void abrirBottomSheet(EstudianteItem estudianteExistente) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_estudiante, null);
        dialog.setContentView(view);

        TextView tvTitulo = view.findViewById(R.id.tvTituloSheetEst);
        EditText etCodigo = view.findViewById(R.id.etCodigoEstudianteSheet);
        EditText etNombre = view.findViewById(R.id.etNombreEstudianteSheet);
        TextView tvEstado = view.findViewById(R.id.tvEstadoActualSheetEst);

        Button btnGuardar = view.findViewById(R.id.btnSheetGuardarEst);
        Button btnInactivar = view.findViewById(R.id.btnSheetInactivarEst);
        Button btnReactivar = view.findViewById(R.id.btnSheetReactivarEst);
        Button btnEliminar = view.findViewById(R.id.btnSheetEliminarEst);
        Button btnCancelar = view.findViewById(R.id.btnSheetCancelarEst);

        boolean esNuevo = (estudianteExistente == null);

        if (esNuevo) {
            tvTitulo.setText("Adicionar Estudiante");
            etCodigo.setEnabled(true);
            etCodigo.setText("");
            etNombre.setText("");
            tvEstado.setText("Estado: A (Activo por defecto)");

            btnGuardar.setVisibility(View.VISIBLE);
            btnGuardar.setText("Guardar");
            btnCancelar.setVisibility(View.VISIBLE);
            btnInactivar.setVisibility(View.GONE);
            btnReactivar.setVisibility(View.GONE);
            btnEliminar.setVisibility(View.GONE);
        } else {
            tvTitulo.setText("Detalle del Estudiante #" + estudianteExistente.codigo);
            etCodigo.setText(estudianteExistente.codigo);
            etCodigo.setEnabled(false); // La clave primaria no se modifica
            etNombre.setText(estudianteExistente.nombre);

            String st = estudianteExistente.estado;
            if ("A".equals(st)) {
                tvEstado.setText("Estado: A (Activo)");
                btnGuardar.setText("Modificar");
                btnGuardar.setVisibility(View.VISIBLE);
                btnInactivar.setVisibility(View.VISIBLE);
                btnEliminar.setVisibility(View.VISIBLE);
                btnReactivar.setVisibility(View.GONE);
                btnCancelar.setVisibility(View.VISIBLE);
            } else if ("I".equals(st)) {
                tvEstado.setText("Estado: I (Inactivo)");
                etNombre.setEnabled(false);
                btnGuardar.setVisibility(View.GONE);
                btnInactivar.setVisibility(View.GONE);
                btnReactivar.setVisibility(View.VISIBLE);
                btnEliminar.setVisibility(View.VISIBLE);
                btnCancelar.setVisibility(View.VISIBLE);
            } else {
                tvEstado.setText("Estado: * (Eliminado lógico)");
                etNombre.setEnabled(false);
                btnGuardar.setVisibility(View.GONE);
                btnInactivar.setVisibility(View.GONE);
                btnReactivar.setVisibility(View.GONE);
                btnEliminar.setVisibility(View.GONE);
                btnCancelar.setText("Cerrar");
                btnCancelar.setVisibility(View.VISIBLE);
            }
        }

        btnGuardar.setOnClickListener(v -> {
            String cod = etCodigo.getText().toString().trim();
            String nom = etNombre.getText().toString().trim();

            if (cod.length() != 3) {
                Toast.makeText(this, "El código debe tener exactamente 3 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nom.isEmpty()) {
                Toast.makeText(this, "Ingrese los apellidos y nombres del estudiante", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            if (esNuevo) {
                if (dbHelper.existeCodigo(DatabaseHelper.TABLA_ESTUDIANTES, cod)) {
                    Toast.makeText(this, "Alerta: El código " + cod + " ya existe en la tabla estudiantes", Toast.LENGTH_LONG).show();
                    return;
                }
                ContentValues cv = new ContentValues();
                cv.put("codigo", cod);
                cv.put("nombre", nom);
                cv.put("estado", "A");
                db.insert(DatabaseHelper.TABLA_ESTUDIANTES, null, cv);
                Toast.makeText(this, "Estudiante registrado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                ContentValues cv = new ContentValues();
                cv.put("nombre", nom);
                db.update(DatabaseHelper.TABLA_ESTUDIANTES, cv, "codigo = ?", new String[]{cod});
                Toast.makeText(this, "Estudiante modificado correctamente", Toast.LENGTH_SHORT).show();
            }
            cargarEstudiantes();
            dialog.dismiss();
        });

        btnInactivar.setOnClickListener(v -> {
            dbHelper.cambiarEstadoRegistro(DatabaseHelper.TABLA_ESTUDIANTES, estudianteExistente.codigo, "I");
            Toast.makeText(this, "Estudiante inactivado (I)", Toast.LENGTH_SHORT).show();
            cargarEstudiantes();
            dialog.dismiss();
        });

        btnReactivar.setOnClickListener(v -> {
            dbHelper.cambiarEstadoRegistro(DatabaseHelper.TABLA_ESTUDIANTES, estudianteExistente.codigo, "A");
            Toast.makeText(this, "Estudiante reactivado (A)", Toast.LENGTH_SHORT).show();
            cargarEstudiantes();
            dialog.dismiss();
        });

        btnEliminar.setOnClickListener(v -> {
            dbHelper.cambiarEstadoRegistro(DatabaseHelper.TABLA_ESTUDIANTES, estudianteExistente.codigo, "*");
            Toast.makeText(this, "Estudiante eliminado lógicamente (*)", Toast.LENGTH_SHORT).show();
            cargarEstudiantes();
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
