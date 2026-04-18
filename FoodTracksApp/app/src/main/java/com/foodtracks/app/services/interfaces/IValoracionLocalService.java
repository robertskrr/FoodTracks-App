/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.services.interfaces;

import com.foodtracks.app.models.ValoracionLocal;

import com.google.android.gms.tasks.Task;

/**
 * Lógica de negocio para la gestión de valoraciones de locales.
 * Actúa como intermediario entre la UI y el repositorio de datos.
 *
 * @author Robert
 * @since 16/04
 */
public interface IValoracionLocalService {

    /**
     * Registra o actualiza la puntuación de un cliente a un local.
     * Este método debe recalcular la media y el total de votos del local.
     * @param valoracion Objeto con los datos de la valoración.
     * @return {@link Task} que representa el estado de la operación.
     */
    Task<Void> valorarLocal(ValoracionLocal valoracion);

    /**
     * Recupera la valoración que un cliente específico le dio a un local concreto.
     * Útil para mostrar las estrellas ya marcadas cuando el usuario vuelve al perfil.
     * @param uidCliente Identificador del cliente.
     * @param uidLocal Identificador del local.
     * @return {@link Task} que contiene el objeto {@link ValoracionLocal}.
     */
    Task<ValoracionLocal> getValoracionUsuario(String uidCliente, String uidLocal);

    /**
     * Elimina una valoración y actualiza las estadísticas del local.
     * @param uidCliente Identificador del cliente.
     * @param uidLocal Identificador del local.
     * @return {@link Task} que representa el estado de la operación.
     */
    Task<Void> eliminarValoracion(String uidCliente, String uidLocal);
}
