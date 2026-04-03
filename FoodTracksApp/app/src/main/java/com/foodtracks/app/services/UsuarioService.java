/**
 * © FoodTracks Project ===robertskrr===
 */

package com.foodtracks.app.services;

import android.net.Uri;

import java.util.List;

import com.foodtracks.app.R;
import com.foodtracks.app.api.imagekit.ImageKitResponse;
import com.foodtracks.app.models.RegistroBorradoUsuario;
import com.foodtracks.app.models.Usuario;
import com.foodtracks.app.models.UsuarioLocal;
import com.foodtracks.app.repositories.interfaces.IRegistroBorradoRepository;
import com.foodtracks.app.repositories.interfaces.IStorageRepository;
import com.foodtracks.app.repositories.interfaces.IUsuarioRepository;
import com.foodtracks.app.services.exceptions.UsuarioNotFoundException;
import com.foodtracks.app.services.exceptions.UsuarioValidationException;
import com.foodtracks.app.services.interfaces.IUsuarioService;

import com.foodtracks.app.utils.StringUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
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
    private final IStorageRepository storageRepository;

    public UsuarioService(
            IUsuarioRepository usuarioRepository,
            IRegistroBorradoRepository registroBorradoRepository,
            IStorageRepository storageRepository) {
        this.usuarioRepository = usuarioRepository;
        this.registroBorradoRepository = registroBorradoRepository;
        this.storageRepository = storageRepository;
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
    public Task<Void> registrarUsuario(Usuario usuario, Uri fotoUri) {
        int errorIdValidacion = validarDatos(usuario);
        if (errorIdValidacion != 0) {
            return Tasks.forException(
                    new UsuarioValidationException(errorIdValidacion));
        }

        usuario.setFechaRegistro(Timestamp.now());
        normalizarDatos(usuario);

        return esUsernameUnico(usuario.getUsername()).continueWithTask(task -> {
            if (task.getResult() != null && !task.getResult()) {
                return Tasks.forException(new UsuarioValidationException(R.string.username_validation_error_message));
            }

            // Subimos la foto de perfil a ImageKit
            if (fotoUri != null) {
                return storageRepository.uploadImage(fotoUri, usuario.getUid(), "perfiles")
                        .continueWithTask(uploadTask -> {
                            ImageKitResponse res = uploadTask.getResult();

                            // Asociamos la URL y el ID de la foto al usuario
                            usuario.setFotoPerfil(res.getUrl());
                            usuario.setFotoId(res.getFileId());

                            return usuarioRepository.saveUsuario(usuario);
                        });
            } else {
                return usuarioRepository.saveUsuario(usuario);
            }
        });
    }

    @Override
    public int validarCredenciales(String email, String pass, String confirmPass) {
        if (email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            return R.string.usuario_empty_fields_error_message;
        }

        if (!emailValido(email)) {
            return R.string.email_validation_error_message;
        }

        if (pass.length() < 8) {
            return R.string.password_length_error_message;
        }

        if (!pass.equals(confirmPass)) {
            return R.string.passwords_dont_match_error_message;
        }

        return 0;
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

                            // Borra la foto de ImageKit
                            if (usuarioABorrar.getFotoId() != null) {
                                storageRepository.deleteImage(usuarioABorrar.getFotoId());
                            }

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
    public Task<Void> actualizarPerfil(Usuario usuarioModificado, Uri fotoUri) {
        int errorIdValidacion = validarDatos(usuarioModificado);
        if (errorIdValidacion != 0) {
            return Tasks.forException(
                    new UsuarioValidationException(errorIdValidacion));
        }

        normalizarDatos(usuarioModificado);

        // Comparamos con el perfil anterior
        return usuarioRepository
                .getUsuarioById(usuarioModificado.getUid())
                .continueWithTask(
                        task -> {
                            Usuario usuarioActual = task.getResult().toObject(Usuario.class);

                            if (usuarioActual == null) {
                                return Tasks.forException(new UsuarioNotFoundException(R.string.usuario_not_found_error_message));
                            }

                            Task<ImageKitResponse> uploadTask;
                            if (fotoUri != null) {
                                uploadTask = storageRepository.uploadImage(fotoUri, usuarioModificado.getUid(), "perfiles");
                            } else {
                                uploadTask = Tasks.forResult(null);
                            }

                            return uploadTask.continueWithTask(resTask -> {
                                if (resTask.isSuccessful() && resTask.getResult() != null) {
                                    ImageKitResponse res = resTask.getResult();

                                    // Borramos la foto antigua de la nube si el usuario ya tenía una
                                    if (usuarioActual.getFotoId() != null) {
                                        storageRepository.deleteImage(usuarioActual.getFotoId());
                                    }

                                    // Actualizamos los campos de imagen en el usuario modificado
                                    usuarioModificado.setFotoPerfil(res.getUrl());
                                    usuarioModificado.setFotoId(res.getFileId());
                                } else {
                                    // Si no hay foto nueva, mantenemos los datos de la foto antigua
                                    usuarioModificado.setFotoPerfil(usuarioActual.getFotoPerfil());
                                    usuarioModificado.setFotoId(usuarioActual.getFotoId());
                                }

                                // Comprueba si se ha cambiado el username
                                if (!usuarioActual
                                        .getUsername()
                                        .equals(usuarioModificado.getUsername())) {
                                    // Si el nombre es distinto, comprobamos que el nuevo no esté pillado
                                    return esUsernameUnico(usuarioModificado.getUsername())
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
                                                                usuarioModificado);
                                                    });
                                }

                                // Si el nombre es el mismo que ya tenía se guarda directamente
                                return usuarioRepository.saveUsuario(usuarioModificado);
                            });
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

    /*
     * ================================================================
     * ==================== Private helpers ===========================
     * ================================================================
     */

    /**
     * Verifica si el email ingresado coincide con el estándar.
     *
     * @param email Correo a validar.
     * @return true si es válido, false en caso contrario.
     */
    private boolean emailValido(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Normaliza los datos del usuario a registrar (username en minúsculas, espacios en blanco, etc).
     *
     * @param usuario Usuario a normalizar.
     */
    private void normalizarDatos(Usuario usuario) {
        if (usuario.getUsername() != null) {
            usuario.setUsername(usuario.getUsername().toLowerCase().trim());
        }

        if (usuario.getEmail() != null) {
            usuario.setEmail(usuario.getEmail().toLowerCase().trim());
        }

        if (usuario.getNombre() != null) {
            usuario.setNombre
                    (StringUtils.capitalizeNombreCompleto(usuario.getNombre()));
        }

        if (usuario.getCiudad() != null) {
            usuario.setCiudad(StringUtils.capitalize(usuario.getCiudad()));
        }

        if (usuario.getOtraPreferencia() instanceof String pref) {
            String textoPref = pref.trim();
            if (!textoPref.isEmpty()) {
                usuario.setOtraPreferencia(StringUtils.capitalize(textoPref));
            } else {
                usuario.setOtraPreferencia(false);
            }
        }
    }

    /**
     * Valida los datos del usuario.
     *
     * @param usuario Usuario a validar.
     * @return 0 si es todo correcto, en caso contrario devuelve el mensaje de error.
     */
    private int validarDatos(Usuario usuario) {

        if (usuario.getEmail().isEmpty() || usuario.getUsername().isEmpty()
                || usuario.getNombre().isEmpty() || usuario.getCiudad().isEmpty()) {
            return R.string.usuario_empty_fields_error_message;
        }

        /* === LOCAL === */
        if (usuario instanceof UsuarioLocal local) {
            if (local.getDireccion().isEmpty() || local.getTelefono().isEmpty()) {
                return R.string.usuario_empty_fields_error_message;
            }
        }

        if (!emailValido(usuario.getEmail())) {
            return R.string.email_validation_error_message;
        }

        return 0;
    }

}
