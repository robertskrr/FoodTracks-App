/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
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

    @EqualsAndHashCode.Include
    private String username;

    private String email;
    private String nombre;
    private String ciudad;
    private String rol;

    @PropertyName("fecha_registro")
    @ServerTimestamp
    private Timestamp fechaRegistro;

    @PropertyName("foto_perfil")
    private String fotoPerfil;

    /** Preferencias alimenticias */

    @PropertyName("es_vegano")
    private boolean esVegano;

    @PropertyName("es_vegetariano")
    private boolean esVegetariano;

    @PropertyName("sin_lactosa")
    private boolean sinLactosa;

    @PropertyName("es_celiaco")
    private boolean esCeliaco;

    @PropertyName("otra_preferencia")
    private Object otraPreferencia; // Puede ser boolean false o String con la preferencia
}
