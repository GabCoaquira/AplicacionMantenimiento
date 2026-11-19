package com.unsa.tickets.estudiantes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.unsa.tickets.R;

import java.util.List;

public class EstudianteAdapter extends BaseAdapter {

    public static class EstudianteItem {
        public String codigo, nombre, estado;

        public EstudianteItem(String codigo, String nombre, String estado) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.estado = estado;
        }
    }

    private final Context context;
    private final List<EstudianteItem> lista;

    public EstudianteAdapter(Context context, List<EstudianteItem> lista) {
        this.context = context;
        this.lista = lista;
    }

    @Override public int getCount() { return lista.size(); }
    @Override public Object getItem(int position) { return lista.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_estudiante, parent, false);
        }

        EstudianteItem item = lista.get(position);

        TextView tvCodigo = convertView.findViewById(R.id.tvItemCodigoEstudiante);
        TextView tvNombre = convertView.findViewById(R.id.tvItemNombreEstudiante);
        TextView tvEstado = convertView.findViewById(R.id.tvItemEstadoEstudiante);

        tvCodigo.setText(item.codigo);
        tvNombre.setText(item.nombre);
        tvEstado.setText(item.estado);

        if ("A".equals(item.estado)) {
            tvEstado.setBackgroundColor(Color.parseColor("#2E7D32")); // Verde
        } else if ("I".equals(item.estado)) {
            tvEstado.setBackgroundColor(Color.parseColor("#F57C00")); // Ámbar
        } else {
            tvEstado.setBackgroundColor(Color.parseColor("#C62828")); // Rojo (*)
        }

        return convertView;
    }
}
