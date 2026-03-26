/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.repositories;

import com.foodtracks.app.models.RegistroBorradoPublicacion;
import com.foodtracks.app.models.RegistroBorradoUsuario;
import com.foodtracks.app.repositories.interfaces.IRegistroBorradoRepository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Repositorio encargado de gestionar los logs de auditoría cuando se eliminan datos del sistema.
 * Registra quién realizó la acción y el motivo, garantizando la trazabilidad.
 *
 * @author Robert
 * @since 26/03
 */
public class RegistroBorradoRepository implements IRegistroBorradoRepository {

    private final CollectionReference deletedLogUsuariosCollection;
    private final CollectionReference deletedLogPublicacionesCollection;

    /** Constructor vacío que se encarga de recoger las colecciones */
    public RegistroBorradoRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.deletedLogUsuariosCollection = db.collection("registros_borrado_usuarios");
        this.deletedLogPublicacionesCollection = db.collection("registros_borrado_publicaciones");
    }

    @Override
    public Task<DocumentReference> saveRegistroBorradoUsuario(RegistroBorradoUsuario registro) {
        return deletedLogUsuariosCollection.add(registro);
    }

    @Override
    public Task<DocumentReference> saveRegistroBorradoPublicacion(
            RegistroBorradoPublicacion registro) {
        return deletedLogPublicacionesCollection.add(registro);
    }
}
