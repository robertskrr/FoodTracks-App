/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.services;

import java.util.List;

import com.foodtracks.app.R;
import com.foodtracks.app.models.RegistroBorradoUsuario;
import com.foodtracks.app.models.Usuario;
import com.foodtracks.app.repositories.interfaces.IRegistroBorradoRepository;
import com.foodtracks.app.repositories.interfaces.IUsuarioRepository;
import com.foodtracks.app.services.exceptions.UsuarioNotFoundException;
import com.foodtracks.app.services.exceptions.UsuarioValidationException;
import com.foodtracks.app.services.interfaces.IUsuarioService;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Lógica de negocio para la gestión de usuarios.
 *
 * @author Robert
 * @since 30/03
 */
public class UsuarioService implements IUsuarioService {
    private final IUsuarioRepository usuarioRepository;
    private final IRegistroBorradoRepository registroBorradoRepository;

    public UsuarioService(
            IUsuarioRepository usuarioRepository,
            IRegistroBorradoRepository registroBorradoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.registroBorradoRepository = registroBorradoRepository;
    }

    @Override
    public Task<Usuario> getPerfil(String uid) {
        return usuarioRepository
                .getUsuarioById(uid)
                .continueWithTask(
                        task -> {
                            DocumentSnapshot doc = task.getResult();
                            // Si el usuario existe lo devuelve, en caso contrario devuelve la
                            // excepción NOT FOUND
                            if (task.isSuccessful() && doc != null && doc.exists()) {
                                return Tasks.forResult(doc.toObject(Usuario.class));
                            } else {
                                return Tasks.forException(
                                        new UsuarioNotFoundException(
                                                R.string.usuario_not_found_error_message));
                            }
                        });
    }

    @Override
    public Task<Void> registrarUsuario(Usuario usuario) {
        if (!emailValido(usuario.getEmail())) {
            return Tasks.forException(
                    new UsuarioValidationException(R.string.email_validation_error_message));
        }
        // Normalizamos username y email a minúsculas
        normalizarDatos(usuario);

        return esUsernameUnico(usuario.getUsername())
                .continueWithTask(
                        task -> {
                            if (task.getResult() != null && !task.getResult()) {
                                return Tasks.forException(
                                        new UsuarioValidationException(
                                                R.string.username_validation_error_message));
                            }
                            return usuarioRepository.saveUsuario(usuario);
                        });
    }

    @Override
    public Task<Void> eliminarCuenta(String uidUsuario, String motivo, String uidAdmin) {
        return usuarioRepository
                .getUsuarioById(uidUsuario)
                .continueWithTask(
                        task -> {
                            DocumentSnapshot doc = task.getResult();

                            if (!doc.exists()) {
                                return Tasks.forException(
                                        new UsuarioNotFoundException(
                                                R.string.usuario_not_found_error_message));
                            }

                            Usuario usuarioABorrar = doc.toObject(Usuario.class);

                            // Creamos el registro de borrado
                            RegistroBorradoUsuario registro =
                                    RegistroBorradoUsuario.builder()
                                            .uidUsuario(uidUsuario)
                                            .uidAdmin(uidAdmin)
                                            .usernameUsuario(usuarioABorrar.getUsername())
                                            .motivo(motivo)
                                            .fechaHora(com.google.firebase.Timestamp.now())
                                            .build();

                            // Guardamos el log y, solo si tiene éxito, procedemos al borrado real
                            return registroBorradoRepository
                                    .saveRegistroBorradoUsuario(registro)
                                    .continueWithTask(
                                            deleteTask ->
                                                    usuarioRepository.deleteUsuario(uidUsuario));
                        });
    }

    @Override
    public Task<Boolean> esUsernameUnico(String username) {
        String cleanUsername = (username != null) ? username.toLowerCase().trim() : "";
        return usuarioRepository
                .getUsuarioByUsername(cleanUsername)
                .continueWith(task -> task.getResult().isEmpty());
    }

    @Override
    public Task<Void> actualizarPerfil(Usuario usuarioNuevo) {
        if (!emailValido(usuarioNuevo.getEmail())) {
            return Tasks.forException(
                    new UsuarioValidationException(R.string.email_validation_error_message));
        }

        normalizarDatos(usuarioNuevo);

        // Comparamos con el perfil anterior
        return usuarioRepository
                .getUsuarioById(usuarioNuevo.getUid())
                .continueWithTask(
                        task -> {
                            Usuario usuarioActual = task.getResult().toObject(Usuario.class);

                            // Comprueba si se ha cambiado el username
                            if (usuarioActual != null
                                    && !usuarioActual
                                            .getUsername()
                                            .equals(usuarioNuevo.getUsername())) {
                                // Si el nombre es distinto, comprobamos que el nuevo no esté
                                // pillado
                                return esUsernameUnico(usuarioNuevo.getUsername())
                                        .continueWithTask(
                                                isUniqueTask -> {
                                                    if (isUniqueTask.getResult() != null
                                                            && !isUniqueTask.getResult()) {
                                                        return Tasks.forException(
                                                                new UsuarioValidationException(
                                                                        R.string
                                                                                .username_validation_error_message));
                                                    }
                                                    // Si es único guardamos los nuevos datos
                                                    return usuarioRepository.saveUsuario(
                                                            usuarioNuevo);
                                                });
                            }

                            // Si el nombre es el mismo que ya tenía se guarda directamente
                            return usuarioRepository.saveUsuario(usuarioNuevo);
                        });
    }

    @Override
    public Task<List<Usuario>> buscarUsuarios(String query) {
        // Siempre en minúsculas
        String cleanQuery = (query != null) ? query.toLowerCase().trim() : "";

        return usuarioRepository
                .searchUsuariosByUsername(cleanQuery)
                .continueWith(
                        task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                return task.getResult().toObjects(Usuario.class);
                            } else {
                                return new java.util.ArrayList<>();
                            }
                        });
    }

    /**
     * ================================================================
     * ==================== Private helpers ===========================
     * ================================================================
     */
    private boolean emailValido(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void normalizarDatos(Usuario usuario) {
        if (usuario.getUsername() != null) {
            usuario.setUsername(usuario.getUsername().toLowerCase().trim());
        }

        if (usuario.getEmail() != null) {
            usuario.setEmail(usuario.getEmail().toLowerCase().trim());
        }
    }
}
