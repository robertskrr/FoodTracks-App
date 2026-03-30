/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.services.exceptions;

import lombok.Getter;

/**
 * Gestiona los errores en cuanto a usuarios no encontrados en la BD.
 *
 * @author Robert
 * @since 30/03
 */
@Getter
public class UsuarioNotFoundException extends Exception {
    private final int resId;

    public UsuarioNotFoundException(int resId) {
        this.resId = resId;
    }
}
