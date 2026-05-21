package com.foodtracks.app.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.foodtracks.app.R;
import com.foodtracks.app.repositories.interfaces.ILikeRepository;
import com.foodtracks.app.repositories.interfaces.IPublicacionRepository;
import com.foodtracks.app.services.LikeService;
import com.foodtracks.app.services.exceptions.FoodTracksNotFoundException;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LikeServiceTest {

    @Mock
    private ILikeRepository mockLikeRepository;

    @Mock
    private IPublicacionRepository mockPublicacionRepository;

    private LikeService likeService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        likeService = new LikeService(mockLikeRepository, mockPublicacionRepository);
    }

    @Test
    public void eliminarLike_LikeNoExiste_LanzaExcepcion() {
        // Simulamos que al buscar el like en Firebase, el documento no existe
        DocumentSnapshot mockDoc = mock(DocumentSnapshot.class);
        when(mockDoc.exists()).thenReturn(false);
        when(mockLikeRepository.getLike(anyString())).thenReturn(Tasks.forResult(mockDoc));

        Task<Void> resultado = likeService.eliminarLike("user1", "pub1");

        assertTrue(resultado.getException() instanceof FoodTracksNotFoundException);
        FoodTracksNotFoundException ex = (FoodTracksNotFoundException) resultado.getException();
        assertEquals(R.string.like_not_found_error_message, ex.getErrorResId());
    }
}