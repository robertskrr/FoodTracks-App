/* © FoodTracks Project ===robertskrr=== */
package com.foodtracks.app.repositories;

import com.foodtracks.app.models.Usuario;

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
public class UsuarioRepository {

    private final CollectionReference usersCollection;

    public UsuarioRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.usersCollection = db.collection("usuarios");
    }

    /**
     * Guarda un nuevo usuario o actualiza uno existente
     *
     * @param usuario que extiende de {@link Usuario} (Cliente, Local o Admin)
     * @return {@link Task} que representa el estado de la operación de escritura
     */
    public Task<Void> saveUsuario(Usuario usuario) {
        return usersCollection.document(usuario.getUid()).set(usuario);
    }

    /**
     * Recupera un documento de usuario específico mediante UID
     *
     * @param uid Identificador del usuario
     * @return {@link Task} con el {@link DocumentSnapshot} del usuario encontrado
     */
    public Task<DocumentSnapshot> getUsuarioById(String uid) {
        return usersCollection.document(uid).get();
    }

    /**
     * Realiza una consulta para encontrar un usuario basado en su username único
     *
     * @param username Nombre de usuario a buscar
     * @return {@link Task} con el {@link QuerySnapshot} que contiene los resultados (lista) de la
     *     búsqueda
     */
    public Task<QuerySnapshot> getUsuarioByUsername(String username) {
        return usersCollection.whereEqualTo("username", username).get();
    }

    /**
     * Elimina físicamente el documento de un usuario de la colección
     *
     * @param uid Identificador del usuario a eliminar
     * @return {@link Task} que representa el estado de la eliminación
     */
    public Task<Void> deleteUsuario(String uid) {
        return usersCollection.document(uid).delete();
    }
}
