/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.adapters;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodtracks.app.R;
import com.foodtracks.app.activities.cliente.PerfilClienteActivity;
import com.foodtracks.app.models.LikePublicacion;
import com.foodtracks.app.models.Publicacion;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.interfaces.ILikeService;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.foodtracks.app.utils.DateUtils;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Adapter para gestionar y renderizar la lista de publicaciones.
 *
 * @author Robert
 * @since 06/05
 */
public class PublicacionAdapter
        extends RecyclerView.Adapter<PublicacionAdapter.PublicacionViewHolder> {

    private final List<Publicacion> listaPublicaciones;
    private final Context context;
    private final IUsuarioService usuarioService;
    private final ILikeService likeService;
    private final String currentUid; // Usuario que está usando la publicación
    private FirebaseAuth mAuth;

    public PublicacionAdapter(List<Publicacion> listaPublicaciones, Context context) {
        this.listaPublicaciones = listaPublicaciones;
        this.context = context;
        this.usuarioService = ServiceFactory.provideUsuarioService(context);
        this.likeService = ServiceFactory.provideLikeService();
        mAuth = FirebaseAuth.getInstance();

        // Obtenemos el usuario logueado para comprobar sus propios likes
        if (mAuth.getCurrentUser() != null) {
            this.currentUid = mAuth.getCurrentUser().getUid();
        } else {
            this.currentUid = "";
        }
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

        View.OnClickListener irAlPerfilListener =
                v -> {
                    // Intent dirigido a la Activity del perfil
                    Intent intent = new Intent(context, PerfilClienteActivity.class);

                    // Pasamos el UID del dueño de la publicación
                    intent.putExtra("UID_USUARIO", publicacion.getUidUsuario());

                    context.startActivity(intent);
                };

        // Asignamos el listener a la foto de perfil y al nombre de usuario
        holder.imgAvatarAutor.setOnClickListener(irAlPerfilListener);
        holder.tvUsernameAutor.setOnClickListener(irAlPerfilListener);

        // Configuramos el evento de Poner / Quitar like
        holder.imgLike.setOnClickListener(
                v -> {
                    // Si no hay currenUid es un usuario Invitado
                    if (currentUid == null || currentUid.isEmpty()) {
                        Toast.makeText(
                                        context,
                                        R.string.inicia_sesion_para_dar_like,
                                        Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    boolean isLiked =
                            holder.imgLike.getTag() != null && (boolean) holder.imgLike.getTag();
                    holder.imgLike.setEnabled(false); // Evita el doble click rápido

                    if (isLiked) {
                        // QUITAR LIKE
                        likeService
                                .eliminarLike(currentUid, publicacion.getUid())
                                .addOnSuccessListener(
                                        unused -> {
                                            marcarComoLike(holder, false);

                                            // Restamos 1 al contador de likes en tiempo real
                                            // Aunque se cambie en Firebase así evitamos tener que
                                            // hacer otra petición innecesaria para recoger este
                                            // dato
                                            long nuevosLikes = publicacion.getNumLikes() - 1;
                                            publicacion.setNumLikes(
                                                    Math.max(0, nuevosLikes)); // Evitar números
                                            // negativos
                                            holder.tvContadorLikes.setText(
                                                    String.valueOf(publicacion.getNumLikes()));

                                            holder.imgLike.setEnabled(true);
                                        })
                                .addOnFailureListener(e -> holder.imgLike.setEnabled(true));
                    } else {
                        // DAR LIKE
                        LikePublicacion nuevoLike =
                                LikePublicacion.builder()
                                        .uidUsuario(currentUid)
                                        .uidPublicacion(publicacion.getUid())
                                        .build();

                        likeService
                                .addLike(nuevoLike)
                                .addOnSuccessListener(
                                        unused -> {
                                            marcarComoLike(holder, true);

                                            // Sumamos 1 al contador de likes en tiempo real
                                            long nuevosLikes = publicacion.getNumLikes() + 1;
                                            publicacion.setNumLikes(nuevosLikes);
                                            holder.tvContadorLikes.setText(
                                                    String.valueOf(publicacion.getNumLikes()));

                                            holder.imgLike.setEnabled(true);
                                        })
                                .addOnFailureListener(e -> holder.imgLike.setEnabled(true));
                    }
                });
    }

    /**
     * Método auxiliar para cambiar el color y el estado del botón Like
     */
    private void marcarComoLike(PublicacionViewHolder holder, boolean isLiked) {
        holder.imgLike.setTag(isLiked);
        if (isLiked) {
            holder.imgLike.setColorFilter(Color.parseColor("#E91E63")); // Rojo
        } else {
            holder.imgLike.setColorFilter(Color.parseColor("#FFFFFF")); // Blanco
        }
    }

    /**
     * Consulta Firestore para obtener el username y la foto del autor de una publicación.
     */
    private void cargarDatosAutor(PublicacionViewHolder holder, String uidAutor) {
        // Ponemos valores por defecto mientras carga
        holder.tvUsernameAutor.setText(R.string.cargando);
        holder.imgAvatarAutor.setImageResource(R.drawable.avatar_default);

        usuarioService
                .getPerfil(uidAutor)
                .addOnSuccessListener(
                        usuario -> {
                            if (usuario != null) {
                                holder.tvUsernameAutor.setText("@" + usuario.getUsername());

                                if (usuario.getFotoPerfil() != null
                                        && !usuario.getFotoPerfil().isEmpty()) {
                                    Glide.with(context)
                                            .load(usuario.getFotoPerfil())
                                            .into(holder.imgAvatarAutor);
                                }
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            holder.tvUsernameAutor.setText(R.string.usuario_desconocido);
                            Log.e("PublicacionAdapter", "Error cargando autor: " + e.getMessage());
                        });
    }

    @Override
    public int getItemCount() {
        return listaPublicaciones.size();
    }

    public static class PublicacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsernameAutor, tvFecha, tvTexto, tvContadorLikes;
        ShapeableImageView imgAvatarAutor, imgPublicacion;
        ImageView imgLike;

        public PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsernameAutor = itemView.findViewById(R.id.tvUsernameAutor);
            imgAvatarAutor = itemView.findViewById(R.id.imgAvatarAutor);
            tvFecha = itemView.findViewById(R.id.tvFechaPublicacion);
            tvTexto = itemView.findViewById(R.id.tvTextoPublicacion);
            imgPublicacion = itemView.findViewById(R.id.imgPublicacion);
            imgLike = itemView.findViewById(R.id.imgLike);
            tvContadorLikes = itemView.findViewById(R.id.tvContadorLikes);
        }
    }
}
