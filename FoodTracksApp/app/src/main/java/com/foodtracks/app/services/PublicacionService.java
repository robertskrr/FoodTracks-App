/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.services;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

import com.foodtracks.app.R;
import com.foodtracks.app.api.imagekit.ImageKitResponse;
import com.foodtracks.app.models.Publicacion;
import com.foodtracks.app.models.RegistroBorradoPublicacion;
import com.foodtracks.app.repositories.interfaces.ILikeRepository;
import com.foodtracks.app.repositories.interfaces.IPublicacionRepository;
import com.foodtracks.app.repositories.interfaces.IRegistroBorradoRepository;
import com.foodtracks.app.repositories.interfaces.IStorageRepository;
import com.foodtracks.app.repositories.interfaces.IUsuarioRepository;
import com.foodtracks.app.services.exceptions.FoodTracksNotFoundException;
import com.foodtracks.app.services.exceptions.FoodTracksValidationException;
import com.foodtracks.app.services.interfaces.IPublicacionService;
import com.foodtracks.app.utils.StringUtils;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Lógica de negocio para la gestión de publicaciones.
 *
 * @author Robert
 * @since 12/04
 */
public class PublicacionService implements IPublicacionService {
    private final IPublicacionRepository publicacionRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IRegistroBorradoRepository registroBorradoRepository;
    private final IStorageRepository storageRepository;
    private final ILikeRepository likeRepository;

    public PublicacionService(
            IPublicacionRepository publicacionRepository,
            IUsuarioRepository usuarioRepository,
            IRegistroBorradoRepository registroBorradoRepository,
            IStorageRepository storageRepository,
            ILikeRepository likeRepository) {
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.registroBorradoRepository = registroBorradoRepository;
        this.storageRepository = storageRepository;
        this.likeRepository = likeRepository;
    }

    @Override
    public Task<Publicacion> getPublicacion(String uid) {
        return publicacionRepository
                .getPublicacionById(uid)
                .continueWithTask(
                        task -> {
                            DocumentSnapshot doc = task.getResult();
                            if (task.isSuccessful() && doc != null && doc.exists()) {
                                return Tasks.forResult(doc.toObject(Publicacion.class));
                            } else {
                                return Tasks.forException(
                                        new FoodTracksNotFoundException(
                                                R.string.publicacion_not_found_error_message));
                            }
                        });
    }

