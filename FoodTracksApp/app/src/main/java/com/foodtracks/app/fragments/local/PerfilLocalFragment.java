/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.fragments.local;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodtracks.app.R;
import com.foodtracks.app.activities.local.MainLocalActivity;
import com.foodtracks.app.activities.local.PerfilLocalActivity;
import com.foodtracks.app.adapters.PublicacionAdapter;
import com.foodtracks.app.models.UsuarioLocal;
import com.foodtracks.app.models.ValoracionLocal;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.exceptions.FoodTracksValidationException;
import com.foodtracks.app.services.interfaces.IPublicacionService;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.foodtracks.app.services.interfaces.IValoracionLocalService;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

/**
 * Fragment de perfil del cliente.
 * Mismo comportamiento que {@link PerfilLocalActivity} pero se ve desde
 * {@link MainLocalActivity} para mantener la barra de navegación y
 * no saturar el rendimiento de la app.
 * @author Robert
 * @since 08/05
 */
public class PerfilLocalFragment extends Fragment {

    private View rootView;

    private TextView tvNombre,
            tvUsername,
            tvDireccion,
            tvTelefono,
            tvPuntuacion,
            tvSitioWeb,
            tvTituloSitioWeb,
            tvSinPublicaciones;
    private ShapeableImageView imgPerfil;
    private ChipGroup chipGroupOpciones;
    private ProgressBar progressBar;
    private NestedScrollView layoutContenido;

    // Valoración
    private LinearLayout layoutValoracion;
    private RatingBar ratingBarLocal;
    private Button btnEnviarValoracion, btnBorrarValoracion;

    // Publicaciones
    private RecyclerView recyclerPublicaciones;
    private PublicacionAdapter adapter;
    private MaterialButtonToggleGroup toggleGroupPublicaciones;
    private IPublicacionService publicacionService;
    private int tareasCompletadas = 0;

    private String uidLocalVisitado, uidUsuarioActual;
    private IUsuarioService usuarioService;
    private IValoracionLocalService valoracionLocalService;

