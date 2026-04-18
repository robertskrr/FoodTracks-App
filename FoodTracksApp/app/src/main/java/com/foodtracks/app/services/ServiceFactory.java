/**
 * © FoodTracks Project ===robertskrr===
 */

package com.foodtracks.app.services;

import android.content.Context;

import com.foodtracks.app.repositories.ImageKitRepository;
import com.foodtracks.app.repositories.PublicacionRepository;
import com.foodtracks.app.repositories.RegistroBorradoRepository;
import com.foodtracks.app.repositories.UsuarioRepository;
import com.foodtracks.app.repositories.ValoracionLocalRepository;
import com.foodtracks.app.repositories.interfaces.IPublicacionRepository;
import com.foodtracks.app.repositories.interfaces.IRegistroBorradoRepository;
import com.foodtracks.app.repositories.interfaces.IUsuarioRepository;
import com.foodtracks.app.repositories.interfaces.IValoracionLocalRepository;
import com.foodtracks.app.services.interfaces.IPublicacionService;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.foodtracks.app.services.interfaces.IValoracionLocalService;

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

    /**
     * Proporciona una instancia configurada de PublicacionService.
     * Vincula el servicio con sus repositorios de base de datos y el
     * sistema de almacenamiento en la nube (ImageKit).
     *
     * @param context Contexto de la aplicación necesario para el repositorio de imágenes.
     * @return Implementación de {@link IPublicacionService}.
     */
    public static IPublicacionService providePublicacionService(Context context) {
        IPublicacionRepository repo = new PublicacionRepository();
        IUsuarioRepository repoUser = new UsuarioRepository();
        IRegistroBorradoRepository repoLog = new RegistroBorradoRepository();
        ImageKitRepository storageRepo = new ImageKitRepository(context);
        return new PublicacionService(repo, repoUser, repoLog, storageRepo);
    }

    /**
     * Proporciona una instancia configurada de ValoracionLocalService.
     * Vincula el servicio con sus repositorios de base de datos.
     *
     * @return Implementación de {@link IValoracionLocalService}.
     */
    public static IValoracionLocalService provideValoracionLocalService() {
        IValoracionLocalRepository repo = new ValoracionLocalRepository();
        IUsuarioRepository repoUsers = new UsuarioRepository();
        return new ValoracionLocalService(repo, repoUsers);
    }
}
