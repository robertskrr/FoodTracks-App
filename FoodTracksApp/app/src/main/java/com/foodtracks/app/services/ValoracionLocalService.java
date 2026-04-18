/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.services;

import com.foodtracks.app.R;
import com.foodtracks.app.models.UsuarioLocal;
import com.foodtracks.app.models.ValoracionLocal;
import com.foodtracks.app.repositories.interfaces.IUsuarioRepository;
import com.foodtracks.app.repositories.interfaces.IValoracionLocalRepository;
import com.foodtracks.app.services.exceptions.FoodTracksNotFoundException;
import com.foodtracks.app.services.exceptions.FoodTracksValidationException;
import com.foodtracks.app.services.interfaces.IValoracionLocalService;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Lógica de negocio para la gestión de valoraciones de locales.
 *
 * @author Robert
 * @since 16/04
 */
public class ValoracionLocalService implements IValoracionLocalService {
    private final IValoracionLocalRepository valoracionLocalRepository;
    private final IUsuarioRepository usuarioRepository;

    public ValoracionLocalService(
            IValoracionLocalRepository valoracionLocalRepository,
            IUsuarioRepository usuarioRepository) {
        this.valoracionLocalRepository = valoracionLocalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Task<Void> valorarLocal(ValoracionLocal valoracion) {
        String customId = getCustomId(valoracion.getUidCliente(), valoracion.getUidLocal());
        valoracion.setUid(customId);

        int errorIdValidacion = validarDatos(valoracion);
        if (errorIdValidacion != 0) {
            return Tasks.forException(new FoodTracksValidationException(errorIdValidacion));
        }

        return valoracionLocalRepository
                .getValoracion(customId)
                .continueWithTask(
                        task -> {
                            DocumentSnapshot ratingDoc = task.getResult();

                            // Recuperamos el perfil del local para recalcular la media
                            return usuarioRepository
                                    .getUsuarioById(valoracion.getUidLocal())
                                    .continueWithTask(
                                            userTask -> {
                                                DocumentSnapshot localDoc = userTask.getResult();
                                                if (!localDoc.exists()) {
                                                    return Tasks.forException(
                                                            new FoodTracksNotFoundException(
                                                                    R.string
                                                                            .local_not_found_error_message));
                                                }

                                                // Convertimos al modelo
                                                UsuarioLocal local =
                                                        localDoc.toObject(UsuarioLocal.class);
                                                assert local != null;
                                                double mediaActual = local.getPuntuacionMedia();
                                                long totalActualValoraciones =
                                                        local.getTotalValoraciones();

                                                double nuevaMedia;
                                                long nuevoTotalValoraciones;

                                                // -- ACTUALIZACIÓN DE MEDIA SI EXISTE --
                                                if (ratingDoc.exists()) {
                                                    ValoracionLocal votoAntiguo =
                                                            ratingDoc.toObject(
                                                                    ValoracionLocal.class);
                                                    assert votoAntiguo != null;
                                                    double notaVieja = votoAntiguo.getPuntuacion();
                                                    double notaNueva = valoracion.getPuntuacion();

                                                    // Solo se ajusta el peso de la nota, no el
                                                    // total
                                                    nuevaMedia =
                                                            ((mediaActual * totalActualValoraciones)
                                                                            - notaVieja
                                                                            + notaNueva)
                                                                    / totalActualValoraciones;
                                                    nuevoTotalValoraciones =
                                                            totalActualValoraciones;
                                                } else {
                                                    // -- NUEVA VALORACIÓN --
                                                    double notaNueva = valoracion.getPuntuacion();

                                                    // Se añade un voto más y se calcula la nueva
                                                    // media
                                                    nuevoTotalValoraciones =
                                                            totalActualValoraciones + 1;
                                                    nuevaMedia =
                                                            ((mediaActual * totalActualValoraciones)
                                                                            + notaNueva)
                                                                    / (nuevoTotalValoraciones);
                                                }

                                                // Actualizamos el nuevo total del local
                                                local.setPuntuacionMedia(nuevaMedia);
                                                local.setTotalValoraciones(nuevoTotalValoraciones);

                                                return valoracionLocalRepository
                                                        .saveValoracion(valoracion)
                                                        .continueWithTask(
                                                                unused ->
                                                                        usuarioRepository
                                                                                .saveUsuario(
                                                                                        local));
                                            });
                        });
    }

    @Override
    public Task<ValoracionLocal> getValoracionUsuario(String uidCliente, String uidLocal) {
        return valoracionLocalRepository
                .getValoracion(getCustomId(uidCliente, uidLocal))
                .continueWithTask(
                        task -> {
                            DocumentSnapshot doc = task.getResult();

                            if (task.isSuccessful() && doc != null && doc.exists()) {
                                return Tasks.forResult(doc.toObject(ValoracionLocal.class));
                            } else {
                                return Tasks.forException(
                                        new FoodTracksNotFoundException(
                                                R.string.valoracion_not_found_error_message));
                            }
                        });
    }

    @Override
    public Task<Void> eliminarValoracion(String uidCliente, String uidLocal) {
        String customId = getCustomId(uidCliente, uidLocal);
        return valoracionLocalRepository
                .getValoracion(customId)
                .continueWithTask(
                        task -> {
                            DocumentSnapshot ratingDoc = task.getResult();

                            if (!ratingDoc.exists()) {
                                return Tasks.forException(
                                        new FoodTracksNotFoundException(
                                                R.string.valoracion_not_found_error_message));
                            }

                            ValoracionLocal votoABorrar = ratingDoc.toObject(ValoracionLocal.class);

                            // Buscamos al local para restarle el voto al total de valoraciones
                            return usuarioRepository
                                    .getUsuarioById(uidLocal)
                                    .continueWithTask(
                                            userTask -> {
                                                DocumentSnapshot localDoc = userTask.getResult();

                                                if (!localDoc.exists()) {
                                                    return Tasks.forException(
                                                            new FoodTracksNotFoundException(
                                                                    R.string
                                                                            .local_not_found_error_message));
                                                }

                                                UsuarioLocal local =
                                                        userTask.getResult()
                                                                .toObject(UsuarioLocal.class);
                                                assert local != null;

                                                double mediaActual = local.getPuntuacionMedia();
                                                long totalActualValoraciones =
                                                        local.getTotalValoraciones();

                                                assert votoABorrar != null;
                                                double notaBorrada = votoABorrar.getPuntuacion();

                                                // Calculamos nuevos valores
                                                long nuevoTotalValoraciones =
                                                        totalActualValoraciones - 1;
                                                double nuevaMedia =
                                                        (nuevoTotalValoraciones <= 0)
                                                                ? 0
                                                                : ((mediaActual
                                                                                        * totalActualValoraciones)
                                                                                - notaBorrada)
                                                                        / nuevoTotalValoraciones;

                                                local.setTotalValoraciones(nuevoTotalValoraciones);
                                                local.setPuntuacionMedia(nuevaMedia);

                                                // Borramos la reseña y actualizamos el local
                                                return valoracionLocalRepository
                                                        .deleteValoracion(customId)
                                                        .continueWithTask(
                                                                unused ->
                                                                        usuarioRepository
                                                                                .saveUsuario(
                                                                                        local));
                                            });
                        });
    }

    /*
     * ================================================================
     * ==================== Private helpers ===========================
     * ================================================================
     */

    private String getCustomId(String uidCliente, String uidLocal) {
        return uidCliente + "_" + uidLocal;
    }

    /**
     * Valida los datos de la valoración.
     *
     * @param valoracion Valoración a validar.
     * @return 0 si es todo correcto, en caso contrario devuelve el mensaje de error.
     */
    private int validarDatos(ValoracionLocal valoracion) {
        if (valoracion.getPuntuacion() < 0.5 || valoracion.getPuntuacion() > 5) {
            return R.string.valoracion_invalid_puntuation_error_message;
        }

        return 0;
    }
}
