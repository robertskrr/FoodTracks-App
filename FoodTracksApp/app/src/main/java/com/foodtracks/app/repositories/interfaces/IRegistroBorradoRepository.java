package com.foodtracks.app.repositories.interfaces;

import com.foodtracks.app.models.RegistroBorradoPublicacion;
import com.foodtracks.app.models.RegistroBorradoUsuario;
import com.foodtracks.app.models.Usuario;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Definición de las operaciones permitidas sobre las colecciones de registros de borrado
 * @author Robert
 * @since 26/03
 */
public interface IRegistroBorradoRepository {

    /**
     * Guarda un rastro permanente de un usuario que va a ser eliminado
     * @param registro Objeto con los datos del usuario, administrador y motivo del borrado
     * @return {@link Task} con la referencia al documento creado
     */
    public Task<DocumentReference> saveRegistroBorradoUsuario(RegistroBorradoUsuario registro);

    /**
     * Guarda un rastro permanente de una publicación que va a ser eliminada
     * @param registro Objeto con los datos de la publicación, autor, administrador y motivo del borrado
     * @return {@link Task} con la referencia al documento creado
     */
    public Task<DocumentReference> saveRegistroBorradoPublicacion(RegistroBorradoPublicacion registro);
}
