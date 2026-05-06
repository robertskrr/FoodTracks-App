/**
 * © FoodTracks Project ===robertskrr===
 */

package com.foodtracks.app.activities.cliente;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodtracks.app.R;

import com.foodtracks.app.adapters.PublicacionAdapter;
import com.foodtracks.app.models.Usuario;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.exceptions.FoodTracksNotFoundException;
import com.foodtracks.app.services.exceptions.FoodTracksValidationException;
import com.foodtracks.app.services.interfaces.IPublicacionService;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.foodtracks.app.utils.DateUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
    private IUsuarioService usuarioService;
    private IPublicacionService publicacionService;
    private PublicacionAdapter adapter;
    private ProgressBar progressBar;
    private NestedScrollView layoutContenido;
    private int tareasCompletadas = 0; // Contador de tareas de Firebase para la pantalla de carga

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_cliente);

        inicializar();
        mostrarDatosCliente();
        cargarPublicaciones();
    }

    /**
     * Asigna los componentes a la interfaz
     */
    private void inicializar() {
        mAuth = FirebaseAuth.getInstance();
        assert mAuth.getCurrentUser() != null;
        uidCliente = mAuth.getCurrentUser().getUid();
        usuarioService = ServiceFactory.provideUsuarioService(this);
        publicacionService = ServiceFactory.providePublicacionService(this);

        // TextView
        tvNombre = findViewById(R.id.tvNombreCliente);
        tvUsername = findViewById(R.id.tvUsernameCliente);
        tvCiudad = findViewById(R.id.tvCiudadCliente);
        tvFechaRegistro = findViewById(R.id.tvFechaRegistroCliente);

        tvSinPublicaciones = findViewById(R.id.tvSinPublicaciones);

        // Foto perfil
        imgPerfil = findViewById(R.id.imgPerfilCliente);

        // Preferencias
        chipGroupPreferencias = findViewById(R.id.chipGroupPreferencias);

        // Publicaciones
        recyclerPublicaciones = findViewById(R.id.recyclerPublicacionesCliente);
        recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(this));

        // Pantalla de carga
        progressBar = findViewById(R.id.progressBarPerfil);
        layoutContenido = findViewById(R.id.layoutContenidoPerfil);
    }

    /**
     * Muestra los datos del cliente
     */
    private void mostrarDatosCliente() {
        // TODO: Obtener el perfil desde username seria lo ideal para otros usuarios
        usuarioService.getPerfil(uidCliente).addOnSuccessListener(usuario -> {
            tvNombre.setText(usuario.getNombre());
            tvUsername.setText("@" + usuario.getUsername());
            tvCiudad.setText(usuario.getCiudad());
            tvFechaRegistro.setText(DateUtils.getFechaFormateadaLong(usuario.getFechaRegistro()));

            if (usuario.getFotoPerfil() != null) {
                Glide.with(this).load(usuario.getFotoPerfil()).into(imgPerfil);
            }

            cargarChipsPreferencias(usuario);
            comprobarCargaCompleta();
        }).addOnFailureListener(e -> {
            if (e
                    instanceof
                    FoodTracksValidationException
                            ex) {
                Toast.makeText(
                                this,
                                ex.getErrorResId(),
                                Toast.LENGTH_SHORT)
                        .show();
            } else if (e
                    instanceof
                    FoodTracksNotFoundException
                            ex) {
                Toast.makeText(
                                this,
                                ex.getErrorResId(),
                                Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(
                                this, getString(R.string.loading_profile_error_message) + ": " + e.getMessage(),
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

        // TODO: personalizar los colores
        // chip.setChipBackgroundColorResource(R.color.tertiary);
        // chip.setTextColor(getResources().getColor(R.color.white));

        chipGroupPreferencias.addView(chip);
    }

    /**
     * Carga las publicaciones del usuario.
     */
    private void cargarPublicaciones() {
        publicacionService.getPublicacionesByUsuario(uidCliente)
                .addOnSuccessListener(publicaciones -> {

                    if (publicaciones == null || publicaciones.isEmpty()) {
                        tvSinPublicaciones.setVisibility(android.view.View.VISIBLE);
                        recyclerPublicaciones.setVisibility(android.view.View.GONE);
                    } else {
                        tvSinPublicaciones.setVisibility(android.view.View.GONE);
                        recyclerPublicaciones.setVisibility(android.view.View.VISIBLE);

                        adapter = new PublicacionAdapter(
                                publicaciones,
                                PerfilClienteActivity.this
                        );
                        recyclerPublicaciones.setAdapter(adapter);
                    }

                    comprobarCargaCompleta();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, R.string.publicaciones_loading_error_message + e.getMessage(), Toast.LENGTH_SHORT).show();
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
}
