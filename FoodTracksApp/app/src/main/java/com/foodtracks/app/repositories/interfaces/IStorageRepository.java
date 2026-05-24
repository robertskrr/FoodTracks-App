/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.repositories.interfaces;

import android.net.Uri;

import com.foodtracks.app.api.imagekit.ImageKitResponse;

import com.google.android.gms.tasks.Task;

/**
 * Repositorio de almacenamiento de imágenes del usuario.
 * @author Robert
 * @since 02/04
 */
public interface IStorageRepository {

    /**
     * Proceso de subida de imagen a la nube.
     * Realiza tres pasos críticos: lectura de bytes, empaquetado Multipart y llamada de red.
     *
     * @param localUri Ruta local del archivo en el dispositivo (ej: de la galería o cámara).
     * @param nombreArchivo Nombre identificador que recibirá el archivo en el servidor.
     * @param folderName Directorio de destino en el CDN (ej: "perfiles" o "publicaciones").
     * @return {@link Task} que entrega un {@link ImageKitResponse} con la URL final y el fileId.
     */
    Task<ImageKitResponse> uploadImage(Uri localUri, String nombreArchivo, String folderName);

    /**
     * Elimina una imagen del servidor de almacenamiento.
     *
     * @param fileId Identificador único del archivo a eliminar (fileId).
     * @return {@link Task} que representa la finalización del proceso de borrado.
     */
    Task<Void> deleteImage(String fileId);
}
