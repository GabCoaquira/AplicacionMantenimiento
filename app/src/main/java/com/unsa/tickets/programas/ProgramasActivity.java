package com.unsa.tickets.programas;

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
import com.unsa.tickets.programas.ProgramaAdapter.ProgramaItem;

import java.util.ArrayList;
import java.util.List;

/**
 * P4 - Mantenimiento de Programas Profesionales.
 *
 * Pantalla principal: buscador + filtros de estado (A / I / *) + orden (código
 * / nombre)
 * + lista tabular + botones Salir y Adicionar.
 *
 * Al tocar una fila o "Adicionar" se abre un BottomSheet
 * (bottom_sheet_programa.xml)
 * que solo muestra los botones permitidos según el estado del registro:
 * - Nuevo : Guardar, Cancelar
 * - Activo (A) : Modificar, Inactivar, Eliminar, Cancelar
 * - Inactivo(I): Reactivar, Eliminar, Cancelar
 * - Eliminado(*): solo Cerrar
 */
public class ProgramasActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView lvProgramas;
    private TextView tvContador, tvVacio;

    private final List<ProgramaItem> listaProgramas = new ArrayList<>();
    private ProgramaAdapter adapter;

    private String filtroEstadoActual = "A"; // Por defecto: Activos
    private String ordenActual = "nombre"; // "nombre" (por defecto) o "codigo"
    private String textoBusqueda = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_programas);

        // Evita que la barra de estado / navegación tape la cabecera y los botones
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        lvProgramas = findViewById(R.id.lvProgramas);
        tvContador = findViewById(R.id.tvContadorProgramas);
        tvVacio = findViewById(R.id.tvVacioProgramas);
        EditText etBuscar = findViewById(R.id.etBuscarPrograma);
        RadioGroup rgFiltroEstado = findViewById(R.id.rgFiltroEstadoPrograma);
        RadioGroup rgOrden = findViewById(R.id.rgOrdenPrograma);
        Button btnSalir = findViewById(R.id.btnSalirProgramas);
        Button btnAdicionar = findViewById(R.id.btnAdicionarPrograma);
        TextView tvAtras = findViewById(R.id.tvAtrasProgramas);

        adapter = new ProgramaAdapter(this, listaProgramas);
        lvProgramas.setAdapter(adapter);
        lvProgramas.setEmptyView(tvVacio);

        cargarProgramas();

        // Filtro por estado
        rgFiltroEstado.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbActivosProg)
                filtroEstadoActual = "A";
            else if (checkedId == R.id.rbInactivosProg)
                filtroEstadoActual = "I";
            else if (checkedId == R.id.rbEliminadosProg)
                filtroEstadoActual = "*";
            cargarProgramas();
        });

        // Ordenamiento
        rgOrden.setOnCheckedChangeListener((group, checkedId) -> {
            ordenActual = (checkedId == R.id.rbOrdenNombreProg) ? "nombre" : "codigo";
            cargarProgramas();
        });

        // Búsqueda en vivo por código o nombre
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoBusqueda = s.toString().trim();
                cargarProgramas();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Tocar una fila -> abre el BottomSheet en modo edición
        lvProgramas
                .setOnItemClickListener((parent, view, position, id) -> abrirBottomSheet(listaProgramas.get(position)));

        // Adicionar -> abre el BottomSheet en modo nuevo
        btnAdicionar.setOnClickListener(v -> abrirBottomSheet(null));

        // Salir -> regresa al menú principal
        btnSalir.setOnClickListener(v -> finish());
        tvAtras.setOnClickListener(v -> finish());
    }

    /** Lee la tabla programas aplicando filtro de estado, búsqueda y orden. */
    private void cargarProgramas() {
        listaProgramas.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // ordenActual solo puede ser "codigo" o "nombre" (no viene del usuario), así
        // que es seguro concatenarlo
        String orderBy = "nombre".equals(ordenActual) ? "nombre COLLATE NOCASE ASC" : "codigo ASC";
        String query = "SELECT codigo, nombre, estado FROM " + DatabaseHelper.TABLA_PROGRAMAS +
                " WHERE estado = ? AND (codigo LIKE ? OR nombre LIKE ?)" +
                " ORDER BY " + orderBy;

        String like = "%" + textoBusqueda + "%";
        Cursor cursor = db.rawQuery(query, new String[] { filtroEstadoActual, like, like });
        while (cursor.moveToNext()) {
            listaProgramas.add(new ProgramaItem(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2)));
        }
        cursor.close();

        adapter.notifyDataSetChanged();
        tvContador.setText(listaProgramas.size() + " registro(s) · toque una fila para ver/editar");
    }

    /**
     * Verifica si ya existe otro programa con el mismo nombre (ignorando
     * mayúsculas).
     */
    private boolean existeNombre(String nombre, String codigoExcluido) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM " + DatabaseHelper.TABLA_PROGRAMAS +
                " WHERE nombre = ? COLLATE NOCASE AND codigo <> ?",
                new String[] { nombre, codigoExcluido == null ? "" : codigoExcluido });
        boolean existe = c.moveToFirst();
        c.close();
        return existe;
    }

    /** Cantidad de tickets activos (A) que referencian este programa. */
    private int contarTicketsActivos(String codigoPrograma) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLA_TICKETS +
                " WHERE cod_programa = ? AND estado = 'A'", new String[] { codigoPrograma });
        int n = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();
        return n;
    }

    /**
     * Texto adicional si hay tickets activos que usan el programa (vacío si no
     * hay).
     */
    private String avisoTickets(String codigoPrograma) {
        int n = contarTicketsActivos(codigoPrograma);
        return n == 0 ? "" : "\n\nHay " + n + " ticket(s) activo(s) que usan este programa.";
    }

    /** Pide confirmación al usuario antes de ejecutar una acción. */
    private void confirmar(String titulo, String mensaje, Runnable accion) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Sí", (d, w) -> accion.run())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Abre la ventana desplazable.
     * 
     * @param existente null para Adicionar; la fila tocada para Ver/Editar.
     */
    private void abrirBottomSheet(ProgramaItem existente) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_programa, null);
        dialog.setContentView(view);
        // Que se abra completamente expandida (útil cuando aparece el teclado)
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.getBehavior().setSkipCollapsed(true);

        TextView tvTitulo = view.findViewById(R.id.tvTituloSheetProg);
        TextView tvModo = view.findViewById(R.id.tvModoSheetProg);
        EditText etCodigo = view.findViewById(R.id.etCodigoProgramaSheet);
        EditText etNombre = view.findViewById(R.id.etNombreProgramaSheet);
        TextView tvEstado = view.findViewById(R.id.tvEstadoActualSheetProg);

        Button btnGuardar = view.findViewById(R.id.btnSheetGuardarProg);
        Button btnInactivar = view.findViewById(R.id.btnSheetInactivarProg);
        Button btnReactivar = view.findViewById(R.id.btnSheetReactivarProg);
        Button btnEliminar = view.findViewById(R.id.btnSheetEliminarProg);
        Button btnCancelar = view.findViewById(R.id.btnSheetCancelarProg);

        final boolean esNuevo = (existente == null);

        // Ocultamos todo y luego mostramos solo lo permitido
        btnGuardar.setVisibility(View.GONE);
        btnInactivar.setVisibility(View.GONE);
        btnReactivar.setVisibility(View.GONE);
        btnEliminar.setVisibility(View.GONE);
        btnCancelar.setVisibility(View.VISIBLE);

        if (esNuevo) {
            tvTitulo.setText("Adicionar Programa");
            tvModo.setText("Modo: ADICIÓN – solo Guardar y Cancelar habilitados");
            etCodigo.setEnabled(true);
            etCodigo.requestFocus();
            tvEstado.setText("Estado: A (Activo por defecto)");
            tvEstado.setTextColor(Color.parseColor("#2E7D32"));
            btnGuardar.setText("Guardar");
            btnGuardar.setVisibility(View.VISIBLE);
        } else {
            tvTitulo.setText("Programa #" + existente.codigo);
            etCodigo.setText(existente.codigo);
            etCodigo.setEnabled(false); // La clave primaria no se modifica
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
                    tvEstado.setText("Estado: * (Eliminado)");
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
                Toast.makeText(this, "Ingrese el nombre del programa", Toast.LENGTH_SHORT).show();
                return;
            }
            if (esNuevo && dbHelper.existeCodigo(DatabaseHelper.TABLA_PROGRAMAS, cod)) {
                etCodigo.setError("Código ocupado");
                new AlertDialog.Builder(this)
                        .setTitle("Código ocupado")
                        .setMessage("No se puede guardar con el código " + cod +
                                " porque ya está ocupado por otro programa.\nIngrese un código diferente.")
                        .setPositiveButton("Aceptar", null)
                        .show();
                return;
            }
            if (existeNombre(nom, esNuevo ? null : cod)) {
                etNombre.setError("Nombre repetido");
                Toast.makeText(this, "Ya existe un programa con ese nombre", Toast.LENGTH_LONG).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("nombre", nom);
            if (esNuevo) {
                cv.put("codigo", cod);
                cv.put("estado", "A");
                db.insert(DatabaseHelper.TABLA_PROGRAMAS, null, cv);
                Toast.makeText(this, "Programa registrado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                db.update(DatabaseHelper.TABLA_PROGRAMAS, cv, "codigo = ?", new String[] { cod });
                Toast.makeText(this, "Programa modificado correctamente", Toast.LENGTH_SHORT).show();
            }
            cargarProgramas();
            dialog.dismiss();
        });

        // INACTIVAR (A -> I) con confirmación
        btnInactivar.setOnClickListener(v -> confirmar("Inactivar programa",
                "¿Seguro que desea inactivar el programa \"" + existente.nombre + "\"?"
                        + avisoTickets(existente.codigo),
                () -> {
                    dbHelper.cambiarEstadoRegistro(DatabaseHelper.TABLA_PROGRAMAS, existente.codigo, "I");
                    Toast.makeText(this, "Programa inactivado", Toast.LENGTH_SHORT).show();
                    cargarProgramas();
                    dialog.dismiss();
                }));

        // REACTIVAR (I -> A) con confirmación
        btnReactivar.setOnClickListener(v -> confirmar("Reactivar programa",
                "¿Seguro que desea reactivar el programa \"" + existente.nombre + "\"?",
                () -> {
                    dbHelper.cambiarEstadoRegistro(DatabaseHelper.TABLA_PROGRAMAS, existente.codigo, "A");
                    Toast.makeText(this, "Programa reactivado", Toast.LENGTH_SHORT).show();
                    cargarProgramas();
                    dialog.dismiss();
                }));

        // ELIMINAR con confirmación
        btnEliminar.setOnClickListener(v -> confirmar("Eliminar programa",
                "¿Seguro que desea eliminar el programa \"" + existente.nombre + "\"?"
                        + avisoTickets(existente.codigo),
                () -> {
                    dbHelper.cambiarEstadoRegistro(DatabaseHelper.TABLA_PROGRAMAS, existente.codigo, "*");
                    Toast.makeText(this, "Programa eliminado", Toast.LENGTH_SHORT).show();
                    cargarProgramas();
                    dialog.dismiss();
                }));

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}