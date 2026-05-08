/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.fragments.cliente;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foodtracks.app.R;
import com.foodtracks.app.activities.cliente.MainClienteActivity;
import com.foodtracks.app.activities.cliente.PerfilClienteActivity;
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
 * Fragment de perfil del cliente.
 * Mismo comportamiento que {@link PerfilClienteActivity} pero se ve desde
 * {@link MainClienteActivity} para mantener la barra de navegación y
 * no saturar el rendimiento de la app.
 * @author Robert
 * @since 08/05
 */
public class PerfilClienteFragment extends Fragment {

    private View rootView; // Vista principal del fragment

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
    private int tareasCompletadas = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_perfil_cliente, container, false);

        inicializar();
        mostrarDatosCliente();
        cargarPublicaciones();

        return rootView;
    }

    private void inicializar() {
        mAuth = FirebaseAuth.getInstance();
        uidCliente = getUidPerfil(mAuth);

        usuarioService = ServiceFactory.provideUsuarioService(requireContext());
        publicacionService = ServiceFactory.providePublicacionService(requireContext());

        tvNombre = rootView.findViewById(R.id.tvNombreCliente);
        tvUsername = rootView.findViewById(R.id.tvUsernameCliente);
        tvCiudad = rootView.findViewById(R.id.tvCiudadCliente);
        tvFechaRegistro = rootView.findViewById(R.id.tvFechaRegistroCliente);
        tvSinPublicaciones = rootView.findViewById(R.id.tvSinPublicaciones);
        imgPerfil = rootView.findViewById(R.id.imgPerfilCliente);
        chipGroupPreferencias = rootView.findViewById(R.id.chipGroupPreferencias);
        recyclerPublicaciones = rootView.findViewById(R.id.recyclerPublicacionesCliente);
        recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        progressBar = rootView.findViewById(R.id.progressBarPerfil);
        layoutContenido = rootView.findViewById(R.id.layoutContenidoPerfil);
    }

    private String getUidPerfil(FirebaseAuth mAuth) {
        // Los fragmentos usan Arguments en vez de Intents para recibir datos
        if (getArguments() != null && getArguments().containsKey("UID_USUARIO")) {
            String uidOtroUsuario = getArguments().getString("UID_USUARIO");
            if (uidOtroUsuario != null && !uidOtroUsuario.isEmpty()) {
                return uidOtroUsuario;
            }
        }

        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

    private void mostrarDatosCliente() {
        usuarioService.getPerfil(uidCliente)
                .addOnSuccessListener(usuario -> {
                    if (!isAdded()) return; // Si el usuario cambió de pantalla se ignora -- Evita crasheos
                    tvNombre.setText(usuario.getNombre());
                    tvUsername.setText("@" + usuario.getUsername());
                    tvCiudad.setText(usuario.getCiudad());
                    tvFechaRegistro.setText(DateUtils.getFechaFormateadaLong(usuario.getFechaRegistro()));

                    if (usuario.getFotoPerfil() != null) {
                        Glide.with(requireContext()).load(usuario.getFotoPerfil()).into(imgPerfil);
                    }

                    cargarChipsPreferencias(usuario);
                    comprobarCargaCompleta();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    if (e instanceof FoodTracksValidationException ex) {
                        Toast.makeText(requireContext(), ex.getErrorResId(), Toast.LENGTH_SHORT).show();
                    } else if (e instanceof FoodTracksNotFoundException ex) {
                        Toast.makeText(requireContext(), ex.getErrorResId(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.loading_profile_error_message) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    comprobarCargaCompleta();
                });
    }

    private void cargarChipsPreferencias(Usuario usuario) {
        chipGroupPreferencias.removeAllViews();
        if (usuario.isEsVegano()) addChip("\uD83C\uDF31" + getString(R.string.vegano));
        if (usuario.isEsVegetariano()) addChip("\uD83C\uDF3F" + getString(R.string.vegetariano));
        if (usuario.isSinLactosa()) addChip("\uD83E\uDD5B" + getString(R.string.sin_lactosa));
        if (usuario.isEsCeliaco()) addChip("\uD83C\uDF3E" + getString(R.string.celiaco));
        if (usuario.getOtraPreferencia() instanceof String otraPreferencia) addChip("\uD83D\uDCDD" + otraPreferencia);

        if (chipGroupPreferencias.getChildCount() == 0) {
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
        chipGroupPreferencias.addView(chip);
    }

    private void cargarPublicaciones() {
        publicacionService.getPublicacionesByUsuario(uidCliente)
                .addOnSuccessListener(publicaciones -> {
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
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), R.string.publicaciones_loading_error_message + e.getMessage(), Toast.LENGTH_SHORT).show();
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
}