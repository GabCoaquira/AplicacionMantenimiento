package com.unsa.tickets.facultades;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.unsa.tickets.R;
import com.unsa.tickets.database.DatabaseHelper;
import com.unsa.tickets.facultades.FacultadAdapter.FacultadItem;

import java.util.ArrayList;
import java.util.List;

/**
 * P3 - Mantenimiento de Facultades.
 *
 * Pantalla principal: buscador + filtros de estado (A / I / *) + orden (código / nombre)
 * + lista tabular + botones Salir y Adicionar.
 *
 * Al tocar una fila o "Adicionar" se abre un BottomSheet (bottom_sheet_facultad.xml)
 * que solo muestra los botones permitidos según el estado del registro:
 *   - Nuevo      : Guardar, Cancelar
 *   - Activo (A) : Modificar, Inactivar, Eliminar, Cancelar
 *   - Inactivo(I): Reactivar, Eliminar, Cancelar
 *   - Eliminado(*): solo Cerrar
 */
public class FacultadesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView lvFacultades;
    private TextView tvContador, tvVacio;

    private final List<FacultadItem> listaFacultades = new ArrayList<>();
    private FacultadAdapter adapter;

    private String filtroEstadoActual = "A";   // Por defecto: Activos
    private String ordenActual = "codigo";      // "codigo" o "nombre"
    private String textoBusqueda = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_facultades);

        // Evita que la barra de estado / navegación tape la cabecera y los botones
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        lvFacultades = findViewById(R.id.lvFacultades);
        tvContador = findViewById(R.id.tvContadorFacultades);
        tvVacio = findViewById(R.id.tvVacioFacultades);
        EditText etBuscar = findViewById(R.id.etBuscarFacultad);
        RadioGroup rgFiltroEstado = findViewById(R.id.rgFiltroEstadoFacultad);
        RadioGroup rgOrden = findViewById(R.id.rgOrdenFacultad);
        Button btnSalir = findViewById(R.id.btnSalirFacultades);
        Button btnAdicionar = findViewById(R.id.btnAdicionarFacultad);
        TextView tvAtras = findViewById(R.id.tvAtrasFacultades);

        adapter = new FacultadAdapter(this, listaFacultades);
        lvFacultades.setAdapter(adapter);
        lvFacultades.setEmptyView(tvVacio);

        cargarFacultades();

        // Filtro por estado
        rgFiltroEstado.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbActivosFac) filtroEstadoActual = "A";
            else if (checkedId == R.id.rbInactivosFac) filtroEstadoActual = "I";
            else if (checkedId == R.id.rbEliminadosFac) filtroEstadoActual = "*";
            cargarFacultades();
        });

        // Ordenamiento
        rgOrden.setOnCheckedChangeListener((group, checkedId) -> {
            ordenActual = (checkedId == R.id.rbOrdenNombreFac) ? "nombre" : "codigo";
            cargarFacultades();
        });

        // Búsqueda en vivo por código o nombre
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoBusqueda = s.toString().trim();
                cargarFacultades();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Tocar una fila -> abre el BottomSheet en modo edición
        lvFacultades.setOnItemClickListener((parent, view, position, id) ->
                abrirBottomSheet(listaFacultades.get(position)));

        // Adicionar -> abre el BottomSheet en modo nuevo
        btnAdicionar.setOnClickListener(v -> abrirBottomSheet(null));

        // Salir -> regresa al menú principal
        btnSalir.setOnClickListener(v -> finish());
        tvAtras.setOnClickListener(v -> finish());
    }

    /** Lee la tabla facultades aplicando filtro de estado, búsqueda y orden. */
    private void cargarFacultades() {
        listaFacultades.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // ordenActual solo puede ser "codigo" o "nombre" (no viene del usuario), así que es seguro concatenarlo
        String orderBy = "nombre".equals(ordenActual) ? "nombre COLLATE NOCASE ASC" : "codigo ASC";
        String query = "SELECT codigo, nombre, estado FROM " + DatabaseHelper.TABLA_FACULTADES +
                " WHERE estado = ? AND (codigo LIKE ? OR nombre LIKE ?)" +
                " ORDER BY " + orderBy;

        String like = "%" + textoBusqueda + "%";
        Cursor cursor = db.rawQuery(query, new String[]{filtroEstadoActual, like, like});
        while (cursor.moveToNext()) {
            listaFacultades.add(new FacultadItem(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2)));
        }
        cursor.close();

        adapter.notifyDataSetChanged();
        tvContador.setText(listaFacultades.size() + " registro(s) · toque una fila para ver/editar");
    }

    /** Verifica si ya existe otra facultad con el mismo nombre (ignorando mayúsculas). */
    private boolean existeNombre(String nombre, String codigoExcluido) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM " + DatabaseHelper.TABLA_FACULTADES +
                        " WHERE nombre = ? COLLATE NOCASE AND codigo <> ?",
                new String[]{nombre, codigoExcluido == null ? "" : codigoExcluido});
        boolean existe = c.moveToFirst();
        c.close();
        return existe;
    }

    /**
     * Abre la ventana desplazable.
     * @param existente null para Adicionar; la fila tocada para Ver/Editar.
     */
    private void abrirBottomSheet(FacultadItem existente) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_facultad, null);
        dialog.setContentView(view);
        // Que se abra completamente expandida (útil cuando aparece el teclado)
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.getBehavior().setSkipCollapsed(true);

        TextView tvTitulo = view.findViewById(R.id.tvTituloSheetFac);
        TextView tvModo = view.findViewById(R.id.tvModoSheetFac);
        EditText etCodigo = view.findViewById(R.id.etCodigoFacultadSheet);
        EditText etNombre = view.findViewById(R.id.etNombreFacultadSheet);
        TextView tvEstado = view.findViewById(R.id.tvEstadoActualSheetFac);

        Button btnGuardar = view.findViewById(R.id.btnSheetGuardarFac);
        Button btnInactivar = view.findViewById(R.id.btnSheetInactivarFac);
        Button btnReactivar = view.findViewById(R.id.btnSheetReactivarFac);
        Button btnEliminar = view.findViewById(R.id.btnSheetEliminarFac);
        Button btnCancelar = view.findViewById(R.id.btnSheetCancelarFac);

        final boolean esNuevo = (existente == null);

        // Ocultamos todo y luego mostramos solo lo permitido
        btnGuardar.setVisibility(View.GONE);
        btnInactivar.setVisibility(View.GONE);
        btnReactivar.setVisibility(View.GONE);
        btnEliminar.setVisibility(View.GONE);
        btnCancelar.setVisibility(View.VISIBLE);

        if (esNuevo) {
            tvTitulo.setText("Adicionar Facultad");
            tvModo.setText("Modo: ADICIÓN – solo Guardar y Cancelar habilitados");
            etCodigo.setEnabled(true);
            etCodigo.requestFocus();
            tvEstado.setText("Estado: A (Activo por defecto)");
            tvEstado.setTextColor(Color.parseColor("#2E7D32"));
            btnGuardar.setText("Guardar");
            btnGuardar.setVisibility(View.VISIBLE);
        } else {
            tvTitulo.setText("Facultad #" + existente.codigo);
            etCodigo.setText(existente.codigo);
            etCodigo.setEnabled(false);          // La clave primaria no se modifica
            etNombre.setText(existente.nombre);

            switch (existente.estado) {
                case "A":
                    tvModo.setText("Modo: EDICIÓN – registro activo");
                    tvEstado.setText("Estado: A (Activo)");
                    tvEstado.setTextColor(Color.parseColor("#2E7D32"));
                    btnGuardar.setText("Modificar");
                    btnGuardar.setVisibility(View.VISIBLE);
                    btnInactivar.setVisibility(View.VISIBLE);
                    btnEliminar.setVisibility(View.VISIBLE);
                    break;
                case "I":
                    tvModo.setText("Registro inactivo – reactívelo para poder modificarlo");
                    tvEstado.setText("Estado: I (Inactivo)");
                    tvEstado.setTextColor(Color.parseColor("#F57C00"));
                    etNombre.setEnabled(false);
                    btnReactivar.setVisibility(View.VISIBLE);
                    btnEliminar.setVisibility(View.VISIBLE);
                    break;
                default: // "*"
                    tvModo.setText("Registro eliminado – solo lectura");
                    tvEstado.setText("Estado: * (Eliminado lógico)");
                    tvEstado.setTextColor(Color.parseColor("#C62828"));
                    etNombre.setEnabled(false);
                    btnCancelar.setText("Cerrar");
                    break;
            }
        }

        // GUARDAR (nuevo) / MODIFICAR (activo)
        btnGuardar.setOnClickListener(v -> {
            String cod = etCodigo.getText().toString().trim();
            String nom = etNombre.getText().toString().trim();

            if (!cod.matches("\\d{3}")) {
                etCodigo.setError("Debe tener exactamente 3 dígitos");
                Toast.makeText(this, "El código debe tener exactamente 3 dígitos (ej. 025)", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nom.isEmpty()) {
                etNombre.setError("Obligatorio");
                Toast.makeText(this, "Ingrese el nombre de la facultad", Toast.LENGTH_SHORT).show();
                return;
            }
            if (esNuevo && dbHelper.existeCodigo(DatabaseHelper.TABLA_FACULTADES, cod)) {
                etCodigo.setError("Código ocupado");
                new AlertDialog.Builder(this)
                        .setTitle("Código ocupado")
                        .setMessage("No se puede guardar con el código " + cod +
                                " porque ya está ocupado por otra facultad.\nIngrese un código diferente.")
                        .setPositiveButton("Aceptar", null)
                        .show();
                return;
            }
            if (existeNombre(nom, esNuevo ? null : cod)) {
                etNombre.setError("Nombre repetido");
                Toast.makeText(this, "Ya existe una facultad con ese nombre", Toast.LENGTH_LONG).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("nombre", nom);
            if (esNuevo) {
                cv.put("codigo", cod);
                cv.put("estado", "A");
                db.insert(DatabaseHelper.TABLA_FACULTADES, null, cv);
                Toast.makeText(this, "Facultad registrada correctamente", Toast.LENGTH_SHORT).show();
            } else {
                db.update(DatabaseHelper.TABLA_FACULTADES, cv, "codigo = ?", new String[]{cod});
                Toast.makeText(this, "Facultad modificada correctamente", Toast.LENGTH_SHORT).show();
            }
            cargarFacultades();
            dialog.dismiss();
        });

        // INACTIVAR (A -> I)
        btnInactivar.setOnClickListener(v -> {
            dbHelper.cambiarEstadoRegistro(DatabaseHelper.TABLA_FACULTADES, existente.codigo, "I");
            Toast.makeText(this, "Facultad inactivada (I)", Toast.LENGTH_SHORT).show();
            cargarFacultades();
            dialog.dismiss();
        });

        // REACTIVAR (I -> A)
        btnReactivar.setOnClickListener(v -> {
            dbHelper.cambiarEstadoRegistro(DatabaseHelper.TABLA_FACULTADES, existente.codigo, "A");
            Toast.makeText(this, "Facultad reactivada (A)", Toast.LENGTH_SHORT).show();
            cargarFacultades();
            dialog.dismiss();
        });

        // ELIMINAR (A/I -> *) con confirmación
        btnEliminar.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Eliminar facultad")
                .setMessage("¿Seguro que desea eliminar la facultad \"" + existente.nombre + "\"?\n" +
                        "(Eliminación lógica: pasará al filtro Eliminados)")
                .setPositiveButton("Eliminar", (d, w) -> {
                    dbHelper.cambiarEstadoRegistro(DatabaseHelper.TABLA_FACULTADES, existente.codigo, "*");
                    Toast.makeText(this, "Facultad eliminada lógicamente (*)", Toast.LENGTH_SHORT).show();
                    cargarFacultades();
                    dialog.dismiss();
                })
                .setNegativeButton("No", null)
                .show());

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