    // Variables para el mapa
    private MapView mapOsm;
    private double latitudLocal = 0.0;
    private double longitudLocal = 0.0;
    private String nombreLocal = "";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_perfil_local, container, false);

        configTheme();
        inicializar();
        mostrarDatosLocal();
        cargarPublicaciones();
        verificarRolYMostrarValoracion();

        return rootView;
    }

    private void inicializar() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        usuarioService = ServiceFactory.provideUsuarioService(requireContext());
        publicacionService = ServiceFactory.providePublicacionService(requireContext());
        valoracionLocalService = ServiceFactory.provideValoracionLocalService();

        if (mAuth.getCurrentUser() != null) {
            uidUsuarioActual = mAuth.getCurrentUser().getUid();
        } else {
            uidUsuarioActual = null;
        }

        uidLocalVisitado = getUidPerfil(mAuth);

        if (uidLocalVisitado == null) {
            Toast.makeText(
                            requireContext(),
                            R.string.loading_profile_error_message,
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        tvNombre = rootView.findViewById(R.id.tvNombreLocal);
        tvUsername = rootView.findViewById(R.id.tvUsernameLocal);
        tvDireccion = rootView.findViewById(R.id.tvDirLocal);
        tvTelefono = rootView.findViewById(R.id.tvTlfLocal);
        tvPuntuacion = rootView.findViewById(R.id.tvPuntuacionMedia);
        tvSitioWeb = rootView.findViewById(R.id.tvSitioWebLocal);
        tvTituloSitioWeb = rootView.findViewById(R.id.tvTituloSitioWebLocal);
        imgPerfil = rootView.findViewById(R.id.imgPerfilLocal);
        chipGroupOpciones = rootView.findViewById(R.id.chipGroupOpcionesLocal);
        progressBar = rootView.findViewById(R.id.progressBarPerfilLocal);
        layoutContenido = rootView.findViewById(R.id.layoutContenidoPerfilLocal);

        layoutValoracion = rootView.findViewById(R.id.layoutValoracion);
        ratingBarLocal = rootView.findViewById(R.id.ratingBarLocal);
        btnEnviarValoracion = rootView.findViewById(R.id.btnEnviarValoracion);
        btnBorrarValoracion = rootView.findViewById(R.id.btnBorrarValoracion);

        tvSinPublicaciones = rootView.findViewById(R.id.tvSinPublicacionesLocal);
        recyclerPublicaciones = rootView.findViewById(R.id.recyclerPublicacionesLocal);
        recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(requireContext()));

        toggleGroupPublicaciones = rootView.findViewById(R.id.toggleGroupPublicaciones);

        configBotonTipoPublicaciones();
        configMapa();
    }

    private void configBotonTipoPublicaciones() {
        toggleGroupPublicaciones.addOnButtonCheckedListener(
                (group, checkedId, isChecked) -> {
                    if (isChecked) {
                        recyclerPublicaciones.setVisibility(View.GONE);
                        tvSinPublicaciones.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);

                        if (checkedId == R.id.btnVerPublicaciones) {
                            cargarPublicaciones();
                        } else if (checkedId == R.id.btnVerMenciones) {
                            cargarMenciones();
                        }
                    }
                });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configMapa() {
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        mapOsm = rootView.findViewById(R.id.mapOsm);
        mapOsm.setTileSource(TileSourceFactory.MAPNIK);
        mapOsm.setMultiTouchControls(true);
        mapOsm.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        mapOsm.setOnTouchListener(
                (v, event) -> {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                });
    }

    private String getUidPerfil(FirebaseAuth mAuth) {
        if (getArguments() != null && getArguments().containsKey("UID_USUARIO")) {
            String uidOtroUsuarioLocal = getArguments().getString("UID_USUARIO");
            if (uidOtroUsuarioLocal != null && !uidOtroUsuarioLocal.isEmpty()) {
                return uidOtroUsuarioLocal;
            }
        }
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

    private void mostrarDatosLocal() {
        tvPuntuacion.setText("...");

        usuarioService
                .getPerfil(uidLocalVisitado)
                .addOnSuccessListener(
                        usuario -> {
                            if (!isAdded()) return; // Red de seguridad

                            if (usuario instanceof UsuarioLocal local) {
                                tvNombre.setText(local.getNombre());
                                tvUsername.setText("@" + local.getUsername());
                                tvDireccion.setText(
                                        local.getDireccion() + ", " + local.getCiudad());

                                String telefonoLocal = local.getTelefono();
                                tvTelefono.setText(telefonoLocal);

                                if (telefonoLocal != null && !telefonoLocal.trim().isEmpty()) {
                                    tvTelefono.setOnClickListener(
                                            v -> {
                                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                                intent.setData(Uri.parse("tel:" + telefonoLocal));
                                                startActivity(intent);
                                            });
                                }

                                tvPuntuacion.setText(
                                        String.format("%.1f", local.getPuntuacionMedia()));

                                String webLocal = local.getSitioWeb();
                                if (webLocal != null && !webLocal.trim().isEmpty()) {
                                    tvSitioWeb.setText(webLocal);
                                    tvSitioWeb.setTextColor(
                                            getResources().getColor(R.color.tertiary, null));
                                    tvSitioWeb.setOnClickListener(
                                            v -> {
                                                String url = webLocal;
                                                if (!url.startsWith("http://")
                                                        && !url.startsWith("https://")) {
                                                    url = "https://" + url;
                                                }
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse(url));
                                                startActivity(intent);
                                            });
                                } else {
                                    tvTituloSitioWeb.setVisibility(View.GONE);
                                    tvSitioWeb.setVisibility(View.GONE);
                                }

                                if (local.getFotoPerfil() != null) {
                                    Glide.with(requireContext())
                                            .load(local.getFotoPerfil())
                                            .into(imgPerfil);
                                }

                                cargarChipsOpciones(local);

                                latitudLocal = local.getLatitud();
                                longitudLocal = local.getLongitud();
                                nombreLocal = local.getNombre();
                                actualizarChinchetaMapa();

                                comprobarCargaCompleta();
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
                            comprobarCargaCompleta();
                        });
    }

    private void cargarPublicaciones() {
        publicacionService
                .getPublicacionesByUsuario(uidLocalVisitado)
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

    private void cargarMenciones() {
        publicacionService
                .getPublicacionesByLocalMencionado(uidLocalVisitado)
                .addOnSuccessListener(
                        publicaciones -> {
                            if (!isAdded()) return;

                            if (tareasCompletadas >= 2) progressBar.setVisibility(View.GONE);

                            if (publicaciones == null || publicaciones.isEmpty()) {
                                tvSinPublicaciones.setText(R.string.no_hay_menciones_aun);
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
                            if (tareasCompletadas >= 2) progressBar.setVisibility(View.GONE);
                            Toast.makeText(
                                            requireContext(),
                                            R.string.menciones_loading_error_message,
                                            Toast.LENGTH_SHORT)
                                    .show();
                            comprobarCargaCompleta();
                        });
    }

    private synchronized void comprobarCargaCompleta() {
        if (tareasCompletadas < 2) {
            tareasCompletadas++;
        }
        if (tareasCompletadas >= 2) {
            progressBar.setVisibility(View.GONE);
            layoutContenido.setVisibility(View.VISIBLE);
        }
    }

    private void verificarRolYMostrarValoracion() {
        if (uidUsuarioActual == null) return;

        usuarioService
                .getPerfil(uidUsuarioActual)
                .addOnSuccessListener(
                        usuarioActual -> {
                            if (!isAdded()) return;

                            if ("cliente".equals(usuarioActual.getRol())
                                    && !uidUsuarioActual.equals(uidLocalVisitado)) {
                                layoutValoracion.setVisibility(View.VISIBLE);

                                valoracionLocalService
                                        .getValoracionUsuario(uidUsuarioActual, uidLocalVisitado)
                                        .addOnSuccessListener(
                                                valoracion -> {
                                                    if (!isAdded()) return;
                                                    if (valoracion != null) {
                                                        ratingBarLocal.setRating(
                                                                (float) valoracion.getPuntuacion());
                                                        btnBorrarValoracion.setVisibility(
                                                                View.VISIBLE);
                                                    }
                                                })
                                        .addOnFailureListener(
                                                e -> {
                                                    if (!isAdded()) return;
                                                    ratingBarLocal.setRating(0f);
                                                    btnBorrarValoracion.setVisibility(View.GONE);
                                                });

                                btnEnviarValoracion.setOnClickListener(
                                        v -> {
                                            btnEnviarValoracion.setEnabled(false);
                                            float puntuacionElegida = ratingBarLocal.getRating();

                                            ValoracionLocal nuevaValoracion =
                                                    ValoracionLocal.builder()
                                                            .uidCliente(uidUsuarioActual)
                                                            .uidLocal(uidLocalVisitado)
                                                            .puntuacion(puntuacionElegida)
                                                            .build();

                                            valoracionLocalService
                                                    .valorarLocal(nuevaValoracion)
                                                    .addOnSuccessListener(
                                                            unused -> {
                                                                if (!isAdded()) return;
                                                                Toast.makeText(
                                                                                requireContext(),
                                                                                R.string
                                                                                        .valoracion_enviada,
                                                                                Toast.LENGTH_SHORT)
                                                                        .show();
                                                                mostrarDatosLocal();
                                                                btnBorrarValoracion.setVisibility(
                                                                        View.VISIBLE);
                                                                btnEnviarValoracion.setEnabled(
                                                                        true);
                                                            })
                                                    .addOnFailureListener(
                                                            e -> {
                                                                if (!isAdded()) return;
                                                                if (e
                                                                        instanceof
                                                                        FoodTracksValidationException
                                                                        ex) {
                                                                    Toast.makeText(
                                                                                    requireContext(),
                                                                                    ex
                                                                                            .getErrorResId(),
                                                                                    Toast
                                                                                            .LENGTH_SHORT)
                                                                            .show();
                                                                } else {
                                                                    Toast.makeText(
                                                                                    requireContext(),
                                                                                    getString(
                                                                                                    R
                                                                                                            .string
                                                                                                            .send_valoracion_error_mensaje)
                                                                                            + e
                                                                                                    .getMessage(),
                                                                                    Toast
                                                                                            .LENGTH_SHORT)
                                                                            .show();
                                                                }
                                                                btnEnviarValoracion.setEnabled(
                                                                        true);
                                                            });
                                        });

                                btnBorrarValoracion.setOnClickListener(
                                        v -> {
                                            btnBorrarValoracion.setEnabled(false);
                                            valoracionLocalService
                                                    .eliminarValoracion(
                                                            uidUsuarioActual, uidLocalVisitado)
                                                    .addOnSuccessListener(
                                                            unused -> {
                                                                if (!isAdded()) return;
                                                                Toast.makeText(
                                                                                requireContext(),
                                                                                R.string
                                                                                        .valoracion_eliminada,
                                                                                Toast.LENGTH_SHORT)
                                                                        .show();
                                                                ratingBarLocal.setRating(0f);
                                                                btnBorrarValoracion.setVisibility(
                                                                        View.GONE);
                                                                mostrarDatosLocal();
                                                                btnBorrarValoracion.setEnabled(
                                                                        true);
                                                            })
                                                    .addOnFailureListener(
                                                            e -> {
                                                                if (!isAdded()) return;
                                                                Toast.makeText(
                                                                                requireContext(),
                                                                                getString(
                                                                                                R
                                                                                                        .string
                                                                                                        .delete_valoracion_error_mensaje)
                                                                                        + e
                                                                                                .getMessage(),
                                                                                Toast.LENGTH_SHORT)
                                                                        .show();
                                                                btnBorrarValoracion.setEnabled(
                                                                        true);
                                                            });
                                        });
                            }
                        });
    }

    private void actualizarChinchetaMapa() {
        if (mapOsm != null && latitudLocal != 0.0 && longitudLocal != 0.0) {
            GeoPoint startPoint = new GeoPoint(latitudLocal, longitudLocal);
            mapOsm.getController().setZoom(17.0);
            mapOsm.getController().setCenter(startPoint);

            Marker startMarker = new Marker(mapOsm);
            startMarker.setPosition(startPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setTitle(nombreLocal);

            mapOsm.getOverlays().clear();
            mapOsm.getOverlays().add(startMarker);
            mapOsm.invalidate();
        }
    }

    private void cargarChipsOpciones(UsuarioLocal local) {
        chipGroupOpciones.removeAllViews();

        if (local.isEsVegano()) addChip("\uD83C\uDF31 " + getString(R.string.vegano));
        if (local.isEsVegetariano()) addChip("\uD83C\uDF3F " + getString(R.string.vegetariano));
        if (local.isSinLactosa()) addChip("\uD83E\uDD5B " + getString(R.string.sin_lactosa));
        if (local.isEsCeliaco()) addChip("\uD83C\uDF3E " + getString(R.string.celiaco));

        if (local.getOtraPreferencia() instanceof String otra) {
            addChip("\uD83D\uDCDD " + otra);
        }

        if (chipGroupOpciones.getChildCount() == 0) {
            addChip(getString(R.string.sin_preferencias));
        }
    }

    private void addChip(String texto) {
        Chip chip = new Chip(requireContext());
        chip.setText(texto);
        chip.setCheckable(false);
        chip.setClickable(false);
        chip.setChipBackgroundColorResource(R.color.tertiary);
        chip.setTextColor(getResources().getColor(R.color.white, null));
        chip.setChipStrokeColorResource(R.color.tertiary);

        chipGroupOpciones.addView(chip);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapOsm != null) mapOsm.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapOsm != null) mapOsm.onPause();
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
                                    requireContext(), R.color.tertiary));
        }
    }
}
