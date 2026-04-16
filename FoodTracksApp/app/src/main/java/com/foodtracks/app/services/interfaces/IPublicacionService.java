/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.services.interfaces;

import java.util.List;

import android.net.Uri;

import com.foodtracks.app.models.Publicacion;

import com.google.android.gms.tasks.Task;

/**
 * Lógica de negocio para la gestión de publicaciones.
 * Actúa como intermediario entre la UI y el repositorio de datos.
 *
 * @author Robert
 * @since 11/04
 */
public interface IPublicacionService {

    /**
     * Recupera una publicación.
     *
     * @param uid Identificador único de la publicación.
     * @return {@link Task} que contiene el objeto {@link Publicacion}.
     */
    Task<Publicacion> getPublicacion(String uid);

    /**
     * Recupera el listado completo de todas las publicaciones del sistema.
     * Útil para el feed principal.
     *
     * @return {@link Task} que contiene una lista de publicaciones {@link Publicacion}.
     */
    Task<List<Publicacion>> getAllPublicaciones();

    /**
     * Recupera todas las publicaciones realizadas por un autor específico.
     * Se utiliza para mostrar el historial en el perfil del usuario.
     *
     * @param uidUsuario ID del autor (cliente o local).
     * @return {@link Task} que contiene la lista de publicaciones del usuario.
     */
    Task<List<Publicacion>> getPublicacionesByUsuario(String uidUsuario);

    /**
     * Recupera las publicaciones donde se ha etiquetado o mencionado a un local específico.
     *
     * @param uidLocal Identificador único del local mencionado.
     * @return {@link Task} que contiene la lista de publicaciones asociadas al local.
     */
    Task<List<Publicacion>> getPublicacionesByLocalMencionado(String uidLocal);

    /**
     * Sube una nueva publicación.
     *
     * @param publicacion Objeto con la información de la nueva publicación.
     * @param fotoUri Uri de la foto adjunta (opcional).
     * @return {@link Task} que representa el estado de la operación.
     */
    Task<Void> subirPublicacion(Publicacion publicacion, Uri fotoUri);

    /**
     * Gestiona la eliminación de una publicación por parte del usuario dueño.
     *
     * @param uid ID de la publicación a eliminar.
     * @return {@link Task} con el resultado final del proceso.
     */
    Task<Void> eliminarPublicacion(String uid);

    /**
     * Gestiona la eliminación de una publicación por parte de un administrador,
     * registrando previamente la acción en el log de auditoría.
     *
     * @param uid ID de la publicación a eliminar.
     * @param uidUsuario Identificador del dueño de la publicación (para el registro).
     * @param motivo Razón justificada del borrado.
     * @param uidAdmin Identificador del administrador que ejecuta la acción.
     * @return {@link Task} que representa el estado final del proceso de borrado.
     */
    Task<Void> eliminarPublicacionByAdmin(
            String uid, String uidUsuario, String motivo, String uidAdmin);
}
