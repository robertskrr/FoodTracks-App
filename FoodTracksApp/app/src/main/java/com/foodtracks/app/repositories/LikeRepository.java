/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.repositories;

import com.foodtracks.app.models.LikePublicacion;
import com.foodtracks.app.repositories.interfaces.ILikeRepository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

/**
 * Repositorio encargado de gestionar la persistencia y consultas de la colección "likes_publicaciones" en Firestore.
 * Centraliza el acceso a datos para los likes a las publicaciones de los usuarios.
 *
 * @author Robert
 * @since 18/04
 */
public class LikeRepository implements ILikeRepository {

    private final CollectionReference likesCollection;

    /**
     * Constructor vacío que se encarga de recoger la colección
     */
    public LikeRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.likesCollection = db.collection("likes_publicaciones");
    }

    @Override
    public Task<Void> saveLike(LikePublicacion like) {
        return likesCollection.document(like.getUid()).set(like);
    }

    @Override
    public Task<Void> deleteLike(String uidLike) {
        return likesCollection.document(uidLike).delete();
    }

    @Override
    public Task<DocumentSnapshot> getLike(String uidLike) {
        return likesCollection.document(uidLike).get();
    }

    @Override
    public Task<Void> deleteAllLikesByPublicacion(String uidPublicacion) {
        // Buscamos todos los likes asociados
        return likesCollection
                .whereEqualTo("uid_publicacion", uidPublicacion)
                .get()
                .continueWithTask(
                        task -> {
                            if (!task.isSuccessful() || task.getResult() == null) {
                                return Tasks.forException(
                                        task.getException() != null
                                                ? task.getException()
                                                : new Exception("Error al obtener likes"));
                            }

                            // Creamos un Batch para ejecutar múltiples borrados a la vez
                            WriteBatch batch = FirebaseFirestore.getInstance().batch();

                            for (DocumentSnapshot doc : task.getResult()) {
                                batch.delete(doc.getReference());
                            }

                            // Ejecutamos el borrado completo
                            return batch.commit();
                        });
    }
}
