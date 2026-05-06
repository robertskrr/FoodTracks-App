/**
 * © FoodTracks Project ===robertskrr===
 */

package com.foodtracks.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodtracks.app.R;
import com.foodtracks.app.activities.cliente.PerfilClienteActivity;
import com.foodtracks.app.models.Publicacion;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.foodtracks.app.utils.DateUtils;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

/**
 * Adapter para gestionar y renderizar la lista de publicaciones.
 *
 * @author Robert
 * @since 06/05
 */
public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.PublicacionViewHolder> {

    private final List<Publicacion> listaPublicaciones;
    private final Context context;
    private final IUsuarioService usuarioService;

    public PublicacionAdapter(List<Publicacion> listaPublicaciones, Context context) {
        this.listaPublicaciones = listaPublicaciones;
        this.context = context;
        this.usuarioService = ServiceFactory.provideUsuarioService(context);
    }

    @NonNull
    @Override
    public PublicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_publicacion, parent, false);
        return new PublicacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicacionViewHolder holder, int position) {
        Publicacion publicacion = listaPublicaciones.get(position);

        // Cargamos los datos básicos de la publicación
        holder.tvTexto.setText(publicacion.getTexto());
        if (publicacion.getFechaHora() != null) {
            holder.tvFecha.setText(DateUtils.getFechaFormateadaShort(publicacion.getFechaHora()));
        }

        // Cargamos la imagen de la publicación (si existe)
        if (publicacion.getImagen() != null && !publicacion.getImagen().isEmpty()) {
            holder.imgPublicacion.setVisibility(View.VISIBLE);
            Glide.with(context).load(publicacion.getImagen()).into(holder.imgPublicacion);
        } else {
            holder.imgPublicacion.setVisibility(View.GONE);
        }

        // Cargamos los datos del autor de la publicación
        cargarDatosAutor(holder, publicacion.getUidUsuario());

        View.OnClickListener irAlPerfilListener = v -> {
            // Intent dirigido a la Activity del perfil
            Intent intent = new Intent(context, PerfilClienteActivity.class);

            // Pasamos el UID del dueño de la publicación
            intent.putExtra("UID_USUARIO", publicacion.getUidUsuario());

            context.startActivity(intent);
        };

        // Asignamos el listener a la foto de perfil y al nombre de usuario
        holder.imgAvatarAutor.setOnClickListener(irAlPerfilListener);
        holder.tvUsernameAutor.setOnClickListener(irAlPerfilListener);
    }

    /**
     * Consulta Firestore para obtener el username y la foto del autor de una publicación.
     */
    private void cargarDatosAutor(PublicacionViewHolder holder, String uidAutor) {
        // Ponemos valores por defecto mientras carga
        holder.tvUsernameAutor.setText(R.string.cargando);
        holder.imgAvatarAutor.setImageResource(R.drawable.avatar_default);

        usuarioService.getPerfil(uidAutor)
                .addOnSuccessListener(usuario -> {
                    if (usuario != null) {
                        holder.tvUsernameAutor.setText("@" + usuario.getUsername());

                        if (usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isEmpty()) {
                            Glide.with(context)
                                    .load(usuario.getFotoPerfil())
                                    .into(holder.imgAvatarAutor);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    holder.tvUsernameAutor.setText("@usuario_desconocido");
                    Log.e("PublicacionAdapter", "Error cargando autor: " + e.getMessage());
                });
    }

    @Override
    public int getItemCount() {
        return listaPublicaciones.size();
    }

    public static class PublicacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsernameAutor, tvFecha, tvTexto;
        ShapeableImageView imgAvatarAutor, imgPublicacion;

        public PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsernameAutor = itemView.findViewById(R.id.tvUsernameAutor);
            imgAvatarAutor = itemView.findViewById(R.id.imgAvatarAutor);
            tvFecha = itemView.findViewById(R.id.tvFechaPublicacion);
            tvTexto = itemView.findViewById(R.id.tvTextoPublicacion);
            imgPublicacion = itemView.findViewById(R.id.imgPublicacion);
        }
    }
}