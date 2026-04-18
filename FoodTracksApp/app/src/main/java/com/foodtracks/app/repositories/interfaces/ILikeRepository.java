/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.repositories.interfaces;

import com.foodtracks.app.models.LikePublicacion;
import com.foodtracks.app.models.Publicacion;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Definición de las operaciones permitidas sobre los likes a publicaciones.
 *
 * @author Robert
 * @since 18/04
 */
public interface ILikeRepository {

    /**
     * Registra un nuevo "Me gusta" en la colección.
     * @param like Like de la publicación.
     * @return {@link Task} que representa el estado de la operación.
     */
    Task<Void> saveLike(LikePublicacion like);

    /**
     * Elimina un "Me gusta" existente.
     * @param uidLike Identificador del like.
     * @return {@link Task} que representa el estado de la operación.
     */
    Task<Void> deleteLike(String uidLike);

    /**
     * Comprueba si un usuario ya ha dado like a una publicación.
     * @param uidLike Identificador del like.
     * @return {@link Task} con el {@link DocumentSnapshot} del like encontrado.
     */
    Task<DocumentSnapshot> getLike(String uidLike);
}
