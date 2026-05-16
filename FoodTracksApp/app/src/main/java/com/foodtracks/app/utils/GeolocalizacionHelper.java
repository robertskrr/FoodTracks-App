/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

/**
 * Clase de ayuda para la geolocalización de los locales
 *
 * @author Robert
 * @since 02/05
 */
public class GeolocalizacionHelper {
    /**
     * Convierte una dirección de texto en coordenadas (Latitud y Longitud).
     *
     * @param context   Contexto de la aplicación.
     * @param direccion Texto de la dirección (ej: "Calle Sierpes, Sevilla").
     * @return Un array de double donde [0] es Latitud y [1] es Longitud. Devuelve null si falla.
     */
    public static double[] obtenerCoordenadas(Context context, String direccion, String ciudad) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {
            List<Address> direcciones = geocoder.getFromLocationName(direccion.trim() + ", " + ciudad.trim(), 1);

            if (direcciones != null && !direcciones.isEmpty()) {
                Address direccionEncontrada = direcciones.get(0);

                double latitud = direccionEncontrada.getLatitude();
                double longitud = direccionEncontrada.getLongitude();

                return new double[] {latitud, longitud};
            }
        } catch (IOException e) {
            Log.e("GEOCODER", e.getMessage(), e);
        }

        return null; // No se encontró la dirección
    }
}
