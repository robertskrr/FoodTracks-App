/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.services.exceptions;

import lombok.Getter;

/**
 * Gestiona las validaciones en cuanto al usuario.
 *
 * @author Robert
 * @since 30/03
 */
@Getter
public class UsuarioValidationException extends Exception {

    private final int resId;

    public UsuarioValidationException(int resId) {
        this.resId = resId;
    }
}
