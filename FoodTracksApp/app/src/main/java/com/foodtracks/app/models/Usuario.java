/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author Robert
 * @since 22/03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Usuario {
    @DocumentId // Asigna el UID del documento automáticamente
    @EqualsAndHashCode.Include
    private String uid;

    @EqualsAndHashCode.Include private String username;

    private String email;
    private String nombre;
    private String ciudad;
    private String rol;

    @ServerTimestamp private Timestamp fechaRegistro;

    private String fotoPerfil;

    /** Preferencias alimenticias */
    private boolean esVegano;

    private boolean esVegetariano;
    private boolean sinLactosa;
    private boolean esCeliaco;
    private Object otraPreferencia; // Puede ser boolean false o String con la preferencia
}
