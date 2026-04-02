package com.foodtracks.app.services;

import com.foodtracks.app.repositories.RegistroBorradoRepository;
import com.foodtracks.app.repositories.StorageRepository;
import com.foodtracks.app.repositories.UsuarioRepository;
import com.foodtracks.app.repositories.interfaces.IRegistroBorradoRepository;
import com.foodtracks.app.repositories.interfaces.IStorageRepository;
import com.foodtracks.app.repositories.interfaces.IUsuarioRepository;
import com.foodtracks.app.services.interfaces.IUsuarioService;

/**
 * Factory que provee a los services sus repositorios correspondientes
 * @author Robert
 * @since 02/04
 */
public class ServiceFactory {

    /**
     * Inicializa los repositorios de UsuarioService
     * @return UsuarioService inicializado
     */
    public static IUsuarioService provideUsuarioService() {
        IUsuarioRepository repo = new UsuarioRepository();
        IRegistroBorradoRepository repoLog = new RegistroBorradoRepository();
        IStorageRepository storage = new StorageRepository();
        return new UsuarioService(repo, repoLog, storage);
    }
}