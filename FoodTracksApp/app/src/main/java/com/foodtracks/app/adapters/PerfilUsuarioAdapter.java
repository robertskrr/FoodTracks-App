/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.adapters;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodtracks.app.R;
import com.foodtracks.app.activities.cliente.PerfilClienteActivity;
import com.foodtracks.app.activities.local.PerfilLocalActivity;
import com.foodtracks.app.models.Usuario;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

/**
 * Adapter para gestionar y renderizar la lista de perfiles
 * en la barra de búsqueda.
 *
 * @author Robert
 * @since 11/05
 */
public class PerfilUsuarioAdapter
        extends RecyclerView.Adapter<PerfilUsuarioAdapter.PerfilUsuarioViewHolder> {

    private final List<Usuario> listaPerfiles;
    private final Context context;

    public PerfilUsuarioAdapter(List<Usuario> listaPerfiles, Context context) {
        this.listaPerfiles = listaPerfiles;
        this.context = context;
    }

    @NonNull
    @Override
    public PerfilUsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_perfil, parent, false);
        return new PerfilUsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PerfilUsuarioViewHolder holder, int position) {
        Usuario usuario = listaPerfiles.get(position);

        // Textos
        holder.tvUsernameBusqueda.setText("@" + usuario.getUsername());
        holder.tvNombreBusqueda.setText(usuario.getNombre());

        // Foto de perfil
        holder.imgAvatarBusqueda.setVisibility(View.VISIBLE);
        if (usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isEmpty()) {
            Glide.with(context).load(usuario.getFotoPerfil()).into(holder.imgAvatarBusqueda);
        } else {
            holder.imgAvatarBusqueda.setImageResource(R.drawable.avatar_default);
        }

        // Icono del tipo de usuario
        if ("local".equals(usuario.getRol())) {
            holder.imgTipoPerfil.setImageResource(R.drawable.ic_store);
        } else {
            holder.imgTipoPerfil.setImageResource(R.drawable.ic_person);
        }

        // Listener para ir al perfil
        View.OnClickListener irAlPerfilListener =
                v -> {
                    Intent intent;
                    if ("local".equals(usuario.getRol())) {
                        intent = new Intent(context, PerfilLocalActivity.class);
                    } else {
                        intent = new Intent(context, PerfilClienteActivity.class);
                    }
                    intent.putExtra("UID_USUARIO", usuario.getUid());
                    context.startActivity(intent);
                };

        holder.imgAvatarBusqueda.setOnClickListener(irAlPerfilListener);
        holder.tvUsernameBusqueda.setOnClickListener(irAlPerfilListener);
        holder.tvNombreBusqueda.setOnClickListener(irAlPerfilListener);
    }

    @Override
    public int getItemCount() {
        return listaPerfiles.size();
    }

    public static class PerfilUsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsernameBusqueda, tvNombreBusqueda;
        ShapeableImageView imgAvatarBusqueda;
        ImageView imgTipoPerfil;

        public PerfilUsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsernameBusqueda = itemView.findViewById(R.id.tvUsernameBusqueda);
            imgAvatarBusqueda = itemView.findViewById(R.id.imgAvatarBusqueda);
            tvNombreBusqueda = itemView.findViewById(R.id.tvNombreBusqueda);
            imgTipoPerfil = itemView.findViewById(R.id.imgTipoPerfil);
        }
    }
}
