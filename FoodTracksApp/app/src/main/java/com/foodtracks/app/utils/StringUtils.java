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

    /**
     * Capitaliza solo la primera letra de un string y pone el resto en minúsculas.
     * * @param texto Palabra a capitalizar.
     * @return Texto formateado (Ej: "sevilla" -> "Sevilla")
     */
    public static String capitalize(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }

        String minusculas = texto.toLowerCase().trim();

        return Character.toUpperCase(minusculas.charAt(0)) + minusculas.substring(1);
    }
}
