/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.services;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.foodtracks.app.R;
import com.foodtracks.app.api.imagekit.ImageKitResponse;
import com.foodtracks.app.models.LikePublicacion;
import com.foodtracks.app.models.Publicacion;
import com.foodtracks.app.models.RegistroBorradoUsuario;
import com.foodtracks.app.models.Usuario;
import com.foodtracks.app.models.UsuarioAdmin;
import com.foodtracks.app.models.UsuarioCliente;
import com.foodtracks.app.models.UsuarioLocal;
import com.foodtracks.app.models.ValoracionLocal;
import com.foodtracks.app.repositories.interfaces.ILikeRepository;
import com.foodtracks.app.repositories.interfaces.IPublicacionRepository;
import com.foodtracks.app.repositories.interfaces.IRegistroBorradoRepository;
import com.foodtracks.app.repositories.interfaces.IStorageRepository;
import com.foodtracks.app.repositories.interfaces.IUsuarioRepository;
import com.foodtracks.app.repositories.interfaces.IValoracionLocalRepository;
import com.foodtracks.app.services.exceptions.FoodTracksNotFoundException;
import com.foodtracks.app.services.exceptions.FoodTracksValidationException;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.foodtracks.app.utils.StringUtils;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

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
    private final IPublicacionRepository publicacionRepository;
    private final ILikeRepository likeRepository;
    private final IValoracionLocalRepository valoracionLocalRepository;

    public UsuarioService(
            IUsuarioRepository usuarioRepository,
            IRegistroBorradoRepository registroBorradoRepository,
            IStorageRepository storageRepository,
            IPublicacionRepository publicacionRepository,
            ILikeRepository likeRepository,
            IValoracionLocalRepository valoracionLocalRepository) {
        this.usuarioRepository = usuarioRepository;
        this.registroBorradoRepository = registroBorradoRepository;
        this.storageRepository = storageRepository;
        this.publicacionRepository = publicacionRepository;
        this.likeRepository = likeRepository;
        this.valoracionLocalRepository = valoracionLocalRepository;
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
                                String rol = doc.getString("rol");
                                Usuario usuario;

                                // Transformamos al tipo de usuario correspondiente
                                assert rol != null;
                                if (rol.equals("local")) {
                                    usuario = doc.toObject(UsuarioLocal.class);
                                } else if (rol.equals("admin")) {
                                    usuario = doc.toObject(UsuarioAdmin.class);
                                } else {
                                    usuario = doc.toObject(UsuarioCliente.class);
                                }

                                return Tasks.forResult(usuario);
                            } else {
                                return Tasks.forException(
                                        new FoodTracksNotFoundException(
                                                R.string.usuario_not_found_error_message));
                            }
                        });
    }

    @Override
    public Task<Void> registrarUsuario(Usuario usuario, Uri fotoUri) {
        int errorIdValidacion = validarDatos(usuario);
        if (errorIdValidacion != 0) {
            return Tasks.forException(new FoodTracksValidationException(errorIdValidacion));
        }

        usuario.setFechaRegistro(Timestamp.now());
        normalizarDatos(usuario);

        return esUsernameUnico(usuario.getUsername())
                .continueWithTask(
                        task -> {
                            if (task.getResult() != null && !task.getResult()) {
                                return Tasks.forException(
                                        new FoodTracksValidationException(
                                                R.string.username_validation_error_message));
                            }

                            // Subimos la foto de perfil a ImageKit
                            if (fotoUri != null) {
                                return storageRepository
                                        .uploadImage(fotoUri, usuario.getUid(), "perfiles")
                                        .continueWithTask(
                                                uploadTask -> {
                                                    ImageKitResponse res = uploadTask.getResult();

                                                    // Asociamos la URL y el ID de la foto al
                                                    // usuario
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
    public Task<Void> eliminarCuentaByAdmin(String uidUsuario, String motivo, String uidAdmin) {
        return usuarioRepository
                .getUsuarioById(uidUsuario)
                .continueWithTask(
                        task -> {
                            DocumentSnapshot doc = task.getResult();

                            if (!doc.exists()) {
                                return Tasks.forException(
                                        new FoodTracksNotFoundException(
                                                R.string.usuario_not_found_error_message));
                            }

                            String rol = doc.getString("rol");
                            Usuario usuarioABorrar;

                            if ("local".equals(rol)) {
                                usuarioABorrar = doc.toObject(UsuarioLocal.class);
                            } else if ("admin".equals(rol)) {
                                usuarioABorrar = doc.toObject(UsuarioAdmin.class);
                            } else {
                                usuarioABorrar = doc.toObject(UsuarioCliente.class);
                            }

                            assert usuarioABorrar != null;

                            // Creamos el registro de borrado
                            RegistroBorradoUsuario registro =
                                    RegistroBorradoUsuario.builder()
                                            .uidUsuario(uidUsuario)
                                            .uidAdmin(uidAdmin)
                                            .usernameUsuario(usuarioABorrar.getUsername())
                                            .motivo(motivo)
                                            .fechaHora(Timestamp.now())
                                            .build();

                            // Log de auditoría -> Borramos rastro del usuario -> Borramos usuario
                            return registroBorradoRepository
                                    .saveRegistroBorradoUsuario(registro)
                                    .continueWithTask(unused -> borrarRastroUsuario(usuarioABorrar))
                                    .continueWithTask(
                                            unused -> usuarioRepository.deleteUsuario(uidUsuario));
                        });
    }

    @Override
    public Task<Void> eliminarCuenta(String uid) {
        return usuarioRepository
                .getUsuarioById(uid)
                .continueWithTask(
                        task -> {
                            DocumentSnapshot doc = task.getResult();

                            if (!doc.exists()) {
                                return Tasks.forException(
                                        new FoodTracksNotFoundException(
                                                R.string.usuario_not_found_error_message));
                            }

                            String rol = doc.getString("rol");
                            Usuario usuarioABorrar;

                            if ("local".equals(rol)) {
                                usuarioABorrar = doc.toObject(UsuarioLocal.class);
                            } else if ("admin".equals(rol)) {
                                usuarioABorrar = doc.toObject(UsuarioAdmin.class);
                            } else {
                                usuarioABorrar = doc.toObject(UsuarioCliente.class);
                            }

                            assert usuarioABorrar != null;
                            // Borramos el rastro del usuario -> Borramos el documento de usuario
                            return borrarRastroUsuario(usuarioABorrar)
                                    .continueWithTask(
                                            unused -> usuarioRepository.deleteUsuario(uid));
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
    public Task<Void> actualizarPerfil(Usuario usuario, Uri fotoUri) {
        // Comprobamos si el username ya existe en otro usuario
        return usuarioRepository
                .getUsuarioByUsername(usuario.getUsername())
                .continueWithTask(
                        task -> {
                            if (task.isSuccessful()
                                    && task.getResult() != null
                                    && !task.getResult().isEmpty()) {
                                DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                                if (!doc.getId().equals(usuario.getUid())) {
                                    return Tasks.forException(
                                            new FoodTracksValidationException(
                                                    R.string.username_validation_error_message));
                                }
                            }

                            int errorIdValidacion = validarDatos(usuario);
                            if (errorIdValidacion != 0) {
                                return Tasks.forException(
                                        new FoodTracksValidationException(errorIdValidacion));
                            }

                            normalizarDatos(usuario);

                            // Actualizamos la foto de perfil
                            if (fotoUri != null) {
                                // Si ya tenía una foto anterior la borramos
                                if (usuario.getFotoId() != null) {
                                    storageRepository.deleteImage(usuario.getFotoId());
                                }

                                return storageRepository
                                        .uploadImage(
                                                fotoUri,
                                                usuario.getUid() + "_" + System.currentTimeMillis(),
                                                "perfiles")
                                        .continueWithTask(
                                                uploadTask -> {
                                                    ImageKitResponse res = uploadTask.getResult();
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
    public Task<List<Usuario>> buscarUsuarios(String query) {
        // Siempre en minúsculas
        String cleanQuery = (query != null) ? query.toLowerCase().trim() : "";

        return usuarioRepository
                .searchUsuariosByField("username", cleanQuery)
                .continueWith(
                        task -> {
                            List<Usuario> listaUsuarios = new ArrayList<>();

                            if (task.isSuccessful() && task.getResult() != null) {
                                for (DocumentSnapshot doc : task.getResult()) {
                                    String rol = doc.getString("rol");

                                    if (rol != null) {
                                        switch (rol) {
                                            case "local":
                                                listaUsuarios.add(doc.toObject(UsuarioLocal.class));
                                                break;
                                            case "admin":
                                                listaUsuarios.add(doc.toObject(UsuarioAdmin.class));
                                                break;
                                            case "cliente":
                                            default:
                                                listaUsuarios.add(
                                                        doc.toObject(UsuarioCliente.class));
                                                break;
                                        }
                                    }
                                }
                            }
                            return listaUsuarios;
                        });
    }

    @Override
    public Task<Usuario> getUsuarioByUsernameExacto(String username) {
        String cleanUsername = (username != null) ? username.toLowerCase().trim() : "";

        if (cleanUsername.isEmpty()) {
            return Tasks.forException(
                    new FoodTracksValidationException(R.string.username_empty_error_message));
        }

        return usuarioRepository
                .getUsuarioByUsername(cleanUsername)
                .continueWithTask(
                        task -> {
                            if (task.isSuccessful()
                                    && task.getResult() != null
                                    && !task.getResult().isEmpty()) {
                                DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                                String rol = doc.getString("rol");

                                Usuario usuario;
                                if ("local".equals(rol)) {
                                    usuario = doc.toObject(UsuarioLocal.class);
                                } else if ("admin".equals(rol)) {
                                    usuario = doc.toObject(UsuarioAdmin.class);
                                } else {
                                    usuario = doc.toObject(UsuarioCliente.class);
                                }

                                return Tasks.forResult(usuario);
                            } else {
                                return Tasks.forException(
                                        new FoodTracksNotFoundException(
                                                R.string.usuario_not_found_error_message));
                            }
                        });
    }

    @Override
    public Task<List<Usuario>> buscarLocalesPorFiltros(
            String ciudad,
            boolean vegano,
            boolean vegetariano,
            boolean sinLactosa,
            boolean celiaco,
            String otraPreferencia) {

        String cleanCiudad =
                (ciudad != null && !ciudad.trim().isEmpty())
                        ? StringUtils.capitalize(ciudad)
                        : null;
        String cleanOtraPreferencia =
                (otraPreferencia != null && !otraPreferencia.trim().isEmpty())
                        ? StringUtils.capitalize(otraPreferencia)
                        : null;

        return usuarioRepository
                .searchLocalesByFiltros(
                        cleanCiudad, vegano, vegetariano, sinLactosa, celiaco, cleanOtraPreferencia)
                .continueWith(
                        task -> {
                            List<Usuario> listaLocales = new ArrayList<>();
                            if (task.isSuccessful() && task.getResult() != null) {
                                for (DocumentSnapshot doc : task.getResult()) {
                                    listaLocales.add(doc.toObject(UsuarioLocal.class));
                                }
                            }
                            return listaLocales;
                        });
    }

    @Override
    public Task<List<Usuario>> buscarLocalesPorMisPreferencias(
            String uidUsuario, String ciudadOpcional) {
        // Recuperamos el perfil del usuario protagonista
        return getPerfil(uidUsuario)
                .continueWithTask(
                        task -> {
                            if (!task.isSuccessful() || task.getResult() == null) {
                                return Tasks.forException(
                                        new FoodTracksNotFoundException(
                                                R.string.profile_not_available_to_filter));
                            }

                            Usuario usuario = task.getResult();

                            // Recuperamos la otra preferencia si es String
                            String otraPreferencia =
                                    (usuario.getOtraPreferencia() instanceof String)
                                            ? (String) usuario.getOtraPreferencia()
                                            : null;

                            return buscarLocalesPorFiltros(
                                    ciudadOpcional,
                                    usuario.isEsVegano(),
                                    usuario.isEsVegetariano(),
                                    usuario.isSinLactosa(),
                                    usuario.isEsCeliaco(),
                                    otraPreferencia);
                        });
    }

    @Override
    public Task<List<Usuario>> buscarLocalesPorUsername(String username) {
        String cleanUsername = (username != null) ? username.toLowerCase().trim() : "";

        return usuarioRepository
                .searchLocalesByUsername(cleanUsername)
                .continueWith(
                        task -> {
                            List<Usuario> listaLocales = new ArrayList<>();
                            if (task.isSuccessful() && task.getResult() != null) {
                                for (DocumentSnapshot doc : task.getResult()) {
                                    listaLocales.add(doc.toObject(UsuarioLocal.class));
                                }
                            }
                            return listaLocales;
                        });
    }

    @Override
    public Task<Void> registrarVisitaPerfil(String uidVisitante, String uidLocal) {
        if (uidVisitante != null && uidVisitante.equals(uidLocal)) {
            return Tasks.forResult(
                    null); // No hace nada si es el mismo local el que visita su perfil
        }

        return usuarioRepository.incrementarVisitasPerfil(uidLocal);
    }

    @Override
    public Task<List<RegistroBorradoUsuario>> getAllRegistrosBorradoUsuarios() {
        return registroBorradoRepository
                .getAllRegistrosUsuarios()
                .continueWith(
                        task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                return task.getResult().toObjects(RegistroBorradoUsuario.class);
                            }
                            return new java.util.ArrayList<>();
                        });
    }

    @Override
    public Task<List<Usuario>> getUltimosUsuariosRegistrados(int limite) {
        return usuarioRepository
                .getUltimosUsuariosRegistrados(limite)
                .continueWith(
                        task -> {
                            List<Usuario> listaUsuarios = new ArrayList<>();

                            if (task.isSuccessful() && task.getResult() != null) {
                                for (DocumentSnapshot doc : task.getResult()) {
                                    String rol = doc.getString("rol");

                                    if (rol != null) {
                                        switch (rol) {
                                            case "local":
                                                listaUsuarios.add(doc.toObject(UsuarioLocal.class));
                                                break;
                                            case "admin":
                                                listaUsuarios.add(doc.toObject(UsuarioAdmin.class));
                                                break;
                                            case "cliente":
                                            default:
                                                listaUsuarios.add(
                                                        doc.toObject(UsuarioCliente.class));
                                                break;
                                        }
                                    }
                                }
                            }
                            return listaUsuarios;
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
     * Verifica si la URL del sitio web ingresada es válida.
     * @param url Dirección web a validar.
     * @return true si el formato es correcto, false en caso contrario.
     */
    private boolean urlValida(String url) {
        return url != null && android.util.Patterns.WEB_URL.matcher(url).matches();
    }

    /**
     * Normaliza los datos del usuario a registrar (username en minúsculas, espacios en blanco, etc.).
     *
     * @param usuario Usuario a normalizar.
     */
    private void normalizarDatos(Usuario usuario) {
        if (usuario.getUsername() != null) {
            usuario.setUsername(StringUtils.quitarAllEspacios(usuario.getUsername().toLowerCase()));
        }

        if (usuario.getEmail() != null) {
            usuario.setEmail(usuario.getEmail().toLowerCase().trim());
        }

        if (usuario.getNombre() != null) {
            usuario.setNombre(StringUtils.capitalizeNombreCompleto(usuario.getNombre().trim()));
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

        if (usuario instanceof UsuarioLocal local) {
            if (local.getNombre() != null) {
                local.setNombre(StringUtils.capitalize(local.getNombre()));
            }
            if (local.getTelefono() != null) {
                local.setTelefono(local.getTelefono().trim());
            }

            if (local.getDireccion() != null) {
                local.setDireccion(StringUtils.capitalizePrimeraLetra(local.getDireccion()));
            }

            if (local.getSitioWeb() != null) {
                String web = local.getSitioWeb().trim();
                // Si no hay nada escrito lo guarda como NULL real en BBDD
                local.setSitioWeb(web.isEmpty() ? null : web);
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

        if (usuario.getEmail().isEmpty()
                || usuario.getUsername().isEmpty()
                || usuario.getNombre().isEmpty()
                || usuario.getCiudad().isEmpty()) {
            return R.string.usuario_empty_fields_error_message;
        }

        if (!emailValido(usuario.getEmail())) {
            return R.string.email_validation_error_message;
        }

        /* === LOCAL === */
        if (usuario instanceof UsuarioLocal local) {
            if (local.getDireccion().isEmpty() || local.getTelefono().isEmpty()) {
                return R.string.usuario_empty_fields_error_message;
            }

            if (local.getTelefono().length() != 9 || !TextUtils.isDigitsOnly(local.getTelefono())) {
                return R.string.local_phone_must_be_9_digits_error_message;
            }

            if (local.getSitioWeb() != null && !local.getSitioWeb().isEmpty()) {
                if (!urlValida(local.getSitioWeb())) {
                    return R.string.local_invalid_url_error_message;
                }
            }
        }

        return 0;
    }

    /**
     * Elimina en cascada y en paralelo todos los datos relacionados con un usuario:
     * Fotos, publicaciones, likes emitidos, valoraciones emitidas y recibidas.
     */
    private Task<Void> borrarRastroUsuario(Usuario usuario) {
        // Lista de tareas de borrado que hay que realizar
        List<Task<?>> tareasCascada = new ArrayList<>();
        String uid = usuario.getUid();

        // Borrar foto de perfil
        if (usuario.getFotoId() != null) {
            tareasCascada.add(storageRepository.deleteImage(usuario.getFotoId()));
            Log.d("Borrado usuario: FOTO PERFIL", "Borrar foto de perfil");
        }

        // Borrar sus publicaciones (fotos, likes asociados y la propia publicación)
        Task<Void> tareaPublicaciones =
                publicacionRepository
                        .getPublicacionesByUsuario(uid)
                        .continueWithTask(
                                task -> {
                                    if (!task.isSuccessful() || task.getResult() == null)
                                        return Tasks.forResult(null);

                                    List<Task<?>> tareasPosts = new ArrayList<>();
                                    WriteBatch batch = FirebaseFirestore.getInstance().batch();

                                    for (DocumentSnapshot doc : task.getResult()) {
                                        Publicacion publicacion = doc.toObject(Publicacion.class);
                                        if (publicacion != null) {
                                            if (publicacion.getImagenId() != null) {
                                                // Borramos las fotos
                                                tareasPosts.add(
                                                        storageRepository.deleteImage(
                                                                publicacion.getImagenId()));
                                            }
                                            // Borramos los likes
                                            tareasPosts.add(
                                                    likeRepository.deleteAllLikesByPublicacion(
                                                            publicacion.getUid()));

                                            // Borramos la publicación
                                            batch.delete(doc.getReference());
                                        }
                                    }
                                    tareasPosts.add(batch.commit());
                                    Log.d(
                                            "Borrado usuario: PUBLICACIONES",
                                            "Likes asociados, fotos y publicacion borradas");
                                    return Tasks.whenAll(tareasPosts);
                                });
        tareasCascada.add(tareaPublicaciones);

        // Borrar likes emitidos por este usuario a otras publicaciones (y restar contadores de
        // likes)
        Task<Void> tareaLikes =
                likeRepository
                        .getLikesByUsuario(uid)
                        .continueWithTask(
                                task -> {
                                    if (!task.isSuccessful() || task.getResult() == null)
                                        return Tasks.forResult(null);

                                    List<Task<?>> decrementos = new ArrayList<>();
                                    WriteBatch batch = FirebaseFirestore.getInstance().batch();

                                    for (DocumentSnapshot doc : task.getResult()) {
                                        LikePublicacion like = doc.toObject(LikePublicacion.class);
                                        if (like != null) {
                                            decrementos.add(
                                                    publicacionRepository.actualizarContadorLikes(
                                                            like.getUidPublicacion(), -1));
                                            batch.delete(doc.getReference());
                                        }
                                    }
                                    decrementos.add(batch.commit());
                                    Log.d(
                                            "Borrado usuario: LIKES EMITIDOS",
                                            "Likes emitidos borrados y contadores de likes actualizados");
                                    return Tasks.whenAll(decrementos);
                                });
        tareasCascada.add(tareaLikes);

        // Borrar valoraciones emitidas (como cliente) y recalcular la media del local
        Task<Void> tareaValoracionesCliente =
                valoracionLocalRepository
                        .getValoracionesByCliente(uid)
                        .continueWithTask(
                                task -> {
                                    if (!task.isSuccessful() || task.getResult() == null)
                                        return Tasks.forResult(null);

                                    List<Task<?>> recalculos = new ArrayList<>();
                                    WriteBatch batch = FirebaseFirestore.getInstance().batch();

                                    for (DocumentSnapshot doc : task.getResult()) {
                                        ValoracionLocal val = doc.toObject(ValoracionLocal.class);
                                        if (val != null) {
                                            recalculos.add(
                                                    usuarioRepository
                                                            .getUsuarioById(val.getUidLocal())
                                                            .continueWithTask(
                                                                    localTask -> {
                                                                        DocumentSnapshot localDoc =
                                                                                localTask
                                                                                        .getResult();
                                                                        if (localDoc != null
                                                                                && localDoc
                                                                                        .exists()) {
                                                                            UsuarioLocal local =
                                                                                    localDoc
                                                                                            .toObject(
                                                                                                    UsuarioLocal
                                                                                                            .class);
                                                                            if (local != null) {
                                                                                // Recalcula la
                                                                                // media
                                                                                double media =
                                                                                        local
                                                                                                .getPuntuacionMedia();
                                                                                long total =
                                                                                        local
                                                                                                .getTotalValoraciones();
                                                                                double nota =
                                                                                        val
                                                                                                .getPuntuacion();

                                                                                long nuevoTotal =
                                                                                        total - 1;
                                                                                double nuevaMedia =
                                                                                        (nuevoTotal
                                                                                                        <= 0)
                                                                                                ? 0
                                                                                                : ((media
                                                                                                                        * total)
                                                                                                                - nota)
                                                                                                        / nuevoTotal;

                                                                                local
                                                                                        .setTotalValoraciones(
                                                                                                nuevoTotal);
                                                                                local
                                                                                        .setPuntuacionMedia(
                                                                                                nuevaMedia);
                                                                                // Actualiza los
                                                                                // nuevos datos del
                                                                                // local
                                                                                return usuarioRepository
                                                                                        .saveUsuario(
                                                                                                local);
                                                                            }
                                                                        }
                                                                        return Tasks.forResult(
                                                                                null);
                                                                    }));
                                            batch.delete(doc.getReference());
                                        }
                                    }
                                    recalculos.add(batch.commit());
                                    Log.d(
                                            "Borrado usuario: VALORACIONES EMITIDAS",
                                            "Valoraciones eliminadas y medias recalculadas");
                                    return Tasks.whenAll(recalculos);
                                });
        tareasCascada.add(tareaValoracionesCliente);

        // Si el usuario que se borra es local, borrar todas las valoraciones que haya recibido
        if ("local".equals(usuario.getRol())) {
            Task<Void> tareaValoracionesLocal =
                    valoracionLocalRepository
                            .getValoracionesByLocal(uid)
                            .continueWithTask(
                                    task -> {
                                        if (!task.isSuccessful() || task.getResult() == null)
                                            return Tasks.forResult(null);
                                        WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                        for (DocumentSnapshot doc : task.getResult()) {
                                            // Borra las valoraciones
                                            batch.delete(doc.getReference());
                                        }
                                        return batch.commit();
                                    });
            Log.d("Borrado usuario: VALORACIONES RECIBIDAS", "Valoraciones eliminadas");
            tareasCascada.add(tareaValoracionesLocal);
        }

        // Ejecutar todas las tareas simultáneamente
        Log.d("Borrado usuario", "RASTRO DE USUARIO BORRADO CON ÉXITO");
        return Tasks.whenAll(tareasCascada);
    }
}
