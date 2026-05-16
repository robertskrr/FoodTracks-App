/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.foodtracks.app.R;
import com.foodtracks.app.activities.cliente.PerfilClienteActivity;
import com.foodtracks.app.activities.local.PerfilLocalActivity;
import com.foodtracks.app.fragments.VisorImagenDialogFragment;
import com.foodtracks.app.models.LikePublicacion;
import com.foodtracks.app.models.Publicacion;
import com.foodtracks.app.models.Usuario;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.interfaces.ILikeService;
import com.foodtracks.app.services.interfaces.IPublicacionService;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.foodtracks.app.utils.DateUtils;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
    private final IPublicacionService publicacionService;
    private final ILikeService likeService;
    private final String currentUid; // Usuario que está usando la publicación
    private boolean esAdmin;
    private final boolean esLogueado;
    private final String MOTIVO_DEFAULT = "Incumplimiento de las normas de la comunidad.";
    private FirebaseAuth mAuth;
    // Memoria caché para no descargar el mismo perfil varias veces
    private final Map<String, Usuario> cacheUsuarios = new HashMap<>();

    public PublicacionAdapter(List<Publicacion> listaPublicaciones, Context context) {
        this.listaPublicaciones = listaPublicaciones;
        this.context = context;
        this.usuarioService = ServiceFactory.provideUsuarioService(context);
        this.likeService = ServiceFactory.provideLikeService();
        this.publicacionService = ServiceFactory.providePublicacionService(context);
        mAuth = FirebaseAuth.getInstance();

        // Obtenemos el usuario logueado para comprobar sus propios likes
        if (mAuth.getCurrentUser() != null) {
            this.currentUid = mAuth.getCurrentUser().getUid();
            esLogueado = true;
            comprobarAdmin();
        } else {
            this.currentUid = "";
            esLogueado = false;
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

        holder.tvContadorLikes.setText(String.valueOf(publicacion.getNumLikes()));

        // Cargamos la imagen de la publicación (si existe)
        if (publicacion.getImagen() != null && !publicacion.getImagen().isEmpty()) {
            holder.imgPublicacion.setVisibility(View.VISIBLE);
            Glide.with(context).load(publicacion.getImagen()).into(holder.imgPublicacion);

            // Abre la imagen en pantalla completa
            holder.imgPublicacion.setOnClickListener(v -> {
                if (context instanceof AppCompatActivity) {
                    AppCompatActivity activity = (AppCompatActivity) context;
                    VisorImagenDialogFragment dialog = VisorImagenDialogFragment.newInstance(publicacion.getImagen());
                    dialog.show(activity.getSupportFragmentManager(), "VisorImagenCompleta");
                }
            });
        } else {
            holder.imgPublicacion.setVisibility(View.GONE);
        }

        // Cargamos los datos del autor de la publicación
        cargarDatosAutor(holder, publicacion.getUidUsuario());
        cargarDatosLocalMencionado(holder, publicacion.getUidLocal());
        comprobarLikeInicial(holder, publicacion.getUid());

        // La papelera solo es visible si el usuario actual es el autor
        if (esLogueado && currentUid.equals(publicacion.getUidUsuario())) {
            holder.imgEliminarPublicacion.setVisibility(View.VISIBLE);
            holder.imgEliminarPublicacion.setOnClickListener(v -> {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    mostrarDialogoEliminar(publicacion, adapterPosition);
                }
            });
        } else if (esAdmin) { // Si es administrador puede borrarla
            // Oculta el botón para hacer la siguiente comprobación: Que no sea de otro admin
            holder.imgEliminarPublicacion.setVisibility(View.GONE);

            // Comprobamos el rol del autor en memoria caché (que no sea admin)
            if (cacheUsuarios.containsKey(publicacion.getUidUsuario()) && cacheUsuarios.get(publicacion.getUidUsuario()) != null) {
                Usuario autor = cacheUsuarios.get(publicacion.getUidUsuario());
                // Si no lo es, activamos la opción de borrar
                if (!"admin".equals(autor.getRol())) {
                    holder.imgEliminarPublicacion.setVisibility(View.VISIBLE);
                    holder.imgEliminarPublicacion.setOnClickListener(v -> {
                        int adapterPosition = holder.getBindingAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mostrarDialogoEliminarByAdmin(publicacion, adapterPosition);
                        }
                    });
                }
            } else {
                // Si no está en la memoria caché, lo consultamos en la base de datos
                usuarioService.getPerfil(publicacion.getUidUsuario()).addOnSuccessListener(autor -> {
                    if (autor != null && !"admin".equals(autor.getRol())) {
                        holder.imgEliminarPublicacion.setVisibility(View.VISIBLE);
                        holder.imgEliminarPublicacion.setOnClickListener(v -> {
                            int adapterPosition = holder.getBindingAdapterPosition();
                            if (adapterPosition != RecyclerView.NO_POSITION) {
                                mostrarDialogoEliminarByAdmin(publicacion, adapterPosition);
                            }
                        });
                    }
                });
            }

        } else {
            holder.imgEliminarPublicacion.setVisibility(View.GONE);
        }

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

                    if (isLiked) {
                        // Simulamos quitar el like rápidamente
                        marcarComoLike(holder, false);
                        long nuevosLikes = publicacion.getNumLikes() - 1;
                        publicacion.setNumLikes(Math.max(0, nuevosLikes));
                        holder.tvContadorLikes.setText(String.valueOf(publicacion.getNumLikes()));

                        // Petición en segundo plano
                        likeService.eliminarLike(currentUid, publicacion.getUid())
                                .addOnFailureListener(e -> {
                                    // Si falla deshacemos el cambio visual
                                    marcarComoLike(holder, true);
                                    publicacion.setNumLikes(publicacion.getNumLikes() + 1);
                                    holder.tvContadorLikes.setText(String.valueOf(publicacion.getNumLikes()));
                                });
                    } else {
                        // Simulamos dar like rápidamente
                        sonidoLike();
                        marcarComoLike(holder, true);
                        long nuevosLikes = publicacion.getNumLikes() + 1;
                        publicacion.setNumLikes(nuevosLikes);
                        holder.tvContadorLikes.setText(String.valueOf(publicacion.getNumLikes()));

                        // Petición en segundo plano
                        LikePublicacion nuevoLike = LikePublicacion.builder()
                                .uidUsuario(currentUid)
                                .uidPublicacion(publicacion.getUid())
                                .build();

                        likeService.addLike(nuevoLike)
                                .addOnFailureListener(e -> {
                                    // Si falla deshacemos el cambio visual
                                    marcarComoLike(holder, false);
                                    publicacion.setNumLikes(Math.max(0, publicacion.getNumLikes() - 1));
                                    holder.tvContadorLikes.setText(String.valueOf(publicacion.getNumLikes()));
                                });
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
     * Reproducción del sonido de like.
     */
    private void sonidoLike() {
        // Comprueba las preferencias de "Sonidos silenciados"
        SharedPreferences prefs = context.getSharedPreferences("FoodTracksSettings", Context.MODE_PRIVATE);

        boolean sonidosSilenciados = prefs.getBoolean("sonidos_silenciados", false);

        // Si no la tiene activada reproduce el sonido
        if (!sonidosSilenciados){
            MediaPlayer mp = MediaPlayer.create(context, R.raw.like);
            mp.start();

            mp.setOnCompletionListener(mediaPlayer -> mp.release());
        }
    }

    private void comprobarAdmin() {
        usuarioService
                .getPerfil(currentUid)
                .addOnSuccessListener(
                        usuario -> {
                            String rol = usuario.getRol();
                            if ("admin".equals(rol)) {
                                esAdmin = true;
                                // Notifica al sistema que los datos se han actualizado
                                // Evitando que no carguen los iconos y lecturas innecesarias
                                notifyDataSetChanged();
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            Log.e("Comprobar admin en publicación", e.getMessage(), e);
                        });
    }

    /**
     * Consulta Firestore para obtener el username y la foto del autor de una publicación.
     */
    private void cargarDatosAutor(PublicacionViewHolder holder, String uidAutor) {
        // Si tenemos los datos en la memoria caché
        if (cacheUsuarios.containsKey(uidAutor) && cacheUsuarios.get(uidAutor) != null) {
            Usuario usuarioGuardado = cacheUsuarios.get(uidAutor);
            assert usuarioGuardado != null;
            pintarDatosAutor(holder, usuarioGuardado, uidAutor);
            return;
        }

        // Si no los tenemos, ponemos los valores por defecto y hacemos la consulta
        holder.tvUsernameAutor.setText(R.string.cargando);
        holder.imgAvatarAutor.setImageResource(R.drawable.avatar_default);

        // Desactivamos los clicks temporalmente para que el usuario no pulse antes de cargar
        holder.imgAvatarAutor.setOnClickListener(null);

        usuarioService
                .getPerfil(uidAutor)
                .addOnSuccessListener(
                        usuario -> {
                            if (usuario != null) {
                                // Guardamos al usuario en la memoria para la próxima vez
                                cacheUsuarios.put(uidAutor, usuario);

                                pintarDatosAutor(holder, usuario, uidAutor);
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            holder.tvUsernameAutor.setText(R.string.usuario_desconocido);
                            Log.e("PublicacionAdapter", "Error cargando autor: " + e.getMessage());
                        });
    }

    /**
     * Método auxiliar para pintar el nombre y la foto del usuario
     */
    private void pintarDatosAutor(PublicacionViewHolder holder, Usuario usuario, String uidAutor) {
        holder.tvUsernameAutor.setText("@" + usuario.getUsername());

        if (usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isEmpty()) {
            Glide.with(context).load(usuario.getFotoPerfil()).into(holder.imgAvatarAutor);
        }

        View.OnClickListener irAlPerfilListener =
                v -> {
                    Intent intent;
                    if ("local".equals(usuario.getRol())) {
                        intent = new Intent(context, PerfilLocalActivity.class);
                    } else {
                        intent = new Intent(context, PerfilClienteActivity.class);
                    }
                    intent.putExtra("UID_USUARIO", uidAutor);
                    context.startActivity(intent);
                };

        holder.imgAvatarAutor.setOnClickListener(irAlPerfilListener);
        holder.tvUsernameAutor.setOnClickListener(irAlPerfilListener);
    }

    /**
     * Carga el username del local mencionado.
     */
    private void cargarDatosLocalMencionado(PublicacionViewHolder holder, String uidLocal) {
        // Si no hay local mencionado
        if (uidLocal == null || uidLocal.trim().isEmpty()) {
            holder.tvLocalMencionado.setVisibility(View.GONE);
            holder.tvLocalMencionado.setOnClickListener(null);
            return;
        }

        holder.tvLocalMencionado.setVisibility(View.VISIBLE);
        holder.tvLocalMencionado.setText(R.string.cargando);

        // Comprobamos la memoria caché
        if (cacheUsuarios.containsKey(uidLocal) && cacheUsuarios.get(uidLocal) != null) {
            Usuario localGuardado = cacheUsuarios.get(uidLocal);
            assert localGuardado != null;
            pintarDatosLocalMencionado(holder, localGuardado, uidLocal);
            return;
        }

        // Si no está en la memoria caché, hacemos la consulta
        usuarioService
                .getPerfil(uidLocal)
                .addOnSuccessListener(
                        usuario -> {
                            if (usuario != null) {
                                cacheUsuarios.put(uidLocal, usuario); // Guardamos en la memoria
                                pintarDatosLocalMencionado(holder, usuario, uidLocal);
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            holder.tvLocalMencionado.setVisibility(View.GONE);
                        });
    }

    /**
     * Método auxiliar para pintar el username del local y configurar su click
     */
    private void pintarDatosLocalMencionado(
            PublicacionViewHolder holder, Usuario local, String uidLocal) {
        holder.tvLocalMencionado.setText("@" + local.getUsername());

        // Username clickable que te lleva al perfil del local
        holder.tvLocalMencionado.setOnClickListener(
                v -> {
                    Intent intent = new Intent(context, PerfilLocalActivity.class);
                    intent.putExtra("UID_USUARIO", uidLocal);
                    context.startActivity(intent);
                });
    }

    /**
     * Consulta si el usuario actual ya le había dado like a esta publicación
     * para pintar el corazón del color correcto al abrir la pantalla.
     * @param holder ViewHolder de la publicación
     * @param uidPublicacion Identificador de la publicación
     */
    private void comprobarLikeInicial(PublicacionViewHolder holder, String uidPublicacion) {
        if (currentUid == null || currentUid.isEmpty()) {
            marcarComoLike(holder, false);
            return;
        }

        // Mientras comprobamos, bloqueamos el botón para que el usuario no pueda hacer click
        holder.imgLike.setEnabled(false);

        likeService
                .getLike(currentUid, uidPublicacion)
                .addOnSuccessListener(
                        like -> {
                            // Si no existe, lo dejamos blanco
                            marcarComoLike(holder, like != null); // Si existe, lo pintamos de rojo
                            holder.imgLike.setEnabled(true);
                        })
                .addOnFailureListener(
                        e -> {
                            marcarComoLike(holder, false);
                            holder.imgLike.setEnabled(true);
                        });
    }

    /**
     * Muestra una alerta de confirmación de eliminación.
     */
    private void mostrarDialogoEliminar(Publicacion publicacion, int position) {
        AlertDialog dialog =
                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.delete_publicacion)
                        .setMessage(R.string.confirm_delete_publicacion)
                        .setPositiveButton(
                                R.string.eliminar,
                                (dialogInterface, which) -> {
                                    publicacionService
                                            .eliminarPublicacion(publicacion.getUid())
                                            .addOnSuccessListener(
                                                    unused -> {
                                                        Toast.makeText(
                                                                        context,
                                                                        R.string
                                                                                .publicacion_eliminada,
                                                                        Toast.LENGTH_SHORT)
                                                                .show();

                                                        // Eliminamos el elemento de la lista de
                                                        // publicaciones
                                                        listaPublicaciones.remove(position);
                                                        // Animación de borrado del recycler View
                                                        notifyItemRemoved(position);
                                                        // Actualizamos los nuevos índices de la
                                                        // lista
                                                        notifyItemRangeChanged(
                                                                position,
                                                                listaPublicaciones.size());
                                                    })
                                            .addOnFailureListener(
                                                    e -> {
                                                        Log.e(
                                                                "PublicacionAdapter",
                                                                "Error al eliminar: "
                                                                        + e.getMessage(),
                                                                e);
                                                        Toast.makeText(
                                                                        context,
                                                                        R.string.error_al_eliminar,
                                                                        Toast.LENGTH_SHORT)
                                                                .show();
                                                    });
                                })
                        .setNegativeButton(
                                R.string.cancelar,
                                (dialogInterface, which) -> {
                                    dialogInterface.dismiss();
                                })
                        .show();

        // Colores de texto de los botones
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }

    /**
     * Muestra un diálogo de eliminación exclusivo para administradores,
     * requiriendo un motivo para el registro de auditoría.
     */
    private void mostrarDialogoEliminarByAdmin(Publicacion publicacion, int position) {
        // Creamos el editText
        EditText inputMotivo = new EditText(context);
        inputMotivo.setHint(R.string.motivo_eliminacion);
        inputMotivo.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        // Lo envolvemos en un container para poder dar márgenes (margin)
        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 50;
        params.rightMargin = 50;
        inputMotivo.setLayoutParams(params);
        container.addView(inputMotivo);

        // Diálogo de confirmación de la eliminación
        AlertDialog dialog =
                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.eliminar_como_administrador)
                        .setMessage(R.string.indica_el_motivo_eliminacion)
                        .setView(container)
                        .setPositiveButton(
                                R.string.eliminar,
                                (dialogInterface, which) -> {
                                    String motivo = inputMotivo.getText().toString().trim();

                                    // Motivo por defecto, por si no escribe nada
                                    if (motivo.isEmpty()) {
                                        motivo = MOTIVO_DEFAULT;
                                    }

                                    publicacionService
                                            .eliminarPublicacionByAdmin(
                                                    publicacion.getUid(),
                                                    publicacion.getUidUsuario(),
                                                    motivo,
                                                    currentUid)
                                            .addOnSuccessListener(
                                                    unused -> {
                                                        Toast.makeText(
                                                                        context,
                                                                        R.string
                                                                                .publicacion_eliminada_by_admin,
                                                                        Toast.LENGTH_SHORT)
                                                                .show();
                                                        listaPublicaciones.remove(position);
                                                        notifyItemRemoved(position);
                                                        notifyItemRangeChanged(
                                                                position,
                                                                listaPublicaciones.size());
                                                    })
                                            .addOnFailureListener(
                                                    e -> {
                                                        Log.e(
                                                                "PublicacionAdapter",
                                                                "Error de admin al eliminar: "
                                                                        + e.getMessage(),
                                                                e);
                                                        Toast.makeText(
                                                                        context,
                                                                        R.string.error_al_eliminar,
                                                                        Toast.LENGTH_SHORT)
                                                                .show();
                                                    });
                                })
                        .setNegativeButton(
                                R.string.cancelar,
                                (dialogInterface, which) -> {
                                    dialogInterface.dismiss();
                                })
                        .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }

    @Override
    public int getItemCount() {
        return listaPublicaciones.size();
    }

    public static class PublicacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsernameAutor, tvFecha, tvTexto, tvContadorLikes, tvLocalMencionado;
        ShapeableImageView imgAvatarAutor, imgPublicacion;
        ImageView imgLike, imgEliminarPublicacion;

        public PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsernameAutor = itemView.findViewById(R.id.tvUsernameAutor);
            imgAvatarAutor = itemView.findViewById(R.id.imgAvatarAutor);
            tvFecha = itemView.findViewById(R.id.tvFechaPublicacion);
            tvTexto = itemView.findViewById(R.id.tvTextoPublicacion);
            imgPublicacion = itemView.findViewById(R.id.imgPublicacion);
            imgLike = itemView.findViewById(R.id.imgLike);
            tvContadorLikes = itemView.findViewById(R.id.tvContadorLikes);
            tvLocalMencionado = itemView.findViewById(R.id.tvLocalMencionado);
            imgEliminarPublicacion = itemView.findViewById(R.id.imgEliminarPublicacion);
        }
    }
}
