/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodtracks.app.R;
import com.foodtracks.app.activities.admin.MainAdminActivity;
import com.foodtracks.app.activities.cliente.MainClienteActivity;
import com.foodtracks.app.activities.local.MainLocalActivity;
import com.foodtracks.app.adapters.PublicacionAdapter;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.interfaces.IPublicacionService;
import com.foodtracks.app.services.interfaces.IUsuarioService;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Fragment de feed de todas las publicaciones de los usuarios.
 *
 * @author Robert
 * @since 09/05
 */
public class FeedFragment extends Fragment {

    private View rootView;
    private TextView tvUsername, tvSinPublicaciones;
    private ProgressBar progressBar;
    private FrameLayout layoutContenido;
    private ConstraintLayout topBarFeed;

    // Publicaciones
    private RecyclerView recyclerPublicaciones;
    private PublicacionAdapter adapter;
    private IPublicacionService publicacionService;
    private int tareasCompletadas = 0;

    private String uidUsuarioActual;
    private IUsuarioService usuarioService;
    private boolean esAdmin, esCliente, esLocal, esInvitado;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_feed, container, false);

        inicializar();
        mostrarDatosUsuario();
        cargarPublicaciones();

        return rootView;
    }

    private void inicializar() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        usuarioService = ServiceFactory.provideUsuarioService(requireContext());
        publicacionService = ServiceFactory.providePublicacionService(requireContext());

        if (mAuth.getCurrentUser() != null) {
            uidUsuarioActual = mAuth.getCurrentUser().getUid();
        } else {
            uidUsuarioActual = null;
            esInvitado = true;
        }

        tvUsername = rootView.findViewById(R.id.tvUsernameFeed);
        progressBar = rootView.findViewById(R.id.progressBarFeed);
        layoutContenido = rootView.findViewById(R.id.layoutContenidoFeed);
        topBarFeed = rootView.findViewById(R.id.topBarFeed);

        tvSinPublicaciones = rootView.findViewById(R.id.tvSinPublicacionesFeed);
        recyclerPublicaciones = rootView.findViewById(R.id.recyclerPublicacionesFeed);
        recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (getActivity() instanceof MainLocalActivity) {
            esLocal = true;
        } else if (getActivity() instanceof MainClienteActivity) {
            if (!esInvitado) {
                esCliente = true;
            }
        } else if (getActivity() instanceof MainAdminActivity) {
            esAdmin = true;
        }

        configTheme();
    }

    private void mostrarDatosUsuario() {
        if (esInvitado) {
            comprobarCargaCompleta();
            return;
        }

        usuarioService
                .getPerfil(uidUsuarioActual)
                .addOnSuccessListener(
                        usuario -> {
                            if (!isAdded()) return; // Red de seguridad

                            tvUsername.setText("@" + usuario.getUsername());
                            comprobarCargaCompleta();
                        })
                .addOnFailureListener(
                        e -> {
                            if (!isAdded()) return; // Red de seguridad
                            Toast.makeText(
                                            requireContext(),
                                            getString(R.string.loading_profile_error_message)
                                                    + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                            comprobarCargaCompleta();
                        });
    }

    private void cargarPublicaciones() {
        publicacionService
                .getAllPublicaciones()
                .addOnSuccessListener(
                        publicaciones -> {
                            if (!isAdded()) return;
                            if (publicaciones == null || publicaciones.isEmpty()) {
                                tvSinPublicaciones.setVisibility(View.VISIBLE);
                                recyclerPublicaciones.setVisibility(View.GONE);
                            } else {
                                tvSinPublicaciones.setVisibility(View.GONE);
                                recyclerPublicaciones.setVisibility(View.VISIBLE);
                                adapter = new PublicacionAdapter(publicaciones, requireContext());
                                recyclerPublicaciones.setAdapter(adapter);
                            }
                            comprobarCargaCompleta();
                        })
                .addOnFailureListener(
                        e -> {
                            if (!isAdded()) return;
                            Toast.makeText(
                                            requireContext(),
                                            getString(R.string.publicaciones_loading_error_message)
                                                    + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                            comprobarCargaCompleta();
                        });
    }

    private synchronized void comprobarCargaCompleta() {
        tareasCompletadas++;
        if (tareasCompletadas >= 2) {
            progressBar.setVisibility(View.GONE);
            layoutContenido.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Configura la barra de navegación y de estado
     */
    private void configTheme() {
        if (getActivity() != null) {

            if (esAdmin) {
                // Tema Administrador
                getActivity()
                        .getWindow()
                        .setStatusBarColor(
                                ContextCompat.getColor(requireContext(), R.color.admin_bottom_nav));
                topBarFeed.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.admin_bottom_nav));
                tvUsername.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            } else if (esCliente || esInvitado) {
                // Tema Cliente o Invitado
                getActivity()
                        .getWindow()
                        .setStatusBarColor(
                                ContextCompat.getColor(
                                        requireContext(), R.color.secondary_perfil_cliente));

                topBarFeed.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.secondary_perfil_cliente));

                if (esInvitado) {
                    tvUsername.setText(R.string.feed);
                }
            } else if (esLocal) {
                // Tema Local
                getActivity()
                        .getWindow()
                        .setStatusBarColor(
                                ContextCompat.getColor(requireContext(), R.color.tertiary));
                topBarFeed.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.tertiary));
                tvUsername.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            }
        }
    }
}
