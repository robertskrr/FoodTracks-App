/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author Robert
 * @since 22/03
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UsuarioLocal extends Usuario {
    @Builder.Default private String rol = "local";

    private String direccion;
    private String telefono;
    private String sitioWeb;

    @Builder.Default private long visitasPerfil = 0;

    @Builder.Default private double puntuacionMedia = 0.0;

    @Builder.Default private long totalValoraciones = 0;
}
