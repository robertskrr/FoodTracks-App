/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.services;

import static org.junit.Assert.assertEquals;

import com.foodtracks.app.R;
import com.foodtracks.app.repositories.interfaces.ILikeRepository;
import com.foodtracks.app.repositories.interfaces.IPublicacionRepository;
import com.foodtracks.app.repositories.interfaces.IRegistroBorradoRepository;
import com.foodtracks.app.repositories.interfaces.IStorageRepository;
import com.foodtracks.app.repositories.interfaces.IUsuarioRepository;
import com.foodtracks.app.repositories.interfaces.IValoracionLocalRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UsuarioServiceTest {

    @Mock private IUsuarioRepository mockUsuRepo;
    @Mock private IRegistroBorradoRepository mockRegRepo;
    @Mock private IStorageRepository mockStorageRepo;
    @Mock private IPublicacionRepository mockPubRepo;
    @Mock private ILikeRepository mockLikeRepo;
    @Mock private IValoracionLocalRepository mockValRepo;

    private UsuarioService usuarioService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        usuarioService =
                new UsuarioService(
                        mockUsuRepo,
                        mockRegRepo,
                        mockStorageRepo,
                        mockPubRepo,
                        mockLikeRepo,
                        mockValRepo);
    }

    @Test
    public void validarCredenciales_CamposVacios_DevuelveError() {
        int error = usuarioService.validarCredenciales("", "12345678", "12345678");
        assertEquals(R.string.usuario_empty_fields_error_message, error);
    }

    @Test
    public void validarCredenciales_PasswordCorta_DevuelveError() {
        int error = usuarioService.validarCredenciales("test@test.com", "12345", "12345");
        assertEquals(R.string.password_length_error_message, error);
    }

    @Test
    public void validarCredenciales_PasswordsNoCoinciden_DevuelveError() {
        int error = usuarioService.validarCredenciales("test@test.com", "12345678", "abcdefgh");
        assertEquals(R.string.passwords_dont_match_error_message, error);
    }

    @Test
    public void validarCredenciales_DatosCorrectos_DevuelveCero() {
        int error =
                usuarioService.validarCredenciales(
                        "correo@valido.com", "PasswordSegura1", "PasswordSegura1");
        assertEquals(0, error);
    }
}
