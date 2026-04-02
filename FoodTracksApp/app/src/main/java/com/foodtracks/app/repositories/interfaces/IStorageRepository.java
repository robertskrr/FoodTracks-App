package com.foodtracks.app.repositories.interfaces;

import android.net.Uri;

import com.google.android.gms.tasks.Task;

/**
 * Repositorio de almacenamiento de archivos del usuario
 * @author Robert
 * @since 02/04
 */
public interface IStorageRepository {

    /**
     * Sube una imagen a Firebase Storage.
     * @param uri Ruta local del archivo en el dispositivo.
     * @param path Ruta de destino en Storage (ej: "perfiles/uid.jpg").
     * @return {@link Task} con la URL de la imagen.
     */
    Task<Uri> uploadImage(Uri uri, String path);

    /**
     * Borra la imagen de Storage
     * @param path Ruta de la imagen
     * @return {@link Task} con el resultado final del proceso.
     */
    Task<Void> deleteImage(String path);
}
