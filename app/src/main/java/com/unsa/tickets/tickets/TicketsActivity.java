package com.unsa.tickets.tickets;

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
import com.unsa.tickets.tickets.TicketAdapter.TicketItem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView lvTickets;
    private EditText etBuscar;
    private RadioGroup rgFiltroEstado;

    private final List<TicketItem> listaTickets = new ArrayList<>();
    private TicketAdapter adapter;

    private String filtroEstadoActual = "A"; // Por defecto muestra Activos
    private String textoBusqueda = "";
    private String selCodEst = "", selCodFac = "", selCodProg = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);
        dbHelper = new DatabaseHelper(this);
        lvTickets = findViewById(R.id.lvTickets);
        etBuscar = findViewById(R.id.etBuscarTicket);
        rgFiltroEstado = findViewById(R.id.rgFiltroEstado);
        Button btnSalir = findViewById(R.id.btnSalirTickets);
        Button btnAdicionar = findViewById(R.id.btnAdicionarTicket);
        adapter = new TicketAdapter(this, listaTickets);
        lvTickets.setAdapter(adapter);
        cargarTickets();
        rgFiltroEstado.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbActivos) filtroEstadoActual = "A";
            else if (checkedId == R.id.rbInactivos) filtroEstadoActual = "I";
            else if (checkedId == R.id.rbEliminados) filtroEstadoActual = "*";
            cargarTickets();
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoBusqueda = s.toString().trim();
                cargarTickets();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        lvTickets.setOnItemClickListener((parent, view, position, id) -> {
            TicketItem seleccionado = listaTickets.get(position);
            abrirBottomSheet(seleccionado);
        });

        btnAdicionar.setOnClickListener(v -> abrirBottomSheet(null));
        btnSalir.setOnClickListener(v -> finish());
    }

    private void cargarTickets() {
        listaTickets.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT t.codigo, t.fecha, t.cod_estudiante, IFNULL(e.nombre, 'Desconocido'), " +
                "t.cod_facultad, IFNULL(f.nombre, ''), t.cod_programa, IFNULL(p.nombre, ''), " +
                "t.descripcion, t.estado " +
                "FROM tickets t " +
                "LEFT JOIN estudiantes e ON t.cod_estudiante = e.codigo " +
                "LEFT JOIN facultades f ON t.cod_facultad = f.codigo " +
                "LEFT JOIN programas p ON t.cod_programa = p.codigo " +
                "WHERE t.estado = ? AND (t.codigo LIKE ? OR e.nombre LIKE ? OR t.descripcion LIKE ?) " +
                "ORDER BY t.codigo ASC";

        String likeParam = "%" + textoBusqueda + "%";
        Cursor cursor = db.rawQuery(query, new String[]{filtroEstadoActual, likeParam, likeParam, likeParam});

        while (cursor.moveToNext()) {
            listaTickets.add(new TicketItem(
                    cursor.getString(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), cursor.getString(5),
                    cursor.getString(6), cursor.getString(7),
                    cursor.getString(8), cursor.getString(9)
            ));
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void abrirBottomSheet(TicketItem ticketExistente) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_ticket, null);
        dialog.setContentView(view);

        TextView tvTitulo = view.findViewById(R.id.tvTituloSheet);
        EditText etCodigo = view.findViewById(R.id.etCodigoTicket);
        EditText etFecha = view.findViewById(R.id.etFechaTicket);
        EditText etEst = view.findViewById(R.id.etEstudianteSeleccionado);
        EditText etFac = view.findViewById(R.id.etFacultadSeleccionada);
        EditText etProg = view.findViewById(R.id.etProgramaSeleccionado);
        EditText etDesc = view.findViewById(R.id.etDescripcionTicket);
        TextView tvEstado = view.findViewById(R.id.tvEstadoActualSheet);

        Button btnLupaEst = view.findViewById(R.id.btnBuscarEstudiante);
        Button btnLupaFac = view.findViewById(R.id.btnBuscarFacultad);
        Button btnLupaProg = view.findViewById(R.id.btnBuscarPrograma);
        Button btnGuardar = view.findViewById(R.id.btnSheetGuardar);
        Button btnInactivar = view.findViewById(R.id.btnSheetInactivar);
        Button btnReactivar = view.findViewById(R.id.btnSheetReactivar);
        Button btnEliminar = view.findViewById(R.id.btnSheetEliminar);
        Button btnCancelar = view.findViewById(R.id.btnSheetCancelar);

        btnLupaEst.setOnClickListener(v -> SeleccionarCatalogoDialog.mostrar(
                this, "Seleccionar Estudiante", DatabaseHelper.TABLA_ESTUDIANTES,
                (cod, nom) -> { selCodEst = cod; etEst.setText(cod + " - " + nom); }
        ));
        btnLupaFac.setOnClickListener(v -> SeleccionarCatalogoDialog.mostrar(
                this, "Seleccionar Facultad", DatabaseHelper.TABLA_FACULTADES,
                (cod, nom) -> { selCodFac = cod; etFac.setText(cod + " - " + nom); }
        ));
        btnLupaProg.setOnClickListener(v -> SeleccionarCatalogoDialog.mostrar(
                this, "Seleccionar Programa", DatabaseHelper.TABLA_PROGRAMAS,
                (cod, nom) -> { selCodProg = cod; etProg.setText(cod + " - " + nom); }
        ));

        boolean esNuevo = (ticketExistente == null);

        if (esNuevo) {
            tvTitulo.setText("Adicionar Ticket de Entrega");
            String fechaHoy = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
            etFecha.setText(fechaHoy);
            selCodEst = ""; selCodFac = ""; selCodProg = "";
            tvEstado.setText("Estado: A (Activo por defecto)");
            btnGuardar.setVisibility(View.VISIBLE);
            btnCancelar.setVisibility(View.VISIBLE);
            btnInactivar.setVisibility(View.GONE);
            btnReactivar.setVisibility(View.GONE);
            btnEliminar.setVisibility(View.GONE);
        } else {
            tvTitulo.setText("Detalle del Ticket #" + ticketExistente.codigo);
            etCodigo.setText(ticketExistente.codigo);
            etCodigo.setEnabled(false); // La clave primaria no se edita al modificar
            etFecha.setText(ticketExistente.fecha);

            selCodEst = ticketExistente.codEst;
            selCodFac = ticketExistente.codFac;
            selCodProg = ticketExistente.codProg;

            etEst.setText(ticketExistente.codEst + " - " + ticketExistente.nomEst);
            etFac.setText(ticketExistente.codFac + " - " + ticketExistente.nomFac);
            etProg.setText(ticketExistente.codProg + " - " + ticketExistente.nomProg);
            etDesc.setText(ticketExistente.descripcion);

            String st = ticketExistente.estado;
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
                bloquearCampos(etFecha, etDesc, btnLupaEst, btnLupaFac, btnLupaProg);
                btnGuardar.setVisibility(View.GONE);
                btnInactivar.setVisibility(View.GONE);
                btnReactivar.setVisibility(View.VISIBLE);
                btnEliminar.setVisibility(View.VISIBLE);
                btnCancelar.setVisibility(View.VISIBLE);
            } else {
                tvEstado.setText("Estado: * (Eliminado lógico)");
                bloquearCampos(etFecha, etDesc, btnLupaEst, btnLupaFac, btnLupaProg);
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
            String fec = etFecha.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (cod.length() != 3) {
                Toast.makeText(this, "El código debe tener exactamente 3 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selCodEst.isEmpty() || selCodFac.isEmpty() || selCodProg.isEmpty() || fec.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            if (esNuevo) {
                if (dbHelper.existeCodigo(DatabaseHelper.TABLA_TICKETS, cod)) {
                    Toast.makeText(this, "No se puede guardar: el código " + cod + " ya está ocupado", Toast.LENGTH_LONG).show();
                    return;
                }
                ContentValues cv = new ContentValues();
                cv.put("codigo", cod);
                cv.put("fecha", fec);
                cv.put("cod_estudiante", selCodEst);
                cv.put("cod_facultad", selCodFac);
                cv.put("cod_programa", selCodProg);
                cv.put("descripcion", desc);
                cv.put("estado", "A");
                db.insert(DatabaseHelper.TABLA_TICKETS, null, cv);
                Toast.makeText(this, "Ticket registrado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                ContentValues cv = new ContentValues();
                cv.put("fecha", fec);
                cv.put("cod_estudiante", selCodEst);
                cv.put("cod_facultad", selCodFac);
                cv.put("cod_programa", selCodProg);
                cv.put("descripcion", desc);
                db.update(DatabaseHelper.TABLA_TICKETS, cv, "codigo = ?", new String[]{cod});
                Toast.makeText(this, "Ticket modificado correctamente", Toast.LENGTH_SHORT).show();
            }
            cargarTickets();
            dialog.dismiss();
        });

        // Acciones de cambio de estado
        btnInactivar.setOnClickListener(v -> {
            dbHelper.cambiarEstadoRegistro(DatabaseHelper.TABLA_TICKETS, ticketExistente.codigo, "I");
            Toast.makeText(this, "Ticket inactivado (I)", Toast.LENGTH_SHORT).show();
            cargarTickets();
            dialog.dismiss();
        });

        btnReactivar.setOnClickListener(v -> {
            dbHelper.cambiarEstadoRegistro(DatabaseHelper.TABLA_TICKETS, ticketExistente.codigo, "A");
            Toast.makeText(this, "Ticket reactivado (A)", Toast.LENGTH_SHORT).show();
            cargarTickets();
            dialog.dismiss();
        });

        btnEliminar.setOnClickListener(v -> {
            dbHelper.cambiarEstadoRegistro(DatabaseHelper.TABLA_TICKETS, ticketExistente.codigo, "*");
            Toast.makeText(this, "Ticket eliminado lógicamente (*)", Toast.LENGTH_SHORT).show();
            cargarTickets();
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void bloquearCampos(EditText etFecha, EditText etDesc, Button b1, Button b2, Button b3) {
        etFecha.setEnabled(false);
        etDesc.setEnabled(false);
        b1.setEnabled(false);
        b2.setEnabled(false);
        b3.setEnabled(false);
    }
}