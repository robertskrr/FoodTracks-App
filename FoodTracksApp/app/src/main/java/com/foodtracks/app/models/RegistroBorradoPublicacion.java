/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Robert
 * @since 22/03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroBorradoPublicacion {
    @DocumentId // Asigna el UID del documento automáticamente
    private String uid;

    @PropertyName("uid_admin")
    private String uidAdmin;

    @PropertyName("uid_publicacion")
    private String uidPublicacion;

    @PropertyName("uid_usuario")
    private String uidUsuario;

    @PropertyName("username_usuario")
    private String usernameUsuario;

    private String motivo;

    @PropertyName("fecha_hora")
    @ServerTimestamp
    private Timestamp fechaHora;
}