    @Override
    public Task<List<Publicacion>> getAllPublicaciones() {
        // Pasamos null como checkpoint para cargar la primera página (50 posts)
        return publicacionRepository
                .getAllPublicaciones(null) // TODO --> Check si marca el checkpoint
                .continueWith(
                        task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                return task.getResult().toObjects(Publicacion.class);
                            }
                            return new java.util.ArrayList<>();
                        });
    }

    @Override
    public Task<List<RegistroBorradoPublicacion>> getAllRegistrosBorradoPublicaciones() {
        return registroBorradoRepository
                .getAllRegistrosPublicaciones()
                .continueWith(
                        task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                return task.getResult().toObjects(RegistroBorradoPublicacion.class);
                            }
                            return new java.util.ArrayList<>();
                        });
    }

    @Override
    public Task<List<Publicacion>> getPublicacionesByUsuario(String uidUsuario) {
        return publicacionRepository
                .getPublicacionesByUsuario(uidUsuario)
                .continueWith(
                        task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                return task.getResult().toObjects(Publicacion.class);
                            } else {
                                return new ArrayList<>(); // Devolvemos lista vacia
                            }
                        });
    }

    @Override
    public Task<List<Publicacion>> getPublicacionesByLocalMencionado(String uidLocal) {
        return publicacionRepository
                .getPublicacionesByLocal(uidLocal)
                .continueWith(
                        task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                return task.getResult().toObjects(Publicacion.class);
                            }
                            return new java.util.ArrayList<>();
                        });
    }

    @Override
    public Task<Void> subirPublicacion(Publicacion publicacion, Uri fotoUri) {
        int errorIdValidacion = validarDatos(publicacion);
        if (errorIdValidacion != 0) {
            return Tasks.forException(new FoodTracksValidationException(errorIdValidacion));
        }

        publicacion.setFechaHora(Timestamp.now());
        normalizarDatos(publicacion);

        if (fotoUri != null) {
            return storageRepository
                    .uploadImage(
                            fotoUri,
                            publicacion.getUidUsuario() + "_pub_" + System.currentTimeMillis(),
                            "publicaciones")
                    .continueWithTask(
                            uploadTask -> {
                                ImageKitResponse res = uploadTask.getResult();

                                publicacion.setImagen(res.getUrl());
                                publicacion.setImagenId(res.getFileId());

                                return publicacionRepository.savePublicacion(publicacion);
                            });
        } else {
            return publicacionRepository.savePublicacion(publicacion);
        }
    }

    @Override
    public Task<Void> eliminarPublicacion(String uid) {
        return publicacionRepository
                .getPublicacionById(uid)
                .continueWithTask(
                        task -> {
                            DocumentSnapshot doc = task.getResult();
                            if (!doc.exists()) {
                                return Tasks.forException(
                                        new FoodTracksNotFoundException(
                                                R.string.publicacion_not_found_error_message));
                            }

                            Publicacion publicacion = doc.toObject(Publicacion.class);

                            // Borramos foto si la hay
                            if (publicacion != null && publicacion.getImagenId() != null) {
                                storageRepository.deleteImage(publicacion.getImagenId());
                            }

                            // Borramos los likes asociados a la publicación
                            return likeRepository
                                    .deleteAllLikesByPublicacion(uid)
                                    // Borramos la publicación
                                    .continueWithTask(
                                            unused -> publicacionRepository.deletePublicacion(uid));
                        });
    }

    @Override
    public Task<Void> eliminarPublicacionByAdmin(
            String uid, String uidUsuario, String motivo, String uidAdmin) {
        return publicacionRepository
                .getPublicacionById(uid)
                .continueWithTask(
                        task -> {
                            DocumentSnapshot doc = task.getResult();
                            if (!doc.exists()) {
                                return Tasks.forException(
                                        new FoodTracksNotFoundException(
                                                R.string.publicacion_not_found_error_message));
                            }

                            Publicacion publicacion = doc.toObject(Publicacion.class);

                            // Buscamos al usuario para tener su username
                            return usuarioRepository
                                    .getUsuarioById(uidUsuario)
                                    .continueWithTask(
                                            userTask -> {
                                                DocumentSnapshot usuarioDoc = userTask.getResult();
                                                String username = "Usuario desconocido";

                                                if (usuarioDoc.exists()) {
                                                    String usernameObtenido =
                                                            usuarioDoc.getString("username");
                                                    if (usernameObtenido != null) {
                                                        username = usernameObtenido;
                                                    }
                                                }

                                                // Borra la foto de ImageKit
                                                if (publicacion != null
                                                        && publicacion.getImagenId() != null) {
                                                    storageRepository.deleteImage(
                                                            publicacion.getImagenId());
                                                }

                                                RegistroBorradoPublicacion registro =
                                                        RegistroBorradoPublicacion.builder()
                                                                .uidAdmin(uidAdmin)
                                                                .uidPublicacion(uid)
                                                                .uidUsuario(uidUsuario)
                                                                .usernameUsuario(username)
                                                                .motivo(motivo)
                                                                .fechaHora(Timestamp.now())
                                                                .build();

                                                // Guardado del registro -> Borrado de likes ->
                                                // Borrado de publicación
                                                return registroBorradoRepository
                                                        .saveRegistroBorradoPublicacion(registro)
                                                        .continueWithTask(
                                                                unused ->
                                                                        likeRepository
                                                                                .deleteAllLikesByPublicacion(
                                                                                        uid))
                                                        .continueWithTask(
                                                                unused ->
                                                                        publicacionRepository
                                                                                .deletePublicacion(
                                                                                        uid));
                                            });
                        });
    }

    /*
     * ================================================================
     * ==================== Private helpers ===========================
     * ================================================================
     */

    /**
     * Normaliza los datos de la publicación a registrar.
     *
     * @param publicacion Publicación a normalizar.
     */
    private void normalizarDatos(Publicacion publicacion) {
        if (publicacion.getTexto() != null) {
            publicacion.setTexto(StringUtils.capitalizePrimeraLetra(publicacion.getTexto()));
        }
    }

    /**
     * Valida los datos de la publicación.
     *
     * @param publicacion Usuario a validar.
     * @return 0 si es todo correcto, en caso contrario devuelve el mensaje de error.
     */
    private int validarDatos(Publicacion publicacion) {
        if (publicacion.getTexto().isEmpty()) {
            return R.string.publicacion_text_empty_error_message;
        }

        // Validar tamaño máximo de caracteres
        if (publicacion.getTexto().length() > 500) {
            return R.string.publicacion_too_long_error_message;
        }

        return 0;
    }
}
