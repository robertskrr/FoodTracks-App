/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.repositories;

import com.foodtracks.app.models.Usuario;
import com.foodtracks.app.repositories.interfaces.IUsuarioRepository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Repositorio encargado de gestionar la persistencia y consultas de la colección "usuarios" en Firestore.
 * Centraliza el acceso a datos para perfiles de Clientes, Locales y Administradores.
 *
 * @author Robert
 * @since 26/03
 */
public class UsuarioRepository implements IUsuarioRepository {

    private final CollectionReference usersCollection;

    /** Constructor vacío que se encarga de recoger la colección */
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

    @Override
    public Task<QuerySnapshot> searchUsuariosByField(String field, String query) {
        return usersCollection
                .orderBy(field)
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(30)
                .get();
    }

    @Override
    public Task<QuerySnapshot> searchLocalesByFiltros(
            String ciudad,
            boolean vegano,
            boolean vegetariano,
            boolean sinLactosa,
            boolean celiaco,
            String otraPreferencia) {
        // Solo queremos locales
        Query query = usersCollection.whereEqualTo("rol", "local");

        // Añadimos los filtros
        if (ciudad != null && !ciudad.isEmpty()) {
            query = query.whereEqualTo("ciudad", ciudad);
        }
        if (vegano) {
            query = query.whereEqualTo("es_vegano", true);
        }
        if (vegetariano) {
            query = query.whereEqualTo("es_vegetariano", true);
        }
        if (sinLactosa) {
            query = query.whereEqualTo("sin_lactosa", true);
        }
        if (celiaco) {
            query = query.whereEqualTo("es_celiaco", true);
        }
        if (otraPreferencia != null && !otraPreferencia.trim().isEmpty()) {
            query = query.whereEqualTo("otra_preferencia", otraPreferencia);
        }

        return query.limit(50).get();
    }

    @Override
    public Task<QuerySnapshot> searchLocalesByUsername(String username) {
        return usersCollection
                .whereEqualTo("rol", "local")
                .orderBy("username")
                .startAt(username)
                .endAt(username + "\uf8ff")
                .limit(10)
                .get();
    }

    @Override
    public Task<Void> incrementarVisitasPerfil(String uidLocal) {
        return usersCollection.document(uidLocal).update("visitas_perfil", FieldValue.increment(1));
    }

    @Override
    public Task<QuerySnapshot> getUltimosUsuariosRegistrados(int limite) {
        return usersCollection
                .orderBy("fecha_registro", Query.Direction.DESCENDING)
                .limit(limite)
                .get();
    }
}
