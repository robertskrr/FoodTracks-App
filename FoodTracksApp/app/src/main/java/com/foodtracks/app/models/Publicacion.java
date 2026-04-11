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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Publicacion {
    @DocumentId private String uid;

    private String texto;
    private String imagen;

    @PropertyName("imagen_id")
    @Getter(onMethod_ = {@PropertyName("imagen_id")})
    @Setter(onMethod_ = {@PropertyName("imagen_id")})
    private String imagen_id; // ID para imageKit

    @PropertyName("uid_usuario")
    @Getter(onMethod_ = {@PropertyName("uid_usuario")})
    @Setter(onMethod_ = {@PropertyName("uid_usuario")})
    private String uidUsuario;

    @PropertyName("uid_local")
    @Getter(onMethod_ = {@PropertyName("uid_local")})
    @Setter(onMethod_ = {@PropertyName("uid_local")})
    private String uidLocal;

    @PropertyName("fecha_hora")
    @ServerTimestamp
    @Getter(onMethod_ = {@PropertyName("fecha_hora")})
    @Setter(onMethod_ = {@PropertyName("fecha_hora")})
    private Timestamp fechaHora;

    @PropertyName("num_likes")
    @Getter(onMethod_ = {@PropertyName("num_likes")})
    @Setter(onMethod_ = {@PropertyName("num_likes")})
    @Builder.Default
    private long numLikes = 0;
}
