/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.models;

import com.google.firebase.firestore.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @PropertyName("sitio_web")
    @Getter(onMethod_ = {@PropertyName("sitio_web")})
    @Setter(onMethod_ = {@PropertyName("sitio_web")})
    private String sitioWeb; // puede ser empty

    @PropertyName("visitas_perfil")
    @Getter(onMethod_ = {@PropertyName("visitas_perfil")})
    @Setter(onMethod_ = {@PropertyName("visitas_perfil")})
    @Builder.Default
    private long visitasPerfil = 0;

    @PropertyName("puntuacion_media")
    @Getter(onMethod_ = {@PropertyName("puntuacion_media")})
    @Setter(onMethod_ = {@PropertyName("puntuacion_media")})
    @Builder.Default
    private double puntuacionMedia = 0.0;

    @PropertyName("total_valoraciones")
    @Getter(onMethod_ = {@PropertyName("total_valoraciones")})
    @Setter(onMethod_ = {@PropertyName("total_valoraciones")})
    @Builder.Default
    private long totalValoraciones = 0;
}
