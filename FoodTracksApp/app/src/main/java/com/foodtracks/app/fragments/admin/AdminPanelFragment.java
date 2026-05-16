/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.fragments.admin;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodtracks.app.R;
import com.foodtracks.app.adapters.RegistroBorradoAdapter;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.interfaces.IPublicacionService;
import com.foodtracks.app.services.interfaces.IUsuarioService;

import com.google.android.material.button.MaterialButtonToggleGroup;

/**
 * Fragment de panel de administrador.
 *
 * @author Robert
 * @since 15/05
 */
public class AdminPanelFragment extends Fragment {

    private View rootView;
    private ProgressBar progressBar;
    private View layoutContenido;
    private TextView tvSinRegistros;

    // Componentes de la lista
    private RecyclerView recyclerRegistros;
    private MaterialButtonToggleGroup toggleGroup;
    private RegistroBorradoAdapter adapter;

    // Memoria caché para las dos listas de registros
    private final List<Object> listaUsuariosBorrados = new ArrayList<>();
    private final List<Object> listaPostsBorrados = new ArrayList<>();

    private IUsuarioService usuarioService;
    private IPublicacionService publicacionService;

    private int tareasCompletadas = 0;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_admin_panel, container, false);

        configTheme();
        inicializar();
        cargarDatosYRegistros();
        configurarToggle();

        return rootView;
    }

    private void inicializar() {
        usuarioService = ServiceFactory.provideUsuarioService(requireContext());
        publicacionService = ServiceFactory.providePublicacionService(requireContext());

        progressBar = rootView.findViewById(R.id.progressBarAdminPanel);
        layoutContenido = rootView.findViewById(R.id.layoutContenidoAdminPanel);
        tvSinRegistros = rootView.findViewById(R.id.tvSinRegistros);

        toggleGroup = rootView.findViewById(R.id.toggleGroupRegistros);
        recyclerRegistros = rootView.findViewById(R.id.recyclerRegistros);
        recyclerRegistros.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void cargarDatosYRegistros() {
        // Descargamos la lista de Usuarios borrados
        usuarioService
                .getAllRegistrosBorradoUsuarios()
                .addOnSuccessListener(
                        lista -> {
                            if (!isAdded()) return;
                            listaUsuariosBorrados.clear();
                            if (lista != null) {
                                listaUsuariosBorrados.addAll(lista);
                            }
                            comprobarCarga();
                        })
                .addOnFailureListener(e -> comprobarCarga());

        // Descargamos la lista de publicaciones borradas
        publicacionService
                .getAllRegistrosBorradoPublicaciones()
                .addOnSuccessListener(
                        lista -> {
                            if (!isAdded()) return;
                            listaPostsBorrados.clear();
                            if (lista != null) {
                                listaPostsBorrados.addAll(lista);
                            }
                            comprobarCarga();
                        })
                .addOnFailureListener(e -> comprobarCarga());
    }

    /**
     * Configura que botón está seleccionado del toggle
     */
    private void configurarToggle() {
        toggleGroup.addOnButtonCheckedListener(
                (group, checkedId, isChecked) -> {
                    if (isChecked) {
                        if (checkedId == R.id.btnVerUsuariosBorrados) {
                            mostrarLista(listaUsuariosBorrados);
                        } else if (checkedId == R.id.btnVerPublicacionesBorradas) {
                            mostrarLista(listaPostsBorrados);
                        }
                    }
                });
    }

    /**
     * Aplica la lista correspondiente al adapter.
     */
    private void mostrarLista(List<Object> listaActiva) {
        if (listaActiva.isEmpty()) {
            tvSinRegistros.setVisibility(View.VISIBLE);
            recyclerRegistros.setVisibility(View.GONE);
        } else {
            tvSinRegistros.setVisibility(View.GONE);
            recyclerRegistros.setVisibility(View.VISIBLE);

            // Creamos tu nuevo adapter pasándole la lista elegida
            adapter = new RegistroBorradoAdapter(listaActiva, requireContext());
            recyclerRegistros.setAdapter(adapter);
        }
    }

    /**
     * Sincroniza la carga de las tareas.
     */
    private synchronized void comprobarCarga() {
        tareasCompletadas++;

        if (tareasCompletadas >= 2) {
            progressBar.setVisibility(View.GONE);
            layoutContenido.setVisibility(View.VISIBLE);

            // Por defecto mostramos la de usuarios borrados
            mostrarLista(listaUsuariosBorrados);
        }
    }

    private void configTheme() {
        if (getActivity() != null) {
            getActivity()
                    .getWindow()
                    .setStatusBarColor(
                            androidx.core.content.ContextCompat.getColor(
                                    requireContext(), R.color.admin_bottom_nav));
        }
    }
}
