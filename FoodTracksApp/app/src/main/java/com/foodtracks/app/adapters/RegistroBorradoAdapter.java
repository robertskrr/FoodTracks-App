package com.foodtracks.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodtracks.app.R;
import com.foodtracks.app.models.RegistroBorradoPublicacion;
import com.foodtracks.app.models.RegistroBorradoUsuario;
import com.foodtracks.app.utils.DateUtils;

import java.util.List;

/**
 * Adapter para gestionar y renderizar la lista de registros de borrado.
 *
 * @author Robert
 * @since 15/05
 */
public class RegistroBorradoAdapter extends RecyclerView.Adapter<RegistroBorradoAdapter.ViewHolder> {

    private final List<Object> listaRegistros;
    private final Context context;

    public RegistroBorradoAdapter(List<Object> listaRegistros, Context context) {
        this.listaRegistros = listaRegistros;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_registro_borrado, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object item = listaRegistros.get(position);

        // Si es un registro de borrado de usuario
        if (item instanceof RegistroBorradoUsuario reg) {
            holder.tvTitulo.setText(context.getString(R.string.cuenta_eliminada_de) + " @" + reg.getUsernameUsuario());
            holder.tvMotivo.setText(context.getString(R.string.motivo) +" "+ reg.getMotivo());
            if (reg.getFechaHora() != null) {
                holder.tvFecha.setText("Admin: " + reg.getUidAdmin().substring(0,5) + "... | " + DateUtils.getFechaFormateadaShort(reg.getFechaHora()));
            }
            holder.imgIcono.setImageResource(R.drawable.ic_person);

            // Si es un registro de borrado de publicación
        } else if (item instanceof RegistroBorradoPublicacion reg) {
            holder.tvTitulo.setText(context.getString(R.string.publicacion_borrada_de) +  " @" + reg.getUsernameUsuario());
            holder.tvMotivo.setText(context.getString(R.string.motivo) + " " + reg.getMotivo());
            if (reg.getFechaHora() != null) {
                holder.tvFecha.setText("Admin: " + reg.getUidAdmin().substring(0,5) + "... | " + DateUtils.getFechaFormateadaShort(reg.getFechaHora()));
            }
            holder.imgIcono.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return listaRegistros.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvMotivo, tvFecha;
        ImageView imgIcono;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloRegistro);
            tvMotivo = itemView.findViewById(R.id.tvMotivoRegistro);
            tvFecha = itemView.findViewById(R.id.tvFechaRegistro);
            imgIcono = itemView.findViewById(R.id.imgIconoRegistro);
        }
    }
}