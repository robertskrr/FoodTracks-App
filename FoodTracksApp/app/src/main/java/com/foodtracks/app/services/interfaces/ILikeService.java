/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.services.interfaces;

import com.foodtracks.app.models.LikePublicacion;
import com.google.android.gms.tasks.Task;

import java.util.List;

/**
 * Lógica de negocio para la gestión de likes de publicaciones.
 * Actúa como intermediario entre la UI y el repositorio de datos.
 *
 * @author Robert
 * @since 18/04
 */
public interface ILikeService {

    /**
     * Recupera el estado del like de un usuario sobre una publicación.
     *
     * @param uidUsuario Identificador del usuario que ha dado like.
     * @param uidPublicacion Identificador de la publicación.
     * @return {@link Task} que contiene el objeto {@link LikePublicacion}.
     */
    Task<LikePublicacion> getLike(String uidUsuario, String uidPublicacion);


    // Task<List<Publicacion>> getLikesByUser(String uidUsuario); TODO --> Si tengo tiempo

    /**
     * Añade un like a una publicación.
     *
     * @param like Objeto con los datos del like.
     * @return {@link Task} que representa el estado de la operación.
     */
    Task<Void> addLike(LikePublicacion like);

    /**
     * Elimina un like y actualiza las estadísticas de la publicación.
     *
     * @param uidUsuario Identificador del usuario que ha dado like.
     * @param uidPublicacion Identificador de la publicación.
     * @return {@link Task} con el resultado final del proceso.
     */
    Task<Void> eliminarLike(String uidUsuario, String uidPublicacion);

    // TODO --> Si se elimina un usuario hay que eliminar todos sus registros, likes también.
    // Dejar para cuando esté casi todo montado
}
