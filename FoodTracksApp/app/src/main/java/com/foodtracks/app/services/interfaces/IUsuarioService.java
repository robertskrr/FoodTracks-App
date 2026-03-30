/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.services.interfaces;

import java.util.List;

import com.foodtracks.app.models.Usuario;

import com.google.android.gms.tasks.Task;

/**
 * Lógica de negocio para la gestión de usuarios. Actúa como intermediario entre la UI y el
 * repositorio de datos
 *
 * @author Robert
 * @since 30/03
 */
public interface IUsuarioService {

    /**
     * Recupera el perfil completo de un usuario
     *
     * @param uid Identificador único del usuario
     * @return {@link Task} que contiene el objeto {@link Usuario}
     */
    Task<Usuario> getPerfil(String uid);

    /**
     * Registra un nuevo usuario
     *
     * @param usuario Objeto con la información del nuevo registro
     * @return {@link Task} que representa el estado de la operación
     */
    Task<Void> registrarUsuario(Usuario usuario);

    /**
     * Gestiona la eliminación de una cuenta, registrando previamente el motivo en la auditoría.
     *
     * @param uid ID del usuario a eliminar
     * @param motivo Razón del borrado
     * @param uidAdmin ID del administrador que ejecuta la acción
     * @return {@link Task} con el resultado final del proceso
     */
    Task<Void> eliminarCuenta(String uid, String motivo, String uidAdmin);

    /**
     * Verifica si un nombre de usuario está disponible para su uso
     *
     * @param username Nombre de usuario a comprobar
     * @return {@link Task} con valor true si está disponible, false si ya existe
     */
    Task<Boolean> esUsernameUnico(String username);

    /**
     * Actualiza los datos de un usuario existente
     *
     * @param usuario Objeto con los nuevos datos
     * @return Task que representa el éxito de la actualización
     */
    Task<Void> actualizarPerfil(Usuario usuario);

    /**
     * Realiza una búsqueda de usuarios basada en el nombre de usuario. Ideal para buscadores
     *
     * @param query Texto o prefijo a buscar
     * @return {@link Task} que contiene una lista de {@link Usuario} que coinciden con la búsqueda
     */
    Task<List<Usuario>> buscarUsuarios(String query);
}
