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
 * @since 18/04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikePublicacion {
    @DocumentId
    private String uid; // ID compuesto: uidUsuario_uidPublicacion

    @PropertyName("uid_usuario")
    private String uidUsuario;

    @PropertyName("uid_publicacion")
    private String uidPublicacion;

    @ServerTimestamp
    @PropertyName("fecha_hora")
    private Timestamp fechaHora;
}