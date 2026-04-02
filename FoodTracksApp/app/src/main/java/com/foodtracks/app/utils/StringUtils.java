package com.foodtracks.app.utils;

/**
 * Clase que ayudará al formateo y estilo de textos
 * @author Robert
 * @since 02/04
 */
public class StringUtils {

    /**
     * Establece el primer carácter en mayúsculas y el resto en minúsculas
     * @param nombre Nombre a modificar.
     * @return Nombre capitalizado (Nombre y apellidos)
     */
    public static String capitalizeNombreCompleto(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return nombre;
        }

        String[] palabras = nombre.toLowerCase().trim().split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)))
                        .append(palabra.substring(1))
                        .append(" ");
            }
        }

        return resultado.toString().trim();
    }
}
