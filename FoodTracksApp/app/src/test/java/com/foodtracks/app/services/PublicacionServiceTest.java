package com.foodtracks.app.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.foodtracks.app.R;
import com.foodtracks.app.models.Publicacion;
import com.foodtracks.app.repositories.interfaces.ILikeRepository;
import com.foodtracks.app.repositories.interfaces.IPublicacionRepository;
import com.foodtracks.app.repositories.interfaces.IRegistroBorradoRepository;
import com.foodtracks.app.repositories.interfaces.IStorageRepository;
import com.foodtracks.app.repositories.interfaces.IUsuarioRepository;
import com.foodtracks.app.services.exceptions.FoodTracksValidationException;

import com.google.android.gms.tasks.Task;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PublicacionServiceTest {

    @Mock private IPublicacionRepository mockPubRepo;
    @Mock private IUsuarioRepository mockUsuarioRepo;
    @Mock private IRegistroBorradoRepository mockRegRepo;
    @Mock private IStorageRepository mockStorageRepo;
    @Mock private ILikeRepository mockLikeRepo;

    private PublicacionService publicacionService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        publicacionService = new PublicacionService(
                mockPubRepo, mockUsuarioRepo, mockRegRepo, mockStorageRepo, mockLikeRepo);
    }

    @Test
    public void subirPublicacion_TextoVacio_LanzaExcepcion() {
        Publicacion pubVacia = new Publicacion();
        pubVacia.setTexto("");

        Task<Void> resultado = publicacionService.subirPublicacion(pubVacia, null);

        assertTrue(resultado.getException() instanceof FoodTracksValidationException);
        FoodTracksValidationException ex = (FoodTracksValidationException) resultado.getException();
        assertEquals(R.string.publicacion_text_empty_error_message, ex.getErrorResId());
    }

    @Test
    public void subirPublicacion_TextoMasDe500Caracteres_LanzaExcepcion() {
        Publicacion pubLarga = new Publicacion();
        pubLarga.setTexto(new String(new char[501]).replace('\0', 'a'));

        Task<Void> resultado = publicacionService.subirPublicacion(pubLarga, null);

        assertTrue(resultado.getException() instanceof FoodTracksValidationException);
        FoodTracksValidationException ex = (FoodTracksValidationException) resultado.getException();
        assertEquals(R.string.publicacion_too_long_error_message, ex.getErrorResId());
    }
}