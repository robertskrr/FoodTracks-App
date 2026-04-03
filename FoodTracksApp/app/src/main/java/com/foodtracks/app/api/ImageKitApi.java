package com.foodtracks.app.api;

import com.google.android.gms.tasks.Task;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.Call;

/**
 * Interfaz de Retrofit que define los endpoints de la API de ImageKit.
 * Se centra en la subida y eliminación de archivos.
 *
 * @author Robert
 * @since 03/04
 */
public interface ImageKitApi {
    /**
     * Sube un archivo de imagen al servidor de ImageKit.
     *
     * @param file El archivo binario de la imagen envuelto en un MultipartBody.
     * @param fileName El nombre que se le asignará al archivo en la nube.
     * @param useUnique El flag para que ImageKit añada un sufijo único al nombre.
     * @param folder La carpeta de destino para organizar el almacenamiento (ej: /perfiles).
     * @return {@link Call} que contiene la respuesta con los datos de la subida.
     */
    @Multipart
    @POST("api/v1/files/upload")
    Call<ImageKitResponse> upload(
            @Part MultipartBody.Part file,
            @Part("fileName")RequestBody fileName,
            @Part("useUniqueFilename") RequestBody useUnique,
            @Part("folder") RequestBody folder // Para separar tipo de archivos: /perfiles, /publicaciones
            );

    // Nota: El borrado usa una URL diferente (endpoint de gestión),
    // lo configuraremos cuando lleguemos al borrado de usuario.
}
