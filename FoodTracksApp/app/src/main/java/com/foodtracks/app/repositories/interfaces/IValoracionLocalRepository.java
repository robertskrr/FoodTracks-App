/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.repositories.interfaces;

import com.foodtracks.app.models.ValoracionLocal;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Definición de las operaciones permitidas sobre la colección de valoración de locales.
 *
 * @author Robert
 * @since 26/03
 */
public interface IValoracionLocalRepository {

    /**
     * Guarda una nueva valoración o actualiza una ya existente.
     *
     * @param valoracionLocal Objeto con los datos de la valoración.
     * @return {@link Task} con la referencia al documento creado.
     */
    public Task<Void> saveValoracion(ValoracionLocal valoracionLocal);

    /**
     * Recupera un documento de valoración específica mediante el UID de los implicados.
     *
     * @param uid Identificador de la valoración.
     * @return {@link Task} con el {@link DocumentSnapshot} de la valoración encontrada.
     */
    public Task<DocumentSnapshot> getValoracion(String uid);

    /**
     * Elimina el documento de una valoración de la colección.
     *
     * @param uid Identificador de la valoración a eliminar.
     * @return {@link Task} que representa el estado de la eliminación.
     */
    public Task<Void> deleteValoracion(String uid);
}
