/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.services;

import com.foodtracks.app.R;
import com.foodtracks.app.models.LikePublicacion;
import com.foodtracks.app.repositories.interfaces.ILikeRepository;
import com.foodtracks.app.repositories.interfaces.IPublicacionRepository;
import com.foodtracks.app.services.exceptions.FoodTracksNotFoundException;
import com.foodtracks.app.services.exceptions.FoodTracksValidationException;
import com.foodtracks.app.services.interfaces.ILikeService;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Lógica de negocio para la gestión de likes de usuarios sobre publicaciones.
 *
 * @author Robert
 * @since 18/04
 */
public class LikeService implements ILikeService {
    private final ILikeRepository likeRepository;
    private final IPublicacionRepository publicacionRepository;

    public LikeService(
            ILikeRepository likeRepository, IPublicacionRepository publicacionRepository) {
        this.likeRepository = likeRepository;
        this.publicacionRepository = publicacionRepository;
    }

    @Override
    public Task<LikePublicacion> getLike(String uidUsuario, String uidPublicacion) {
        return likeRepository
                .getLike(getCustomId(uidUsuario, uidPublicacion))
                .continueWithTask(
                        task -> {
                            DocumentSnapshot doc = task.getResult();

                            if (task.isSuccessful() && doc != null && doc.exists()) {
                                return Tasks.forResult(doc.toObject(LikePublicacion.class));
                            } else {
                                return Tasks.forException(
                                        new FoodTracksNotFoundException(
                                                R.string.like_not_found_error_message));
                            }
                        });
    }

    @Override
    public Task<Void> addLike(LikePublicacion like) {
        String customId = getCustomId(like.getUidUsuario(), like.getUidPublicacion());
        like.setUid(customId);
        like.setFechaHora(Timestamp.now());

        // Comprobamos si el usuario ya le había dado like
        return likeRepository
                .getLike(customId)
                .continueWithTask(
                        task -> {
                            DocumentSnapshot doc = task.getResult();

                            if (task.isSuccessful() && doc != null && doc.exists()) {
                                return Tasks.forException(
                                        new FoodTracksValidationException(
                                                R.string.like_duplicated_error_message));
                            } else {
                                // Si no existe, procedemos a guardar e incrementar el contador
                                return likeRepository
                                        .saveLike(like)
                                        .continueWithTask(
                                                unused ->
                                                        publicacionRepository
                                                                .actualizarContadorLikes(
                                                                        like.getUidPublicacion(),
                                                                        1));
                            }
                        });
    }

    @Override
    public Task<Void> eliminarLike(String uidUsuario, String uidPublicacion) {
        String customId = getCustomId(uidUsuario, uidPublicacion);

        // Comprobamos si el like existe
        return likeRepository
                .getLike(customId)
                .continueWithTask(
                        task -> {
                            DocumentSnapshot doc = task.getResult();

                            if (task.isSuccessful() && doc != null && doc.exists()) {
                                // Si existe, lo borramos y decrementamos el contador
                                return likeRepository
                                        .deleteLike(customId)
                                        .continueWithTask(
                                                unused ->
                                                        publicacionRepository
                                                                .actualizarContadorLikes(
                                                                        uidPublicacion, -1));
                            } else {
                                return Tasks.forException(
                                        new FoodTracksNotFoundException(
                                                R.string.like_not_found_error_message));
                            }
                        });
    }

    /*
     * ================================================================
     * ==================== Private helpers ===========================
     * ================================================================
     */

    private String getCustomId(String uidUsuario, String uidPublicacion) {
        return uidUsuario + "_" + uidPublicacion;
    }
}
