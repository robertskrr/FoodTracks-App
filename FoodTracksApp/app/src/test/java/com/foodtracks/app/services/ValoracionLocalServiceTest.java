/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.foodtracks.app.R;
import com.foodtracks.app.models.ValoracionLocal;
import com.foodtracks.app.repositories.interfaces.IUsuarioRepository;
import com.foodtracks.app.repositories.interfaces.IValoracionLocalRepository;
import com.foodtracks.app.services.exceptions.FoodTracksValidationException;

import com.google.android.gms.tasks.Task;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ValoracionLocalServiceTest {

    @Mock private IValoracionLocalRepository mockValoracionRepo;

    @Mock private IUsuarioRepository mockUsuarioRepo;

    private ValoracionLocalService valoracionService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        valoracionService = new ValoracionLocalService(mockValoracionRepo, mockUsuarioRepo);
    }

    @Test
    public void valorarLocal_PuntuacionMayorQue5_LanzaExcepcion() {
        // When
        ValoracionLocal valoracionMala = new ValoracionLocal();
        valoracionMala.setUidCliente("cliente1");
        valoracionMala.setUidLocal("local1");
        valoracionMala.setPuntuacion(6.0);

        // Then
        Task<Void> resultado = valoracionService.valorarLocal(valoracionMala);

        // Verify
        assertTrue(resultado.getException() instanceof FoodTracksValidationException);
        FoodTracksValidationException ex = (FoodTracksValidationException) resultado.getException();
        assertEquals(R.string.valoracion_invalid_puntuation_error_message, ex.getErrorResId());
    }

    @Test
    public void valorarLocal_PuntuacionMenorQue0_5_LanzaExcepcion() {
        ValoracionLocal valoracionMala = new ValoracionLocal();
        valoracionMala.setUidCliente("cliente1");
        valoracionMala.setUidLocal("local1");
        valoracionMala.setPuntuacion(0.0);

        Task<Void> resultado = valoracionService.valorarLocal(valoracionMala);

        assertTrue(resultado.getException() instanceof FoodTracksValidationException);
    }
}
