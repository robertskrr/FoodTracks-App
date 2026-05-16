/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.activities.cliente;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodtracks.app.R;
import com.foodtracks.app.adapters.PublicacionAdapter;
import com.foodtracks.app.models.Usuario;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.exceptions.FoodTracksNotFoundException;
import com.foodtracks.app.services.exceptions.FoodTracksValidationException;
import com.foodtracks.app.services.interfaces.IPublicacionService;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.foodtracks.app.utils.DateUtils;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * @author Robert
 * @since 18/02
 */
public class PerfilClienteActivity extends AppCompatActivity {

    private TextView tvNombre, tvUsername, tvCiudad, tvFechaRegistro, tvSinPublicaciones;
    private ShapeableImageView imgPerfil;
    private ChipGroup chipGroupPreferencias;
    private RecyclerView recyclerPublicaciones;
    private String uidCliente;
    private FirebaseAuth mAuth;
    private ImageView imgEliminarPerfilAdmin;
    private String uidUsuarioActual;
    private boolean esInvitado;
    private IUsuarioService usuarioService;
    private IPublicacionService publicacionService;
    private PublicacionAdapter adapter;
    private ProgressBar progressBar;
    private NestedScrollView layoutContenido;
    private int tareasCompletadas = 0; // Contador de tareas de Firebase para la pantalla de carga
    private final String MOTIVO_DEFAULT = "Incumplimiento de las normas de la comunidad.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_perfil_cliente);

