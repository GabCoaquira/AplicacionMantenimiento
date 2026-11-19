package com.unsa.tickets.tickets;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.unsa.tickets.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class SeleccionarCatalogoDialog {

    public interface OnItemSelectedListener {
        void onSelected(String codigo, String nombre);
    }

    public static void mostrar(Context context, String titulo, String nombreTabla, OnItemSelectedListener listener) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        List<String> itemsMostrados = new ArrayList<>();
        List<String> codigos = new ArrayList<>();
        List<String> nombres = new ArrayList<>();

        // Consulta referencial: SOLO registros activos ('A') en orden alfabético
        Cursor cursor = db.rawQuery(
                "SELECT codigo, nombre FROM " + nombreTabla + " WHERE estado = 'A' ORDER BY nombre ASC",
                null
        );

        while (cursor.moveToNext()) {
            String cod = cursor.getString(0);
            String nom = cursor.getString(1);
            codigos.add(cod);
            nombres.add(nom);
            itemsMostrados.add(cod + " - " + nom);
        }
        cursor.close();

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText etBuscar = new EditText(context);
        etBuscar.setHint("Buscar por nombre o código...");
        layout.addView(etBuscar);

        ListView listView = new ListView(context);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, itemsMostrados);
        listView.setAdapter(adapter);
        layout.addView(listView);

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(titulo)
                .setView(layout)
                .setNegativeButton("Cancelar", null)
                .create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String seleccionado = adapter.getItem(position);
            if (seleccionado != null && seleccionado.contains(" - ")) {
                String[] partes = seleccionado.split(" - ", 2);
                listener.onSelected(partes[0], partes[1]);
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}
