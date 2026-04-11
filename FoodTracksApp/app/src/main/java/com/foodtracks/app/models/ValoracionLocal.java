/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Robert
 * @since 22/03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValoracionLocal {
    @DocumentId // Asigna el UID del documento automáticamente
    private String uid;

    @PropertyName("uid_cliente")
    @Getter(onMethod_ = {@PropertyName("uid_cliente")})
    @Setter(onMethod_ = {@PropertyName("uid_cliente")})
    private String uidCliente;

    @PropertyName("uid_local")
    @Getter(onMethod_ = {@PropertyName("uid_local")})
    @Setter(onMethod_ = {@PropertyName("uid_local")})
    private String uidLocal;

    private double puntuacion;
}
