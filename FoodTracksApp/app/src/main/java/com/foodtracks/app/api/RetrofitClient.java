/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.api;

import com.foodtracks.app.api.imagekit.AuthInterceptor;
import com.foodtracks.app.api.imagekit.ImageKitApi;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente centralizado para la gestión de peticiones de red.
 *
 * @author Robert
 * @since 03/04
 */
public class RetrofitClient {
    private static Retrofit retrofit = null;

    /**
     * Proporciona la instancia única de la interfaz de la API.
     * Configura el cliente OkHttp con el interceptor de seguridad y el conversor GSON.
     *
     * @return La interfaz {@link ImageKitApi} lista para ser utilizada.
     */
    public static ImageKitApi getInterface() {
        if (retrofit == null) {
            OkHttpClient client =
                    new OkHttpClient.Builder().addInterceptor(new AuthInterceptor()).build();

            retrofit =
                    new Retrofit.Builder()
                            .baseUrl("https://upload.imagekit.io/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .build();
        }
        return retrofit.create(ImageKitApi.class);
    }
}
