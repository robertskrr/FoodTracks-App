/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.fragments;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodtracks.app.R;
import com.foodtracks.app.adapters.PerfilUsuarioAdapter;
import com.foodtracks.app.models.Usuario;
import com.foodtracks.app.models.UsuarioAdmin;
import com.foodtracks.app.models.UsuarioCliente;
import com.foodtracks.app.models.UsuarioLocal;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.exceptions.FoodTracksNotFoundException;
import com.foodtracks.app.services.interfaces.IUsuarioService;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Fragment de búsqueda de perfiles de usuarios.
 *
 * @author Robert
 * @since 11/05
 */
public class BusquedaFragment extends Fragment {

    private View rootView;
    private TextView tvSinResultados;
    private EditText etBuscador;
    private ProgressBar progressBar;
    private FrameLayout layoutContenido;
    private ConstraintLayout topBarBusqueda;
    private ImageView btnFiltros;

    // Perfiles
    private RecyclerView recyclerPerfiles;
    private PerfilUsuarioAdapter adapter;
    private String uidUsuarioActual;
    private IUsuarioService usuarioService;
    private boolean esAdmin, esCliente, esLocal, esInvitado;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_busqueda, container, false);

        inicializar();
        mostrarInterfazUsuario();

        return rootView;
    }

    private void inicializar() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        usuarioService = ServiceFactory.provideUsuarioService(requireContext());

        if (mAuth.getCurrentUser() != null) {
            uidUsuarioActual = mAuth.getCurrentUser().getUid();
        } else {
            uidUsuarioActual = null;
            esInvitado = true;
        }

        topBarBusqueda = rootView.findViewById(R.id.topBarBusqueda);
        etBuscador = rootView.findViewById(R.id.etBuscador);
        tvSinResultados = rootView.findViewById(R.id.tvSinResultadosBusqueda);
        recyclerPerfiles = rootView.findViewById(R.id.recyclerPerfiles);
        recyclerPerfiles.setLayoutManager(new LinearLayoutManager(requireContext()));

        progressBar = rootView.findViewById(R.id.progressBarBusqueda);
        progressBar.setVisibility(View.GONE);

        layoutContenido = rootView.findViewById(R.id.layoutContenidoBusqueda);
        btnFiltros = rootView.findViewById(R.id.btnFiltros);

        setListeners();
    }

    private void setListeners() {
        // Al pulsar Enter en el teclado
        etBuscador.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        procesoBusquedaInterfaz();
                        return true;
                    }
                    return false;
                });

        // Menú de filtros
        btnFiltros.setOnClickListener(
                v -> {
                    FiltrosBusquedaFragment fm = new FiltrosBusquedaFragment();
                    fm.show(getParentFragmentManager(), "Filtros");
                });

        // Respuesta del menú cuando se cierre
        getParentFragmentManager()
                .setFragmentResultListener(
                        "CLAVE_FILTROS",
                        this,
                        (requestKey, result) -> {
                            String tipoBusqueda = result.getString("TIPO_BUSQUEDA");
                            String ciudad = result.getString("ciudad");

                            layoutContenido.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.VISIBLE);
                            recyclerPerfiles.setVisibility(View.GONE);
                            tvSinResultados.setVisibility(View.GONE);

                            // Borramos el texto del buscador
                            etBuscador.setText("");

                            if ("MANUAL".equalsIgnoreCase(tipoBusqueda)) {
                                boolean vegano = result.getBoolean("vegano");
                                boolean vegetariano = result.getBoolean("vegetariano");
                                boolean lactosa = result.getBoolean("lactosa");
                                boolean celiaco = result.getBoolean("celiaco");
                                String otra = result.getString("otra");

                                ejecutarBusquedaFiltros(
                                        usuarioService.buscarLocalesPorFiltros(
                                                ciudad,
                                                vegano,
                                                vegetariano,
                                                lactosa,
                                                celiaco,
                                                otra));

                            } else if ("MIS_PREFERENCIAS".equalsIgnoreCase(tipoBusqueda)) {
                                if (!esInvitado) {
                                    ejecutarBusquedaFiltros(
                                            usuarioService.buscarLocalesPorMisPreferencias(
                                                    uidUsuarioActual, ciudad));
                                } else {
                                    Toast.makeText(
                                                    requireContext(),
                                                    R.string.inicia_sesion_para_usar_esto,
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
    }

    private void procesoBusquedaInterfaz() {
        String usernameBusqueda = etBuscador.getText().toString().trim();

        if (!usernameBusqueda.isEmpty()) {
            layoutContenido.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            recyclerPerfiles.setVisibility(View.GONE);
            tvSinResultados.setVisibility(View.GONE);

            cargarPerfiles(usernameBusqueda);

            // Oculta el teclado al buscar
            InputMethodManager imm =
                    (InputMethodManager)
                            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etBuscador.getWindowToken(), 0);
        } else {
            layoutContenido.setVisibility(View.GONE);
        }
    }

    private void mostrarInterfazUsuario() {
        if (uidUsuarioActual == null) {
            return;
        }
        usuarioService
                .getPerfil(uidUsuarioActual)
                .addOnSuccessListener(
                        usuario -> {
                            if (!isAdded()) return; // Red de seguridad

                            switch (usuario) {
                                case UsuarioAdmin ignored -> {
                                    esAdmin = true;
                                    configTheme();
                                }
                                case UsuarioCliente ignored -> {
                                    esCliente = true;
                                    configTheme();
                                }
                                case UsuarioLocal ignored -> {
                                    esLocal = true;
                                    configTheme();
                                }
                                default -> {}
                            }
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
                        });
    }

    private void cargarPerfiles(String username) {
        usuarioService
                .buscarUsuarios(username)
                .addOnSuccessListener(
                        usuarios -> {
                            if (!isAdded()) return;
                            progressBar.setVisibility(View.GONE);

                            if (usuarios == null || usuarios.isEmpty()) {
                                tvSinResultados.setVisibility(View.VISIBLE);
                                recyclerPerfiles.setVisibility(View.GONE);
                            } else {
                                tvSinResultados.setVisibility(View.GONE);
                                recyclerPerfiles.setVisibility(View.VISIBLE);
                                adapter = new PerfilUsuarioAdapter(usuarios, requireContext());
                                recyclerPerfiles.setAdapter(adapter);
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            if (!isAdded()) return;
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(
                                            requireContext(),
                                            R.string.publicaciones_loading_error_message,
                                            Toast.LENGTH_SHORT)
                                    .show();
                            Log.e("BusquedaFragment", "Error cargando perfiles: " + e.getMessage());
                        });
    }

    /**
     * Procesa la lista de usuarios devuelta por Firebase
     */
    private void ejecutarBusquedaFiltros(Task<List<Usuario>> taskBusqueda) {
        taskBusqueda
                .addOnSuccessListener(
                        usuarios -> {
                            if (!isAdded()) return;
                            progressBar.setVisibility(View.GONE);

                            if (usuarios == null || usuarios.isEmpty()) {
                                tvSinResultados.setVisibility(View.VISIBLE);
                                recyclerPerfiles.setVisibility(View.GONE);
                            } else {
                                tvSinResultados.setVisibility(View.GONE);
                                recyclerPerfiles.setVisibility(View.VISIBLE);

                                adapter = new PerfilUsuarioAdapter(usuarios, requireContext());
                                recyclerPerfiles.setAdapter(adapter);
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            if (!isAdded()) return;
                            progressBar.setVisibility(View.GONE);
                            if (e instanceof FoodTracksNotFoundException ex) {
                                Toast.makeText(
                                                requireContext(),
                                                ex.getErrorResId(),
                                                Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                Toast.makeText(
                                                requireContext(),
                                                R.string.loading_stores_error_message,
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
    }

    /**
     * Configura la barra de navegación y de estado
     */
    private void configTheme() {
        if (getActivity() != null) {

            if (esAdmin) {
                getActivity()
                        .getWindow()
                        .setStatusBarColor(
                                androidx.core.content.ContextCompat.getColor(
                                        requireContext(), R.color.admin_bottom_nav));
                layoutContenido.setBackgroundColor(getResources().getColor(R.color.black, null));
                topBarBusqueda.setBackgroundColor(
                        getResources().getColor(R.color.admin_bottom_nav, null));
            }

            if (esCliente || esInvitado) {
                getActivity()
                        .getWindow()
                        .setStatusBarColor(
                                androidx.core.content.ContextCompat.getColor(
                                        requireContext(), R.color.fondo));
                // TODO -> Interfaz de cliente/invitado (colores, etc)
            }

            if (esLocal) {
                // TODO -> Interfaz de local (colores, etc)
            }
        }
    }
}
