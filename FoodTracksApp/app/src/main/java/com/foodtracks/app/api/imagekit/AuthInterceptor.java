/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.api.imagekit;

import java.io.IOException;

import com.foodtracks.app.BuildConfig;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor de OkHttp encargado de la autenticación con la API de ImageKit.
 * Añade una cabecera de "Authorization" de tipo Basic Auth utilizando la clave privada
 * almacenada de forma segura en las propiedades del proyecto.
 *
 * @author Robert
 * @since 03/04
 */
public class AuthInterceptor implements Interceptor {

    /**
     * Intercepta la petición saliente para inyectar las credenciales.
     * @param chain Cadena de ejecución de la petición.
     * @return {@link Response} Respuesta de la red tras aplicar la cabecera.
     * @throws IOException Si ocurre un error en la transmisión.
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        String credential = Credentials.basic(BuildConfig.IK_PRIVATE, "");

        Request request = chain.request().newBuilder().header("Authorization", credential).build();
        return chain.proceed(request);
    }
}
