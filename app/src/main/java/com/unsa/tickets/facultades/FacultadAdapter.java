package com.unsa.tickets.facultades;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.unsa.tickets.R;

import java.util.List;

/**
 * Adaptador de la lista tabular de Facultades.
 * Cada fila muestra: Código | Nombre | Estado (A / I / *) con color.
 */
public class FacultadAdapter extends BaseAdapter {

    /** Modelo simple de una fila de la tabla facultades. */
    public static class FacultadItem {
        public String codigo, nombre, estado;

        public FacultadItem(String codigo, String nombre, String estado) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.estado = estado;
        }
    }

    /** ViewHolder: evita llamar findViewById en cada scroll. */
    private static class ViewHolder {
        TextView tvCodigo, tvNombre, tvEstado;
    }

    private final Context context;
    private final List<FacultadItem> lista;

    public FacultadAdapter(Context context, List<FacultadItem> lista) {
        this.context = context;
        this.lista = lista;
    }

    @Override public int getCount() { return lista.size(); }
    @Override public Object getItem(int position) { return lista.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_facultad, parent, false);
            holder = new ViewHolder();
            holder.tvCodigo = convertView.findViewById(R.id.tvItemCodigoFacultad);
            holder.tvNombre = convertView.findViewById(R.id.tvItemNombreFacultad);
            holder.tvEstado = convertView.findViewById(R.id.tvItemEstadoFacultad);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FacultadItem item = lista.get(position);
        holder.tvCodigo.setText(item.codigo);
        holder.tvNombre.setText(item.nombre);
        holder.tvEstado.setText(item.estado);

        if ("A".equals(item.estado)) {
            holder.tvEstado.setBackgroundColor(Color.parseColor("#2E7D32")); // Verde
        } else if ("I".equals(item.estado)) {
            holder.tvEstado.setBackgroundColor(Color.parseColor("#F57C00")); // Ámbar
        } else {
            holder.tvEstado.setBackgroundColor(Color.parseColor("#C62828")); // Rojo (*)
        }

        return convertView;
    }
}
