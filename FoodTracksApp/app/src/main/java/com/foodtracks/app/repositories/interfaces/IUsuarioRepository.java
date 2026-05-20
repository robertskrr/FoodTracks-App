/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.repositories.interfaces;

import com.foodtracks.app.models.Usuario;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Definición de las operaciones permitidas sobre la colección de usuarios.
 *
 * @author Robert
 * @since 26/03
 */
public interface IUsuarioRepository {

    /**
     * Guarda un nuevo usuario o actualiza uno existente.
     *
     * @param usuario que extiende de {@link Usuario} (Cliente, Local o Admin).
     * @return {@link Task} que representa el estado de la operación de escritura.
     */
    Task<Void> saveUsuario(Usuario usuario);

    /**
     * Recupera un documento de usuario específico mediante UID.
     *
     * @param uid Identificador del usuario.
     * @return {@link Task} con el {@link DocumentSnapshot} del usuario encontrado.
     */
    Task<DocumentSnapshot> getUsuarioById(String uid);

    /**
     * Realiza una consulta para encontrar un usuario basado en su username único.
     *
     * @param username Nombre de usuario a buscar.
     * @return {@link Task} con el {@link QuerySnapshot} que contiene los resultados (lista) de la búsqueda.
     */
    Task<QuerySnapshot> getUsuarioByUsername(String username);

    /**
     * Elimina físicamente el documento de un usuario de la colección.
     *
     * @param uid Identificador del usuario a eliminar.
     * @return {@link Task} que representa el estado de la eliminación.
     */
    Task<Void> deleteUsuario(String uid);

    /**
     * Busca usuarios cuyo nombre de usuario comience por una cadena específica.
     *
     * @param field Campo concreto de búsqueda
     * @param query Texto introducido por el usuario.
     * @return {@link Task} con el {@link QuerySnapshot} con la lista de posibles coincidencias.
     */
    Task<QuerySnapshot> searchUsuariosByField(String field, String query);

    /**
     * Busca locales aplicando filtros de ciudad y preferencias alimenticias.
     * @return {@link Task} con el {@link QuerySnapshot} con la lista de locales.
     */
    Task<QuerySnapshot> searchLocalesByFiltros(
            String ciudad,
            boolean vegano,
            boolean vegetariano,
            boolean sinLactosa,
            boolean celiaco,
            String otraPreferencia);

    /**
     * Busca locales cuyo nombre de usuario comience por una cadena específica.
     *
     * @param username Nombre de usuario.
     * @return {@link Task} con el {@link QuerySnapshot} con la lista de posibles coincidencias.
     */
    public Task<QuerySnapshot> searchLocalesByUsername(String username);

    /**
     * Incrementa atómicamente el contador de visitas del perfil de un local.
     *
     * @param uidLocal Identificador del local visitado.
     * @return {@link Task} que representa el éxito o fallo de la operación.
     */
    Task<Void> incrementarVisitasPerfil(String uidLocal);

    /**
     * Recoge los últimos usuarios registrados en la aplicación.
     * @param limite Número límite de usuarios.
     * @return {@link Task} con el {@link QuerySnapshot} con la lista de los usuarios.
     */
    Task<QuerySnapshot> getUltimosUsuariosRegistrados(int limite);
}
