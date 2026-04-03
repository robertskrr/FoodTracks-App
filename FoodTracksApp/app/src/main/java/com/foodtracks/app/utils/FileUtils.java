package com.foodtracks.app.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Clase de utilidad para la manipulación de flujos de datos y archivos.
 * Proporciona métodos auxiliares para procesar información binaria necesaria
 * en las operaciones de red y almacenamiento de la aplicación.
 *
 * @author Robert
 * @since 03/04
 */
public class FileUtils {
    /**
     * Lee un InputStream y lo convierte en un array de bytes.
     * @param inputStream Flujo de datos del archivo.
     * @return Array de bytes con el contenido.
     * @throws IOException Si hay error de lectura.
     */
    public static byte[] getBytes(InputStream inputStream) throws IOException {
        // Almacén temporal de bytes que crece dinámicamente
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // Definimos un búfer de 1KB para ir leyendo por trozos
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        // Leemos trozos del flujo hasta llegar al final (-1)
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // Convertimos el almacén temporal en el array final de bytes
        return byteBuffer.toByteArray();
    }
}
