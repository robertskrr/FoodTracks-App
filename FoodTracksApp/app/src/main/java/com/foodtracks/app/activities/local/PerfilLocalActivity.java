/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.activities.local;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodtracks.app.R;
import com.foodtracks.app.adapters.PublicacionAdapter;
import com.foodtracks.app.models.UsuarioLocal;
import com.foodtracks.app.models.ValoracionLocal;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.exceptions.FoodTracksValidationException;
import com.foodtracks.app.services.interfaces.IPublicacionService;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.foodtracks.app.services.interfaces.IValoracionLocalService;

import com.bumptech.glide.Glide;
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
 * @author Robert
 * @since 18/02
 */
public class PerfilLocalActivity extends AppCompatActivity {

    private TextView tvNombre,
            tvUsername,
            tvDireccion,
            tvTelefono,
            tvPuntuacion,
            tvSitioWeb,
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
    private IPublicacionService publicacionService;
    private int tareasCompletadas = 0; // Contador de tareas para la carga

    private String uidLocalVisitado, uidUsuarioActual;
    private IUsuarioService usuarioService;
    private IValoracionLocalService valoracionLocalService;

    // Variables para el mapa
    private MapView mapOsm;
    private double latitudLocal = 0.0;
    private double longitudLocal = 0.0;
    private String nombreLocal = "";

