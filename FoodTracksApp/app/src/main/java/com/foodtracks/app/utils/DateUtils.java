/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.firebase.Timestamp;

/**
 * Clase de utilidad para la manipulación de fechas.
 * Proporciona métodos auxiliares para procesar información necesaria
 * en las operaciones de gestión de fechas.
 *
 * @author Robert
 * @since 03/05
 */
public class DateUtils {
    public static String getFechaFormateadaLong(Timestamp fecha) {
        Date fechaObjeto = fecha.toDate();
        DateFormat formatoFecha = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        return formatoFecha.format(fechaObjeto);
    }

    public static String getFechaFormateadaShort(Timestamp fecha) {
        Date fechaObjeto = fecha.toDate();
        DateFormat formatoFecha = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        return formatoFecha.format(fechaObjeto);
    }
}
