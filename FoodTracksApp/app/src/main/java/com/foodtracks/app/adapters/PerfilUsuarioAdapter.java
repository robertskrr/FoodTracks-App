/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodtracks.app.R;
import com.foodtracks.app.activities.cliente.PerfilClienteActivity;
import com.foodtracks.app.activities.local.PerfilLocalActivity;
import com.foodtracks.app.models.LikePublicacion;
import com.foodtracks.app.models.Usuario;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.foodtracks.app.utils.DateUtils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

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
    private final IUsuarioService usuarioService;

    public PerfilUsuarioAdapter(List<Usuario> listaPerfiles, Context context) {
        this.listaPerfiles = listaPerfiles;
        this.context = context;
        this.usuarioService = ServiceFactory.provideUsuarioService(context);
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

        // Cargamos los datos básicos de la publicación
        holder.tvUsernameBusqueda.setText(usuario.getUsername());

        // Cargamos la foto de perfil del usuario (si existe)
        if (usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isEmpty()) {
            holder.imgAvatarBusqueda.setVisibility(View.VISIBLE);
            Glide.with(context).load(usuario.getFotoPerfil()).into(holder.imgAvatarBusqueda);
        } else {
            holder.imgAvatarBusqueda.setVisibility(View.GONE);
        }

        // Cargamos los datos del autor de la publicación
        cargarDatosPerfil(holder, usuario.getUid());}


    /**
     * Consulta Firestore para obtener el perfil en la interfaz de búsqueda.
     */
    private void cargarDatosPerfil(PerfilUsuarioViewHolder holder, String uid) {
        // Ponemos valores por defecto mientras carga
        holder.imgAvatarBusqueda.setImageResource(R.drawable.avatar_default);

        // Desactivamos los clicks temporalmente para que el usuario no pulse antes de cargar
        holder.imgAvatarBusqueda.setOnClickListener(null);
        holder.tvUsernameBusqueda.setOnClickListener(null);

        usuarioService
                .getPerfil(uid)
                .addOnSuccessListener(
                        usuario -> {
                            if (usuario != null) {
                                holder.tvUsernameBusqueda.setText(usuario.getUsername());
                                holder.tvNombreBusqueda.setText(usuario.getNombre());

                                if (usuario.getFotoPerfil() != null
                                        && !usuario.getFotoPerfil().isEmpty()) {
                                    Glide.with(context)
                                            .load(usuario.getFotoPerfil())
                                            .into(holder.imgAvatarBusqueda);
                                }

                                View.OnClickListener irAlPerfilListener =
                                        v -> {
                                            Intent intent;
                                            if ("local".equals(usuario.getRol())) {
                                                intent =
                                                        new Intent(
                                                                context, PerfilLocalActivity.class);
                                            } else {
                                                intent =
                                                        new Intent(
                                                                context,
                                                                PerfilClienteActivity.class);
                                            }

                                            intent.putExtra("UID_USUARIO", uid);
                                            context.startActivity(intent);
                                        };

                                // Asignamos el listener ya configurado
                                holder.imgAvatarBusqueda.setOnClickListener(irAlPerfilListener);
                                holder.tvUsernameBusqueda.setOnClickListener(irAlPerfilListener);
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            holder.tvUsernameBusqueda.setText(R.string.usuario_desconocido);
                            Log.e("PerfilUsuarioAdapter", "Error cargando perfil: " + e.getMessage());
                        });
    }

    @Override
    public int getItemCount() {
        return listaPerfiles.size();
    }

    public static class PerfilUsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsernameBusqueda, tvNombreBusqueda;
        ShapeableImageView imgAvatarBusqueda;

        public PerfilUsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsernameBusqueda = itemView.findViewById(R.id.tvUsernameBusqueda);
            imgAvatarBusqueda = itemView.findViewById(R.id.imgAvatarBusqueda);
            tvNombreBusqueda = itemView.findViewById(R.id.tvNombreBusqueda);
        }
    }
}
