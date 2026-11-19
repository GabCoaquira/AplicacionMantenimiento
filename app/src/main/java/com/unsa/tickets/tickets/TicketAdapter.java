package com.unsa.tickets.tickets;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.unsa.tickets.R;

import java.util.List;

public class TicketAdapter extends BaseAdapter {

    public static class TicketItem {
        public String codigo, fecha, codEst, nomEst, codFac, nomFac, codProg, nomProg, descripcion, estado;

        public TicketItem(String codigo, String fecha, String codEst, String nomEst,
                          String codFac, String nomFac, String codProg, String nomProg,
                          String descripcion, String estado) {
            this.codigo = codigo;
            this.fecha = fecha;
            this.codEst = codEst;
            this.nomEst = nomEst;
            this.codFac = codFac;
            this.nomFac = nomFac;
            this.codProg = codProg;
            this.nomProg = nomProg;
            this.descripcion = descripcion;
            this.estado = estado;
        }
    }

    private final Context context;
    private final List<TicketItem> lista;

    public TicketAdapter(Context context, List<TicketItem> lista) {
        this.context = context;
        this.lista = lista;
    }

    @Override public int getCount() { return lista.size(); }
    @Override public Object getItem(int position) { return lista.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_ticket, parent, false);
        }

        TicketItem item = lista.get(position);

        TextView tvCodigo = convertView.findViewById(R.id.tvItemCodigo);
        TextView tvEstudiante = convertView.findViewById(R.id.tvItemEstudiante);
        TextView tvDetalle = convertView.findViewById(R.id.tvItemDetalle);
        TextView tvEstado = convertView.findViewById(R.id.tvItemEstado);

        tvCodigo.setText(item.codigo);
        tvEstudiante.setText(item.codEst + " - " + item.nomEst);
        tvDetalle.setText(item.fecha + " • " + item.descripcion);
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
