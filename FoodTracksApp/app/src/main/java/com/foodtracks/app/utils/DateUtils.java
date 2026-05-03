package com.foodtracks.app.utils;

import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Clase de utilidad para la manipulación de fechas.
 * Proporciona métodos auxiliares para procesar información necesaria
 * en las operaciones de gestión de fechas.
 *
 * @author Robert
 * @since 03/05
 */
public class DateUtils {
    public static String getFechaFormateada(Timestamp fecha){
        Date fechaObjeto = fecha.toDate();
        DateFormat formatoFecha = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        return formatoFecha.format(fechaObjeto);
    }
}