    private boolean esInvitado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_perfil_local);

        configTheme();
        inicializar();
        mostrarDatosLocal();
        cargarPublicaciones();
        accionesUsuarioVisitante();
    }

    private void inicializar() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        usuarioService = ServiceFactory.provideUsuarioService(this);
        publicacionService = ServiceFactory.providePublicacionService(this);
        valoracionLocalService = ServiceFactory.provideValoracionLocalService();

        // El usuario actual puede ser null si es invitado
        if (mAuth.getCurrentUser() != null) {
            uidUsuarioActual = mAuth.getCurrentUser().getUid();
        } else {
            uidUsuarioActual = null;
            esInvitado = true;
        }

        uidLocalVisitado = getUidPerfil(mAuth);

        if (uidLocalVisitado == null) {
            Toast.makeText(this, R.string.loading_profile_error_message, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvNombre = findViewById(R.id.tvNombreLocal);
        tvUsername = findViewById(R.id.tvUsernameLocal);
        tvDireccion = findViewById(R.id.tvDirLocal);
        tvTelefono = findViewById(R.id.tvTlfLocal);
        tvPuntuacion = findViewById(R.id.tvPuntuacionMedia);
        tvSitioWeb = findViewById(R.id.tvSitioWebLocal);
        imgPerfil = findViewById(R.id.imgPerfilLocal);
        chipGroupOpciones = findViewById(R.id.chipGroupOpcionesLocal);
        progressBar = findViewById(R.id.progressBarPerfilLocal);
        layoutContenido = findViewById(R.id.layoutContenidoPerfilLocal);

        layoutValoracion = findViewById(R.id.layoutValoracion);
        ratingBarLocal = findViewById(R.id.ratingBarLocal);
        btnEnviarValoracion = findViewById(R.id.btnEnviarValoracion);
        btnBorrarValoracion = findViewById(R.id.btnBorrarValoracion);

        // Publicaciones
        tvSinPublicaciones = findViewById(R.id.tvSinPublicacionesLocal);
        recyclerPublicaciones = findViewById(R.id.recyclerPublicacionesLocal);
        recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(this));

        // Mapa
        configMapa();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configMapa() {
        Configuration.getInstance().setUserAgentValue(getPackageName());

        mapOsm = findViewById(R.id.mapOsm);
        mapOsm.setTileSource(TileSourceFactory.MAPNIK); // Estilo de mapa estándar
        mapOsm.setMultiTouchControls(true); // Permitir zoom con dos dedos
        mapOsm.getZoomController()
                .setVisibility(
                        CustomZoomButtonsController.Visibility
                                .NEVER); // Elimina los botones de zoom

        // Evita que el NestedScrollView interrumpa el gesto de desplazamiento del mapa
        mapOsm.setOnTouchListener(
                (v, event) -> {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                });
    }

    private String getUidPerfil(FirebaseAuth mAuth) {
        String uidOtroUsuarioLocal = getIntent().getStringExtra("UID_USUARIO");

        if (uidOtroUsuarioLocal != null && !uidOtroUsuarioLocal.isEmpty()) {
            return uidOtroUsuarioLocal;
        }

        // Si no hay Intent, comprobamos si es su propio perfil
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }

        // Este caso es el de un invitado que ha entrado a un perfil
        return null;
    }

    private void mostrarDatosLocal() {
        // Reiniciamos temporalmente los textos para que se note la actualización en tiempo real
        tvPuntuacion.setText("...");

        usuarioService
                .getPerfil(uidLocalVisitado)
                .addOnSuccessListener(
                        usuario -> {
                            if (usuario instanceof UsuarioLocal local) {
                                tvNombre.setText(local.getNombre());
                                tvUsername.setText("@" + local.getUsername());
                                tvDireccion.setText(
                                        local.getDireccion() + ", " + local.getCiudad());
                                String telefonoLocal = local.getTelefono();
                                // Configuración teléfono clickable
                                tvTelefono.setText(telefonoLocal);

                                if (telefonoLocal != null && !telefonoLocal.trim().isEmpty()) {
                                    tvTelefono.setOnClickListener(
                                            v -> {
                                                Intent intent =
                                                        new android.content.Intent(
                                                                android.content.Intent.ACTION_DIAL);
                                                intent.setData(
                                                        Uri.parse("tel:" + telefonoLocal)); // Abre
                                                // app de
                                                // teléfono con el número
                                                startActivity(intent);
                                            });
                                }

                                // Refrescamos la nota media aquí
                                tvPuntuacion.setText(
                                        String.format("%.1f", local.getPuntuacionMedia()));

                                // Configuración sitio web clickable
                                String webLocal = local.getSitioWeb();
                                if (webLocal != null && !webLocal.trim().isEmpty()) {
                                    tvSitioWeb.setText(webLocal);
                                    tvSitioWeb.setTextColor(
                                            getResources().getColor(R.color.tertiary, null));

                                    tvSitioWeb.setOnClickListener(
                                            v -> {
                                                String url = webLocal;
                                                // Asegurarnos prefijo correcto para que el
                                                // navegador lo entienda
                                                if (!url.startsWith("http://")
                                                        && !url.startsWith("https://")) {
                                                    url = "https://" + url;
                                                }
                                                Intent intent =
                                                        new Intent(
                                                                android.content.Intent.ACTION_VIEW);
                                                intent.setData(
                                                        android.net.Uri.parse(
                                                                url)); // Abre el navegador con la
                                                // página del local
                                                startActivity(intent);
                                            });
                                } else {
                                    tvSitioWeb.setText(R.string.no_disponible);
                                    tvSitioWeb.setTextColor(
                                            getResources().getColor(R.color.black, null));
                                    tvSitioWeb.setOnClickListener(
                                            null); // Desactivamos el click si no hay web
                                }

                                if (local.getFotoPerfil() != null) {
                                    Glide.with(this).load(local.getFotoPerfil()).into(imgPerfil);
                                }

                                cargarChipsOpciones(local);

                                // Guardar coordenadas y actualizar el mapa si ya cargó
                                latitudLocal = local.getLatitud();
                                longitudLocal = local.getLongitud();
                                nombreLocal = local.getNombre();
                                actualizarChinchetaMapa();

                                comprobarCargaCompleta();
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            Toast.makeText(
                                            this,
                                            getString(R.string.loading_profile_error_message)
                                                    + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                            comprobarCargaCompleta();
                        });
    }

    /** Carga las publicaciones publicadas por este local */
    private void cargarPublicaciones() {
        publicacionService
                .getPublicacionesByUsuario(uidLocalVisitado)
                .addOnSuccessListener(
                        publicaciones -> {
                            if (publicaciones == null || publicaciones.isEmpty()) {
                                tvSinPublicaciones.setVisibility(View.VISIBLE);
                                recyclerPublicaciones.setVisibility(View.GONE);
                            } else {
                                tvSinPublicaciones.setVisibility(View.GONE);
                                recyclerPublicaciones.setVisibility(View.VISIBLE);

                                adapter = new PublicacionAdapter(publicaciones, this);
                                recyclerPublicaciones.setAdapter(adapter);
                            }
                            comprobarCargaCompleta();
                        })
                .addOnFailureListener(
                        e -> {
                            Toast.makeText(
                                            this,
                                            getString(R.string.publicaciones_loading_error_message)
                                                    + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                            comprobarCargaCompleta();
                        });
    }

    /** Sincroniza las tareas de red para mostrar la pantalla solo cuando todo esté listo */
    private synchronized void comprobarCargaCompleta() {
        tareasCompletadas++;

        if (tareasCompletadas >= 2) {
            progressBar.setVisibility(View.GONE);
            layoutContenido.setVisibility(View.VISIBLE);
        }
    }

    // TODO -> Añadir sonido al valorar cuando se pruebe desde un cliente
    /** Muestra el bloque de estrellas, gestiona el envío/borrado de la valoración si es cliente
     * e incrementa las visitas del local
     */
    private void accionesUsuarioVisitante() {
        // Incrementa las visitas del perfil del local
        usuarioService.registrarVisitaPerfil(uidUsuarioActual, uidLocalVisitado);

        // Si no hay usuario logueado (invitado) no hacemos nada más
        if (esInvitado) {
            return;
        }

        usuarioService
                .getPerfil(uidUsuarioActual)
                .addOnSuccessListener(
                        usuarioActual -> {
                            // Si es un cliente y no está viendo su propio perfil, le dejamos
                            // valorar
                            if ("cliente".equals(usuarioActual.getRol())
                                    && !uidUsuarioActual.equals(uidLocalVisitado)) {
                                layoutValoracion.setVisibility(View.VISIBLE);
                                // Recuperamos si ya había votado antes
                                valoracionLocalService
                                        .getValoracionUsuario(uidUsuarioActual, uidLocalVisitado)
                                        .addOnSuccessListener(
                                                valoracion -> {
                                                    if (valoracion != null) {
                                                        // Pinta las estrellas y muestra el botón de
                                                        // borrar
                                                        ratingBarLocal.setRating(
                                                                (float) valoracion.getPuntuacion());
                                                        btnBorrarValoracion.setVisibility(
                                                                View.VISIBLE);
                                                    }
                                                })
                                        .addOnFailureListener(
                                                e -> {
                                                    // Asumimos que no ha votado todavía
                                                    ratingBarLocal.setRating(0f);
                                                    btnBorrarValoracion.setVisibility(View.GONE);
                                                });

                                // Botón de enviar valoración
                                btnEnviarValoracion.setOnClickListener(
                                        v -> {
                                            btnEnviarValoracion.setEnabled(
                                                    false); // Evitamos el doble click rápido
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
                                                                Toast.makeText(
                                                                                this,
                                                                                R.string
                                                                                        .valoracion_enviada,
                                                                                Toast.LENGTH_SHORT)
                                                                        .show();
                                                                mostrarDatosLocal(); // Recarga la
                                                                // media en
                                                                // tiempo real
                                                                btnBorrarValoracion.setVisibility(
                                                                        View
                                                                                .VISIBLE); // Mostramos el botón de borrar
                                                                btnEnviarValoracion.setEnabled(
                                                                        true);
                                                            })
                                                    .addOnFailureListener(
                                                            e -> {
                                                                if (e
                                                                        instanceof
                                                                        FoodTracksValidationException
                                                                        ex) {
                                                                    Toast.makeText(
                                                                                    this,
                                                                                    ex
                                                                                            .getErrorResId(),
                                                                                    Toast
                                                                                            .LENGTH_SHORT)
                                                                            .show();
                                                                } else {
                                                                    Toast.makeText(
                                                                                    this,
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

                                // Botón de borrar valoración
                                btnBorrarValoracion.setOnClickListener(
                                        v -> {
                                            btnBorrarValoracion.setEnabled(false);

                                            valoracionLocalService
                                                    .eliminarValoracion(
                                                            uidUsuarioActual, uidLocalVisitado)
                                                    .addOnSuccessListener(
                                                            unused -> {
                                                                Toast.makeText(
                                                                                this,
                                                                                R.string
                                                                                        .valoracion_eliminada,
                                                                                Toast.LENGTH_SHORT)
                                                                        .show();
                                                                ratingBarLocal.setRating(
                                                                        0f); // Reseteamos las
                                                                // estrellas
                                                                btnBorrarValoracion.setVisibility(
                                                                        View.GONE); // Ocultamos
                                                                // el botón
                                                                mostrarDatosLocal(); // Recarga la
                                                                // media sin
                                                                // este voto
                                                                btnBorrarValoracion.setEnabled(
                                                                        true);
                                                            })
                                                    .addOnFailureListener(
                                                            e -> {
                                                                Toast.makeText(
                                                                                this,
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

            // Centrar el mapa
            mapOsm.getController().setZoom(17.0);
            mapOsm.getController().setCenter(startPoint);

            // Añadir el marcador
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
        Chip chip = new Chip(this);
        chip.setText(texto);
        chip.setCheckable(false);
        chip.setClickable(false);
        chip.setChipBackgroundColorResource(R.color.primary);
        chip.setTextColor(getResources().getColor(R.color.black, null));
        chipGroupOpciones.addView(chip);
    }

    private void configTheme() {
        getWindow()
                .setStatusBarColor(
                        androidx.core.content.ContextCompat.getColor(this, R.color.tertiary));
        getWindow()
                .setNavigationBarColor(
                        androidx.core.content.ContextCompat.getColor(this, R.color.secondary));
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
}
