/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
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
public class RegistroBorradoUsuario {
    @DocumentId // Asigna el UID del documento automáticamente
    private String uid;

    @PropertyName("uid_admin")
    @Getter(onMethod_ = {@PropertyName("uid_admin")})
    @Setter(onMethod_ = {@PropertyName("uid_admin")})
    private String uidAdmin;

    @PropertyName("uid_usuario")
    @Getter(onMethod_ = {@PropertyName("uid_usuario")})
    @Setter(onMethod_ = {@PropertyName("uid_usuario")})
    private String uidUsuario;

    @PropertyName("username_usuario")
    @Getter(onMethod_ = {@PropertyName("username_usuario")})
    @Setter(onMethod_ = {@PropertyName("username_usuario")})
    private String usernameUsuario;

    private String motivo;

    @PropertyName("fecha_hora")
    @ServerTimestamp
    @Getter(onMethod_ = {@PropertyName("fecha_hora")})
    @Setter(onMethod_ = {@PropertyName("fecha_hora")})
    private Timestamp fechaHora;
}
