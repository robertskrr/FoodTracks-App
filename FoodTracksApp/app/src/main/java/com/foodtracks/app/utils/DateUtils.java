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

    /**
     * Convierte la fecha en un formato largo (Ejemplo: 2 de mayo de 2026).
     * @param fecha Fecha a transformar.
     * @return Fecha transformada.
     */
    public static String getFechaFormateadaLong(Timestamp fecha) {
        Date fechaObjeto = fecha.toDate();
        DateFormat formatoFecha = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        return formatoFecha.format(fechaObjeto);
    }

    /**
     * Convierte la fecha en un formato corto (Ejemplo: 2/5/26 16:15).
     * @param fecha Fecha a transformar.
     * @return Fecha transformada.
     */
    public static String getFechaFormateadaShort(Timestamp fecha) {
        Date fechaObjeto = fecha.toDate();
        DateFormat formatoFecha =
                DateFormat.getDateTimeInstance(
                        DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        return formatoFecha.format(fechaObjeto);
    }
}
