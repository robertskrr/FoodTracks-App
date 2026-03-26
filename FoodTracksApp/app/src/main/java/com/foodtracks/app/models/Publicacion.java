/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Publicacion {
    @DocumentId private String uid;

    private String texto;
    private String imagen;
    private String uidCliente;
    private String uidLocal;
    @ServerTimestamp private Timestamp fechaHora;

    @Builder.Default private long numLikes = 0;
}
