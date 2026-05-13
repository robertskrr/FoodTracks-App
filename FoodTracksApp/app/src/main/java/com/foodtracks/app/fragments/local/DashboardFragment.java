package com.foodtracks.app.fragments.local;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.foodtracks.app.R;
import com.foodtracks.app.models.UsuarioLocal;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.interfaces.IPublicacionService;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

/**
 * Fragment de dashboard con estadísticas del local.
 *
 * @author Robert
 * @since 13/05
 */
public class DashboardFragment extends Fragment {

    private View rootView;
    private ProgressBar progressBar;
    private NestedScrollView layoutContenido;
    private ShapeableImageView imgPerfil;
    private TextView tvUsername;

    // Tarjetas de estadísticas
    private View cardVisitas, cardPuntuacion, cardTotalValoraciones, cardPostsPropios, cardMenciones;

    private String uidUsuario;
    private IUsuarioService usuarioService;
    private IPublicacionService publicacionService;
    FirebaseAuth mAuth;

    private int tareasCompletadas = 0;
    private final int TOTAL_TAREAS = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dashboard_local, container, false);

        configTheme();
        inicializar();
        cargarEstadisticas();

        return rootView;
    }

    private void inicializar() {
        mAuth = FirebaseAuth.getInstance();
        usuarioService = ServiceFactory.provideUsuarioService(requireContext());
        publicacionService = ServiceFactory.providePublicacionService(requireContext());

        if (mAuth.getCurrentUser() != null) {
            uidUsuario = mAuth.getCurrentUser().getUid();
        }

        progressBar = rootView.findViewById(R.id.progressBarDashboard);
        layoutContenido = rootView.findViewById(R.id.layoutContenidoDashboard);
        imgPerfil = rootView.findViewById(R.id.imgPerfilDashboard);
        tvUsername = rootView.findViewById(R.id.tvUsernameDashboard);

        // Enlazamos las tarjetas de las estadísticas
        cardVisitas = rootView.findViewById(R.id.statVisitas);
        cardPuntuacion = rootView.findViewById(R.id.statPuntuacion);
        cardTotalValoraciones = rootView.findViewById(R.id.statTotalValoraciones);
        cardPostsPropios = rootView.findViewById(R.id.statPostsPropios);
        cardMenciones = rootView.findViewById(R.id.statMenciones);

        // Labels de las estadísticas
        setStatLabel(cardVisitas, getString(R.string.visitas_al_perfil));
        setStatLabel(cardPuntuacion, getString(R.string.puntuacion_media));
        setStatLabel(cardTotalValoraciones, getString(R.string.total_valoraciones));
        setStatLabel(cardPostsPropios, getString(R.string.tus_publicaciones));
        setStatLabel(cardMenciones, getString(R.string.menciones_de_usuarios));
    }

    private void cargarEstadisticas() {
        if (uidUsuario == null){
            return;
        }

        usuarioService.getPerfil(uidUsuario).addOnSuccessListener(usuario -> {
            if (!isAdded()) return;
            if (usuario instanceof UsuarioLocal local) {
                tvUsername.setText("@" + local.getUsername());
                if (local.getFotoPerfil() != null) {
                    Glide.with(this).load(local.getFotoPerfil()).into(imgPerfil);
                } else {
                    imgPerfil.setImageResource(R.drawable.avatar_default);
                }

                setStatValue(cardVisitas, String.valueOf(local.getVisitasPerfil()));
                setStatValue(cardPuntuacion, String.format(Locale.getDefault(), "%.2f", local.getPuntuacionMedia()));
                setStatValue(cardTotalValoraciones, String.valueOf(local.getTotalValoraciones()));
            }
            comprobarCarga();
        });

        // Contador de publicaciones propias
        publicacionService.getPublicacionesByUsuario(uidUsuario).addOnSuccessListener(publicaciones -> {
            if (!isAdded()) return;
            setStatValue(cardPostsPropios, String.valueOf(publicaciones.size()));
            comprobarCarga();
        });

        // Contador de menciones en publicaciones
        publicacionService.getPublicacionesByLocalMencionado(uidUsuario).addOnSuccessListener(menciones -> {
            if (!isAdded()) return;
            setStatValue(cardMenciones, String.valueOf(menciones.size()));
            comprobarCarga();
        }).addOnFailureListener(e -> comprobarCarga());
    }

    /**
     * Aplica la etiqueta correspondiente al textview.
     * @param card Tarjeta de estadística
     * @param label Etiqueta (menciones, visitas, etc)
     */
    private void setStatLabel(View card, String label) {
        TextView tv = card.findViewById(R.id.tvStatLabel);
        tv.setText(label);
    }

    /**
     * Aplica el valor a la estadística correspondiente.
     * @param card Tarjeta de estadística
     * @param value Valor de la estadística
     */
    private void setStatValue(View card, String value) {
        TextView tv = card.findViewById(R.id.tvStatValue);
        tv.setText(value);
    }

    /**
     * Comprueba la carga de los procesos para eliminar la barra de progreso.
     */
    private synchronized void comprobarCarga() {
        tareasCompletadas++;
        if (tareasCompletadas >= TOTAL_TAREAS) {
            progressBar.setVisibility(View.GONE);
            layoutContenido.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Configura la barra de navegación y de estado
     */
    private void configTheme() {
        if (getActivity() != null) {
            getActivity()
                    .getWindow()
                    .setStatusBarColor(
                            androidx.core.content.ContextCompat.getColor(
                                    requireContext(), R.color.fondo));
        }
    }
}