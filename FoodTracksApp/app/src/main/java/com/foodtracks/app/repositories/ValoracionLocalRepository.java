/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.repositories;

import com.foodtracks.app.models.ValoracionLocal;
import com.foodtracks.app.repositories.interfaces.IValoracionLocalRepository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Repositorio encargado de gestionar la persistencia y consultas de la colección "valoraciones_locales" en Firestore.
 * Centraliza el acceso a datos para las valoraciones de los locales por parte de clientes.
 *
 * @author Robert
 * @since 26/03
 */
public class ValoracionLocalRepository implements IValoracionLocalRepository {

    private final CollectionReference ratingsCollection;

    /** Constructor vacío que se encarga de recoger la colección */
    public ValoracionLocalRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.ratingsCollection = db.collection("valoraciones_locales");
    }

    @Override
    public Task<Void> saveValoracion(ValoracionLocal valoracionLocal) {
        return ratingsCollection.document(valoracionLocal.getUid()).set(valoracionLocal);
    }

    @Override
    public Task<DocumentSnapshot> getValoracion(String uid) {
        return ratingsCollection.document(uid).get();
    }

    @Override
    public Task<Void> deleteValoracion(String uid) {
        return ratingsCollection.document(uid).delete();
    }

    // TODO --> Borrar todas las valoraciones de un usuario si se elimina su cuenta
}
