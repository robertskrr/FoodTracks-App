/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.repositories.interfaces;

import com.foodtracks.app.models.Usuario;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Definición de las operaciones permitidas sobre la colección de usuarios
 *
 * @author Robert
 * @since 26/03
 */
public interface IUsuarioRepository {

    /**
     * Guarda un nuevo usuario o actualiza uno existente
     *
     * @param usuario que extiende de {@link Usuario} (Cliente, Local o Admin)
     * @return {@link Task} que representa el estado de la operación de escritura
     */
    Task<Void> saveUsuario(Usuario usuario);

    /**
     * Recupera un documento de usuario específico mediante UID
     *
     * @param uid Identificador del usuario
     * @return {@link Task} con el {@link DocumentSnapshot} del usuario encontrado
     */
    Task<DocumentSnapshot> getUsuarioById(String uid);

    /**
     * Realiza una consulta para encontrar un usuario basado en su username único
     *
     * @param username Nombre de usuario a buscar
     * @return {@link Task} con el {@link QuerySnapshot} que contiene los resultados (lista) de la
     *     búsqueda
     */
    Task<QuerySnapshot> getUsuarioByUsername(String username);

    /**
     * Elimina físicamente el documento de un usuario de la colección
     *
     * @param uid Identificador del usuario a eliminar
     * @return {@link Task} que representa el estado de la eliminación
     */
    Task<Void> deleteUsuario(String uid);

    /**
     * Busca usuarios cuyo nombre de usuario comience por una cadena específica
     *
     * @param query Texto introducido por el usuario
     * @return Task con la lista de posibles coincidencias
     */
    Task<QuerySnapshot> searchUsuariosByUsername(String query);
}
