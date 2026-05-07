/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.activities.local;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodtracks.app.R;
import com.foodtracks.app.adapters.PublicacionAdapter;
import com.foodtracks.app.models.UsuarioLocal;
import com.foodtracks.app.models.ValoracionLocal;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.interfaces.IPublicacionService;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.foodtracks.app.services.interfaces.IValoracionLocalService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * @author Robert
 * @since 18/02
 */
public class PerfilLocalActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView tvNombre, tvUsername, tvDireccion, tvTelefono, tvPuntuacion, tvSitioWeb, tvSinPublicaciones;
    private ShapeableImageView imgPerfil;
    private ChipGroup chipGroupOpciones;
    private ProgressBar progressBar;
    private NestedScrollView layoutContenido;

    // Valoración
    private LinearLayout layoutValoracion;
    private RatingBar ratingBarLocal;
    private Button btnEnviarValoracion;

    // Publicaciones
    private RecyclerView recyclerPublicaciones;
    private PublicacionAdapter adapter;
    private IPublicacionService publicacionService;
    private int tareasCompletadas = 0; // Contador de tareas para la carga

    private String uidLocalVisitado;
    private String uidUsuarioActual;
    private IUsuarioService usuarioService;
    private IValoracionLocalService valoracionLocalService; // NUEVO SERVICIO

    // Variables para el mapa
    private GoogleMap mMap;
    private double latitudLocal = 0.0;
    private double longitudLocal = 0.0;
    private String nombreLocal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_local);

        configTheme();
        inicializar();
        mostrarDatosLocal();
        cargarPublicaciones();
        if (uidUsuarioActual !=null){
            verificarRolYMostrarValoracion();
        }
    }

    private void inicializar() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        usuarioService = ServiceFactory.provideUsuarioService(this);
        publicacionService = ServiceFactory.providePublicacionService(this);
        valoracionLocalService = ServiceFactory.provideValoracionLocalService();

        if (mAuth.getCurrentUser() != null){
            uidUsuarioActual = mAuth.getCurrentUser().getUid();
        }

        // Verificamos de quién es el perfil
        String uidOtroUsuario = getIntent().getStringExtra("UID_USUARIO");
        if (uidOtroUsuario != null && !uidOtroUsuario.isEmpty()) {
            uidLocalVisitado = uidOtroUsuario;
        } else {
            uidLocalVisitado = uidUsuarioActual;
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

        // Publicaciones
        tvSinPublicaciones = findViewById(R.id.tvSinPublicacionesLocal);
        recyclerPublicaciones = findViewById(R.id.recyclerPublicacionesLocal);
        recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(this));

        // Mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapLocal);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void mostrarDatosLocal() {
        // Reiniciamos temporalmente los textos para que se note la actualización en tiempo real
        tvPuntuacion.setText("...");

        usuarioService.getPerfil(uidLocalVisitado)
                .addOnSuccessListener(usuario -> {
                    if (usuario instanceof UsuarioLocal local) {
                        tvNombre.setText(local.getNombre());
                        tvUsername.setText("@" + local.getUsername());
                        tvDireccion.setText(local.getDireccion() + ", " + local.getCiudad());
                        tvTelefono.setText(local.getTelefono());

                        // Refrescamos la nota media aquí
                        tvPuntuacion.setText(String.format("%.1f", local.getPuntuacionMedia()));

                        if (local.getSitioWeb() != null && !local.getSitioWeb().isEmpty()) {
                            tvSitioWeb.setText(local.getSitioWeb());
                        } else {
                            tvSitioWeb.setText("No disponible");
                            tvSitioWeb.setTextColor(getResources().getColor(R.color.black, null));
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
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar el perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    comprobarCargaCompleta();
                });
    }

    /** Carga las publicaciones publicadas por este local */
    private void cargarPublicaciones() {
        publicacionService.getPublicacionesByUsuario(uidLocalVisitado)
                .addOnSuccessListener(publicaciones -> {
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
                .addOnFailureListener(e -> {
                    Toast.makeText(this, getString(R.string.publicaciones_loading_error_message) + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    /** Muestra el bloque de estrellas y gestiona el envío de la valoración a la base de datos */
    private void verificarRolYMostrarValoracion() {
        usuarioService.getPerfil(uidUsuarioActual)
                .addOnSuccessListener(usuarioActual -> {
                    // Si es un cliente y no está viendo su propio perfil, le dejamos valorar
                    if ("cliente".equals(usuarioActual.getRol()) && !uidUsuarioActual.equals(uidLocalVisitado)) {
                        layoutValoracion.setVisibility(View.VISIBLE);

                        // Recuperamos si ya había votado antes para pintar las estrellas
                        valoracionLocalService.getValoracionUsuario(uidUsuarioActual, uidLocalVisitado)
                                .addOnSuccessListener(valoracion -> {
                                    if (valoracion != null) {
                                        ratingBarLocal.setRating((float) valoracion.getPuntuacion());
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Asumimos que no ha votado todavía y se queda en 0
                                    ratingBarLocal.setRating(0f);
                                });

                        // Botón de enviar (Lógica limpia delegada al servicio)
                        btnEnviarValoracion.setOnClickListener(v -> {
                            // Evitamos doble click
                            btnEnviarValoracion.setEnabled(false);

                            float puntuacionElegida = ratingBarLocal.getRating();

                            // Construimos el objeto y dejamos que el Servicio decida si es válido
                            ValoracionLocal nuevaValoracion = ValoracionLocal.builder()
                                    .uidCliente(uidUsuarioActual)
                                    .uidLocal(uidLocalVisitado)
                                    .puntuacion(puntuacionElegida)
                                    .build();

                            // Guarda y actualiza la media
                            valoracionLocalService.valorarLocal(nuevaValoracion)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "¡Valoración enviada!", Toast.LENGTH_SHORT).show();
                                        // Recargamos el perfil
                                        mostrarDatosLocal();
                                        btnEnviarValoracion.setEnabled(true);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Capturamos tu excepción de validación personalizada igual que en Register
                                        if (e instanceof com.foodtracks.app.services.exceptions.FoodTracksValidationException ex) {
                                            Toast.makeText(this, ex.getErrorResId(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(this, "Error al enviar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                        btnEnviarValoracion.setEnabled(true);
                                    });
                        });
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        actualizarChinchetaMapa();
    }

    private void actualizarChinchetaMapa() {
        if (mMap != null && latitudLocal != 0.0 && longitudLocal != 0.0) {
            LatLng ubicacion = new LatLng(latitudLocal, longitudLocal);
            mMap.addMarker(new MarkerOptions().position(ubicacion).title(nombreLocal));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15f));
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
        chip.setChipBackgroundColorResource(R.color.tertiary);
        chip.setTextColor(getResources().getColor(R.color.white, null));
        chipGroupOpciones.addView(chip);
    }

    private void configTheme() {
        getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.fondo));
        getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.primary));
    }
}