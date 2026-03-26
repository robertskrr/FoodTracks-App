/* © FoodTracks Project ===robertskrr=== */
package com.foodtracks.app.models;

import com.google.firebase.firestore.DocumentId;
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
public class ValoracionLocal {
    @DocumentId // Asigna el UID del documento automáticamente
    private String uid;

    private String uidCliente;
    private String uidLocal;
    private double puntuacion;
}