        configTheme();
        inicializar();
        mostrarDatosCliente();
        cargarPublicaciones();
    }

    /**
     * Asigna los componentes a la interfaz
     */
    private void inicializar() {
        mAuth = FirebaseAuth.getInstance();
        uidCliente = getUidPerfil(mAuth);

        usuarioService = ServiceFactory.provideUsuarioService(this);
        publicacionService = ServiceFactory.providePublicacionService(this);

        // TextView
        tvNombre = findViewById(R.id.tvNombreCliente);
        tvUsername = findViewById(R.id.tvUsernameCliente);
        tvCiudad = findViewById(R.id.tvCiudadCliente);
        tvFechaRegistro = findViewById(R.id.tvFechaRegistroCliente);

        tvSinPublicaciones = findViewById(R.id.tvSinPublicacionesFeed);

        // Foto perfil
        imgPerfil = findViewById(R.id.imgPerfilCliente);

        // Preferencias
        chipGroupPreferencias = findViewById(R.id.chipGroupPreferencias);

        // Publicaciones
        recyclerPublicaciones = findViewById(R.id.recyclerPublicacionesCliente);
        recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(this));

        // Pantalla de carga
        progressBar = findViewById(R.id.progressBarFeed);
        layoutContenido = findViewById(R.id.layoutContenidoPerfil);

        imgEliminarPerfilAdmin = findViewById(R.id.imgEliminarPerfilAdminCliente);

        if (mAuth.getCurrentUser() != null) {
            uidUsuarioActual = mAuth.getCurrentUser().getUid();
            esInvitado = false;
        } else {
            uidUsuarioActual = null;
            esInvitado = true;
        }

        comprobarSiEsAdmin();
    }

    private String getUidPerfil(FirebaseAuth mAuth) {
        String uidOtroUsuario = getIntent().getStringExtra("UID_USUARIO");

        if (uidOtroUsuario != null && !uidOtroUsuario.isEmpty()) {
            return uidOtroUsuario;
        }

        // Si no hay Intent, comprobamos si es su propio perfil
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }

        // Este caso es el de un invitado que ha entrado a un perfil
        return null;
    }

    /**
     * Muestra los datos del cliente
     */
    private void mostrarDatosCliente() {
        usuarioService
                .getPerfil(uidCliente)
                .addOnSuccessListener(
                        usuario -> {
                            tvNombre.setText(usuario.getNombre());
                            tvUsername.setText("@" + usuario.getUsername());
                            tvCiudad.setText(usuario.getCiudad());
                            tvFechaRegistro.setText(
                                    DateUtils.getFechaFormateadaLong(usuario.getFechaRegistro()));

                            if (usuario.getFotoPerfil() != null) {
                                Glide.with(this).load(usuario.getFotoPerfil()).into(imgPerfil);
                            }

                            cargarChipsPreferencias(usuario);
                            comprobarCargaCompleta();
                        })
                .addOnFailureListener(
                        e -> {
                            if (e instanceof FoodTracksValidationException ex) {
                                Toast.makeText(this, ex.getErrorResId(), Toast.LENGTH_SHORT).show();
                            } else if (e instanceof FoodTracksNotFoundException ex) {
                                Toast.makeText(this, ex.getErrorResId(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(
                                                this,
                                                getString(R.string.loading_profile_error_message)
                                                        + ": "
                                                        + e.getMessage(),
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                            comprobarCargaCompleta();
                        });
    }

    /**
     * Inyecta los chips de preferencias basados en los datos del usuario.
     */
    private void cargarChipsPreferencias(Usuario usuario) {
        // Limpiamos los chips anteriores por si la vista se recarga
        chipGroupPreferencias.removeAllViews();

        if (usuario.isEsVegano()) {
            addChip("\uD83C\uDF31" + getString(R.string.vegano));
        }
        if (usuario.isEsVegetariano()) {
            addChip("\uD83C\uDF3F" + getString(R.string.vegetariano));
        }
        if (usuario.isSinLactosa()) {
            addChip("\uD83E\uDD5B" + getString(R.string.sin_lactosa));
        }
        if (usuario.isEsCeliaco()) {
            addChip("\uD83C\uDF3E" + getString(R.string.celiaco));
        }

        if (usuario.getOtraPreferencia() instanceof String otraPreferencia) {
            addChip("\uD83D\uDCDD" + otraPreferencia);
        }

        // Si el usuario no tiene ninguna preferencia, mostramos por defecto
        if (chipGroupPreferencias.getChildCount() == 0) {
            addChip(getString(R.string.sin_preferencias));
        }
    }

    /**
     * Crea un Chip visual y lo añade al ChipGroup de la interfaz.
     * @param texto Texto de la preferencia.
     */
    private void addChip(String texto) {
        Chip chip = new Chip(this);
        chip.setText(texto);

        // Solo lectura
        chip.setCheckable(false);
        chip.setClickable(false);

        chip.setChipBackgroundColorResource(R.color.tertiary);
        chip.setTextColor(getResources().getColor(R.color.white, null));

        chipGroupPreferencias.addView(chip);
    }

    /**
     * Carga las publicaciones del usuario.
     */
    private void cargarPublicaciones() {
        publicacionService
                .getPublicacionesByUsuario(uidCliente)
                .addOnSuccessListener(
                        publicaciones -> {
                            if (publicaciones == null || publicaciones.isEmpty()) {
                                tvSinPublicaciones.setVisibility(android.view.View.VISIBLE);
                                recyclerPublicaciones.setVisibility(android.view.View.GONE);
                            } else {
                                tvSinPublicaciones.setVisibility(android.view.View.GONE);
                                recyclerPublicaciones.setVisibility(android.view.View.VISIBLE);

                                adapter = new PublicacionAdapter(publicaciones, this);
                                recyclerPublicaciones.setAdapter(adapter);
                            }

                            comprobarCargaCompleta();
                        })
                .addOnFailureListener(
                        e -> {
                            Toast.makeText(
                                            this,
                                            R.string.publicaciones_loading_error_message
                                                    + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                            comprobarCargaCompleta();
                        });
    }

    /**
     * Aumenta el contador y, si ambas peticiones han terminado, muestra la pantalla.
     */
    private synchronized void comprobarCargaCompleta() {
        tareasCompletadas++;

        // 2 tareas: 1. Cargar perfil, 2. Cargar publicaciones
        if (tareasCompletadas >= 2) {
            progressBar.setVisibility(android.view.View.GONE);
            layoutContenido.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void comprobarSiEsAdmin() {
        if (esInvitado || uidUsuarioActual == null) return;

        usuarioService.getPerfil(uidUsuarioActual).addOnSuccessListener(usuarioActual -> {
            if ("admin".equals(usuarioActual.getRol()) && !uidUsuarioActual.equals(uidCliente)) {

                // Comprobamos el rol del perfil que estamos visitando
                usuarioService.getPerfil(uidCliente).addOnSuccessListener(usuarioVisitado -> {
                    // Si el otro usuario es admin no activamos la opción de borrar
                    if (!"admin".equals(usuarioVisitado.getRol())) {
                        imgEliminarPerfilAdmin.setVisibility(android.view.View.VISIBLE);
                        imgEliminarPerfilAdmin.setOnClickListener(v -> mostrarDialogoEliminarPerfilAdmin());
                    } else {
                        imgEliminarPerfilAdmin.setVisibility(android.view.View.GONE);
                    }
                });
            }
        });
    }

    private void mostrarDialogoEliminarPerfilAdmin() {
        EditText inputMotivo = new EditText(this);
        inputMotivo.setHint(R.string.motivo_eliminacion);
        inputMotivo.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 50;
        params.rightMargin = 50;
        inputMotivo.setLayoutParams(params);
        container.addView(inputMotivo);

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.eliminar_perfil_como_admin)
                        .setMessage(R.string.motivo_eliminacion_usuario_como_admin)
                        .setView(container)
                        .setPositiveButton(
                                R.string.eliminar,
                                (dialogInterface, which) -> {
                                    String motivo = inputMotivo.getText().toString().trim();
                                    if (motivo.isEmpty()) motivo = MOTIVO_DEFAULT;

                                    layoutContenido.setVisibility(android.view.View.GONE);
                                    progressBar.setVisibility(android.view.View.VISIBLE);

                                    usuarioService
                                            .eliminarCuentaByAdmin(
                                                    uidCliente, motivo, uidUsuarioActual)
                                            .addOnSuccessListener(
                                                    unused -> {
                                                        Toast.makeText(
                                                                        this,
                                                                        R.string
                                                                                .perfil_eliminado_por_admin,
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                        finish();
                                                    })
                                            .addOnFailureListener(
                                                    e -> {
                                                        layoutContenido.setVisibility(
                                                                android.view.View.VISIBLE);
                                                        progressBar.setVisibility(
                                                                android.view.View.GONE);
                                                        Log.e(
                                                                "Eliminado usuario cliente como admin",
                                                                e.getMessage(),
                                                                e);
                                                        Toast.makeText(
                                                                        this,
                                                                        R.string.error_al_eliminar,
                                                                        Toast.LENGTH_SHORT)
                                                                .show();
                                                    });
                                })
                        .setNegativeButton(
                                R.string.cancelar,
                                (dialogInterface, which) -> dialogInterface.dismiss())
                        .show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(android.graphics.Color.RED);
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(android.graphics.Color.BLACK);
    }

    /**
     * Configura la barra de navegación y de estado
     */
    private void configTheme() {
        getWindow()
                .setNavigationBarColor(
                        androidx.core.content.ContextCompat.getColor(this, R.color.primary));
    }
}
