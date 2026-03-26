/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.repositories;

import com.foodtracks.app.models.Usuario;

import com.foodtracks.app.repositories.interfaces.IUsuarioRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Repositorio encargado de gestionar la persistencia y consultas de la colección "usuarios" en
 * Firestore. Centraliza el acceso a datos para perfiles de Clientes, Locales y Administradores.
 *
 * @author Robert
 * @since 26/03
 */
public class UsuarioRepository implements IUsuarioRepository {

    private final CollectionReference usersCollection;

    /**
     * Constructor vacío que se encarga de recoger la colección
     */
    public UsuarioRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.usersCollection = db.collection("usuarios");
    }

    @Override
    public Task<Void> saveUsuario(Usuario usuario) {
        return usersCollection.document(usuario.getUid()).set(usuario);
    }

    @Override
    public Task<DocumentSnapshot> getUsuarioById(String uid) {
        return usersCollection.document(uid).get();
    }

    @Override
    public Task<QuerySnapshot> getUsuarioByUsername(String username) {
        return usersCollection.whereEqualTo("username", username).get();
    }

    @Override
    public Task<Void> deleteUsuario(String uid) {
        return usersCollection.document(uid).delete();
    }
}
