package com.unsa.tickets.programas;

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
 * Adaptador de la lista tabular de Programas.
 * Cada fila muestra: Código | Nombre | Estado (A / I / *) con color.
 */
public class ProgramaAdapter extends BaseAdapter {

    /** Modelo simple de una fila de la tabla programas. */
    public static class ProgramaItem {
        public String codigo, nombre, estado;

        public ProgramaItem(String codigo, String nombre, String estado) {
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
    private final List<ProgramaItem> lista;

    public ProgramaAdapter(Context context, List<ProgramaItem> lista) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_programa, parent, false);
            holder = new ViewHolder();
            holder.tvCodigo = convertView.findViewById(R.id.tvItemCodigoPrograma);
            holder.tvNombre = convertView.findViewById(R.id.tvItemNombrePrograma);
            holder.tvEstado = convertView.findViewById(R.id.tvItemEstadoPrograma);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ProgramaItem item = lista.get(position);
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
