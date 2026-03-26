/* © FoodTracks Project ===robertskrr=== */
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
public class UsuarioCliente extends Usuario {
    @Builder.Default private String rol = "cliente";
}
