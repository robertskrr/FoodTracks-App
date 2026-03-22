package com.foodtracks.app.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
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

    private String uidAdmin;
    private String uidPublicacion;
    private String uidUsuario;
    private String usernameUsuario;
    private String motivo;

    @ServerTimestamp
    private Timestamp fecha;
}