/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.services;

import android.content.Context;

import com.foodtracks.app.repositories.ImageKitRepository;
import com.foodtracks.app.repositories.RegistroBorradoRepository;
import com.foodtracks.app.repositories.UsuarioRepository;
import com.foodtracks.app.repositories.interfaces.IRegistroBorradoRepository;
import com.foodtracks.app.repositories.interfaces.IUsuarioRepository;
import com.foodtracks.app.services.interfaces.IUsuarioService;

/**
 * Fábrica centralizada para la creación e inyección de servicios.
 * Aplica el patrón Factory para desacoplar la lógica de negocio de las
 * implementaciones concretas de los repositorios.
 *
 * @author Robert
 * @since 02/04
 */
public class ServiceFactory {

    /**
     * Proporciona una instancia configurada de UsuarioService.
     * Vincula el servicio con sus repositorios de base de datos y el
     * sistema de almacenamiento en la nube (ImageKit).
     *
     * @param context Contexto de la aplicación necesario para el repositorio de imágenes.
     * @return Implementación de {@link IUsuarioService}.
     */
    public static IUsuarioService provideUsuarioService(Context context) {
        IUsuarioRepository repo = new UsuarioRepository();
        IRegistroBorradoRepository repoLog = new RegistroBorradoRepository();
        ImageKitRepository storageRepo = new ImageKitRepository(context);
        return new UsuarioService(repo, repoLog, storageRepo);
    }
}
