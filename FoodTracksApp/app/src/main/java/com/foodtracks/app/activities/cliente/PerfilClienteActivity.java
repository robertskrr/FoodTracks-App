/**
 * © FoodTracks Project ===robertskrr===
 */

package com.foodtracks.app.activities.cliente;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.foodtracks.app.R;

import com.foodtracks.app.models.Usuario;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.exceptions.FoodTracksNotFoundException;
import com.foodtracks.app.services.exceptions.FoodTracksValidationException;
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

    private TextView tvNombre, tvUsername, tvCiudad, tvFechaRegistro;
    private ShapeableImageView imgPerfil;
    private ChipGroup chipGroupPreferencias;
    private String uidCliente;
    private FirebaseAuth mAuth;
    private IUsuarioService usuarioService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_cliente);

        // TODO: Pantalla de carga para mostrar los datos
        inicializar();
        mostrarDatosCliente();
    }

    /**
     * Asigna los componentes a la interfaz
     */
    private void inicializar() {
        mAuth = FirebaseAuth.getInstance();
        assert mAuth.getCurrentUser() != null;
        uidCliente = mAuth.getCurrentUser().getUid();
        usuarioService = ServiceFactory.provideUsuarioService(this);

        // TextView
        tvNombre = findViewById(R.id.tvNombreCliente);
        tvUsername = findViewById(R.id.tvUsernameCliente);
        tvCiudad = findViewById(R.id.tvCiudadCliente);
        tvFechaRegistro = findViewById(R.id.tvFechaRegistroCliente);

        // Foto perfil
        imgPerfil = findViewById(R.id.imgPerfilCliente);

        // Preferencias
        chipGroupPreferencias = findViewById(R.id.chipGroupPreferencias);
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
            tvFechaRegistro.setText(DateUtils.getFechaFormateada(usuario.getFechaRegistro()));

            if (usuario.getFotoPerfil() != null) {
                Glide.with(this).load(usuario.getFotoPerfil()).into(imgPerfil);
            }

            cargarChipsPreferencias(usuario);

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
        });
    }

    /**
     * Inyecta los chips de preferencias basados en los datos del usuario.
     */
    private void cargarChipsPreferencias(Usuario usuario) {
        // Limpiamos los chips anteriores por si la vista se recarga
        chipGroupPreferencias.removeAllViews(); //[cite: 25]

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

        // Si el usuario no tiene ninguna preferencia marcada, mostramos un chip por defecto
        if (chipGroupPreferencias.getChildCount() == 0) {
            addChip(getString(R.string.sin_preferencias));
        }
    }

    /**
     * Crea un Chip visual y lo añade al ChipGroup de la interfaz.
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
}
