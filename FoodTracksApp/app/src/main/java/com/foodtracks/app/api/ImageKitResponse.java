package com.foodtracks.app.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Modelo de datos que representa la respuesta JSON de ImageKit tras una subida.
 *
 * @author Robert
 * @since 03/04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageKitResponse {
    /** Identificador único del archivo en ImageKit. */
    private String fileId;

    /** Nombre asignado al archivo subido. */
    private String name;

    /** URL absoluta de acceso público a la imagen. */
    private String url;

    /** URL de la miniatura generada automáticamente. */
    private String thumbnailUrl;

    /** Altura en píxeles. */
    private int height;

    /** Anchura en píxeles. */
    private int width;

    /** Tamaño del archivo en bytes. */
    private long size;
}

