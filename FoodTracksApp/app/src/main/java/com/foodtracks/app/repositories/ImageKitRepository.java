package com.foodtracks.app.repositories;

import android.content.Context;
import android.net.Uri;

import com.foodtracks.app.utils.FileUtils;

import com.foodtracks.app.api.ImageKitResponse;
import com.foodtracks.app.api.RetrofitClient;
import com.foodtracks.app.repositories.interfaces.IStorageRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementación del repositorio de almacenamiento utilizando el servicio de ImageKit.io.
 * Esta clase se encarga de la lógica técnica para subir imágenes: convierte recursos locales (Uri)
 * en flujos de datos binarios, gestiona la petición Multipart hacia la API de ImageKit
 * y envuelve la respuesta en objetos Task de Google.
 *
 * @author Robert
 * @since 03/04
 */
public class ImageKitRepository implements IStorageRepository {
    private final Context context;

    public ImageKitRepository(Context context) {
        this.context = context;
    }


    @Override
    public Task<ImageKitResponse> uploadImage(Uri localUri, String nombreArchivo, String folderName) {
        // Objeto que permite convertir una Callback de Retrofit en una Task de Google
        TaskCompletionSource<ImageKitResponse> tcs = new TaskCompletionSource<>();

        try {
            // Conversión de Uri a datos binarios (bytes)
            InputStream iStream = context.getContentResolver().openInputStream(localUri);
            byte[] inputData = FileUtils.getBytes(iStream);

            // Construcción del cuerpo de la petición Multipart
            RequestBody requestFile = RequestBody.create(inputData, MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", nombreArchivo + ".jpg", requestFile);

            // Parámetros adicionales de la API de ImageKit
            RequestBody fileName = RequestBody.create(nombreArchivo, MediaType.parse("text/plain"));
            RequestBody useUnique = RequestBody.create("true", MediaType.parse("text/plain"));
            RequestBody folder = RequestBody.create(folderName, MediaType.parse("text/plain"));

            // Ejecución de la petición mediante Retrofit
            Call<ImageKitResponse> call = RetrofitClient.getInterface().upload(body, fileName, useUnique, folder);
            call.enqueue(new Callback<ImageKitResponse>() {
                @Override
                public void onResponse(Call<ImageKitResponse> call, Response<ImageKitResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Transmitimos el éxito al Task
                        tcs.setResult(response.body());
                    } else {
                        tcs.setException(new Exception("Error de respuesta API: " + response.code()));
                    }
                }

                @Override
                public void onFailure(Call<ImageKitResponse> call, Throwable t) {
                    // Transmitimos el fallo de red
                    tcs.setException(new Exception(t.getMessage()));
                }
            });

        } catch (Exception e) {
            tcs.setException(e);
        }

        return tcs.getTask();
    }


    @Override
    public Task<Void> deleteImage(String path) {

        return Tasks.forResult(null);
    }
}
