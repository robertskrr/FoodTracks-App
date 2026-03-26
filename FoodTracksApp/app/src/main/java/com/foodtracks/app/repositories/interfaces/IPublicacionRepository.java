/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.repositories.interfaces;

import com.foodtracks.app.models.Publicacion;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Definición de las operaciones permitidas sobre la colección de publicaciones
 *
 * @author Robert
 * @since 26/03
 */
public interface IPublicacionRepository {

    /**
     * Guarda una nueva publicación
     *
     * @param publicacion Objeto Publicación
     * @return {@link Task} que representa el estado de la operación de escritura
     */
    Task<Void> savePublicacion(Publicacion publicacion);

    /**
     * Recupera un documento de publicación específica mediante UID
     *
     * @param uid Identificador de la publicación
     * @return {@link Task} con el {@link DocumentSnapshot} de la publicación encontrada
     */
    Task<DocumentSnapshot> getPublicacionById(String uid);

    /**
     * Elimina físicamente el documento de una publicacion de la colección
     *
     * @param uid Identificador de la publicación a eliminar
     * @return {@link Task} que representa el estado de la eliminación
     */
    Task<Void> deletePublicacion(String uid);

    /**
     * Recupera todas las publicaciones de un usuario específico
     *
     * @param uidUsuario ID del usuario autor
     * @return Task con la lista de publicaciones
     */
    Task<QuerySnapshot> getPublicacionesByUsuario(String uidUsuario);

    /**
     * Recupera todas las publicaciones asociadas a un local específico
     *
     * @param uidLocal ID del establecimiento.
     * @return Task con la lista de publicaciones
     */
    Task<QuerySnapshot> getPublicacionesByLocal(String uidLocal);

    /**
     * Recupera una página de publicaciones.
     *
     * @param lastVisible El último documento cargado (null si es la primera página).
     * @return Task con el siguiente bloque de resultados.
     */
    Task<QuerySnapshot> getAllPublicaciones(DocumentSnapshot lastVisible);
}
