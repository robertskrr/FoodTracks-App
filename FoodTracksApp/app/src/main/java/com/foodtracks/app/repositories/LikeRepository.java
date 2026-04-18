/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.repositories;

import com.foodtracks.app.models.LikePublicacion;
import com.foodtracks.app.repositories.interfaces.ILikeRepository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
}
