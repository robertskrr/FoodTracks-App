/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.repositories;

import com.foodtracks.app.models.Publicacion;
import com.foodtracks.app.repositories.interfaces.IPublicacionRepository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Repositorio encargado de gestionar la persistencia y consultas de la colección "publicaciones" en Firestore.
 * Centraliza el acceso a datos para las publicaciones de los usuarios.
 *
 * @author Robert
 * @since 26/03
 */
public class PublicacionRepository implements IPublicacionRepository {

    private final CollectionReference postsCollection;

    /**
     * Constructor vacío que se encarga de recoger la colección
     */
    public PublicacionRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.postsCollection = db.collection("publicaciones");
    }

    @Override
    public Task<Void> savePublicacion(Publicacion publicacion) {
        // Devuelve null para evitar problemas con add,ya que no devuelve un void originalmente
        return postsCollection.add(publicacion).continueWith(task -> null);
    }

    @Override
    public Task<DocumentSnapshot> getPublicacionById(String uid) {
        return postsCollection.document(uid).get();
    }

    @Override
    public Task<Void> deletePublicacion(String uid) {
        return postsCollection.document(uid).delete();
    }

    @Override
    public Task<QuerySnapshot> getPublicacionesByUsuario(String uidUsuario) {
        return postsCollection.whereEqualTo("uid_usuario", uidUsuario).get();
    }

    @Override
    public Task<QuerySnapshot> getPublicacionesByLocal(String uidLocal) {
        return postsCollection.whereEqualTo("uid_local", uidLocal).get();
    }

    @Override
    public Task<QuerySnapshot> getAllPublicaciones(DocumentSnapshot lastVisible) {
        Query query =
                postsCollection
                        .orderBy("fecha_hora", Query.Direction.DESCENDING) // Ordenamos por fecha
                        .limit(50); // Muestra de 50 en 50

        if (lastVisible != null) {
            // Si ha ido viendo publicaciones, guarda un "checkpoint" de la última vista y carga a
            // partir de esa
            query = query.startAfter(lastVisible);
        }

        return query.get();
    }

    @Override
    public Task<Void> actualizarContadorLikes(String uidPublicacion, int cantidad){
        return postsCollection.document(uidPublicacion)
                .update("num_likes", FieldValue.increment(cantidad));
    }
}
