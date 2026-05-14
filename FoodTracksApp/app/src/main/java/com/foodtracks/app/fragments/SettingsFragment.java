/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.foodtracks.app.R;

import com.foodtracks.app.activities.MainActivity;

import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Fragment de ajustes de la aplicación.
 *
 * @author Robert
 * @since 14/05
 */
public class SettingsFragment extends Fragment {

    private View rootView;
    private SwitchMaterial switchNotificaciones, switchSonidos;
    private Button btnEditarPerfil, btnCerrarSesion, btnEliminarCuenta;

    private String uidUsuario;
    private IUsuarioService usuarioService;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private boolean esInvitado;

    // Nombres para SharedPreferences
    private static final String PREFS_NAME = "FoodTracksSettings";
    private static final String KEY_NOTIF = "notificaciones_activas";
    private static final String KEY_SOUNDS = "sonidos_silenciados";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        configTheme();
        inicializar();
        cargarPreferenciasLocales();
        configurarListeners();

        return rootView;
    }

    private void inicializar() {
        mAuth = FirebaseAuth.getInstance();
        usuarioService = ServiceFactory.provideUsuarioService(requireContext());
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (mAuth.getCurrentUser() != null) {
            uidUsuario = mAuth.getCurrentUser().getUid();
        } else{
            esInvitado = true;
        }

        switchNotificaciones = rootView.findViewById(R.id.switchNotificaciones);
        switchSonidos = rootView.findViewById(R.id.switchSonidos);
        btnEditarPerfil = rootView.findViewById(R.id.btnEditarPerfil);
        btnCerrarSesion = rootView.findViewById(R.id.btnCerrarSesion);
        btnEliminarCuenta = rootView.findViewById(R.id.btnEliminarCuenta);

        if (esInvitado) {
            btnEditarPerfil.setVisibility(View.GONE);
            btnEliminarCuenta.setVisibility(View.GONE);
            btnCerrarSesion.setText(R.string.salir);
        }
    }

    private void cargarPreferenciasLocales() {
        boolean notifActivas = sharedPreferences.getBoolean(KEY_NOTIF, true);
        boolean sonidosSilenciados = sharedPreferences.getBoolean(KEY_SOUNDS, false);

        switchNotificaciones.setChecked(notifActivas);
        switchSonidos.setChecked(sonidosSilenciados);
    }

    private void configurarListeners() {
        // Guardar cambios en switches
        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_NOTIF, isChecked).apply();
            // TODO: Lógica real para activar/desactivar notificaciones FCM
        });

        switchSonidos.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_SOUNDS, isChecked).apply();
            // TODO: Lógica real para mutear MediaPlayer/SoundPool
        });

        // Editar perfil (dependiendo del rol)
        btnEditarPerfil.setOnClickListener(v -> redirigirAEdicionPerfil());

        // Cerrar sesión
        btnCerrarSesion.setOnClickListener(v -> {
            if (esInvitado) {
                logOut();
            } else {
                mostrarDialogoCerrarSesion();
            }
        });

        // Eliminar cuenta
        btnEliminarCuenta.setOnClickListener(v -> mostrarDialogoEliminarCuenta());
    }

    private void redirigirAEdicionPerfil() {
        if (uidUsuario == null) return;

        btnEditarPerfil.setEnabled(false);

        usuarioService.getPerfil(uidUsuario).addOnSuccessListener(usuario -> {
            if (!isAdded()) return;

            Intent intent;
            if ("local".equals(usuario.getRol())) {
                // TODO: intent = new Intent(requireContext(), EditarPerfilLocalActivity.class);
            } else {
                // TODO: intent = new Intent(requireContext(), EditarPerfilClienteActivity.class);
            }

            // startActivity(intent);
            btnEditarPerfil.setEnabled(true);

        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            Toast.makeText(requireContext(), R.string.loading_profile_error_message, Toast.LENGTH_SHORT).show();
            btnEditarPerfil.setEnabled(true);
        });
    }

    private void mostrarDialogoCerrarSesion() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cerrar_sesion)
                .setMessage(R.string.confirm_cerrar_sesion)
                .setPositiveButton(R.string.salir, (dialogInterface, which) -> {
                    logOut();
                })
                .setNegativeButton(R.string.cancelar, (dialogInterface, which) -> dialogInterface.dismiss())
                .show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.RED);
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.BLACK);
    }

    private void mostrarDialogoEliminarCuenta() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.eliminar_cuenta_warning)
                .setMessage(R.string.confirm_eliminar_cuenta)
                .setPositiveButton(R.string.eliminar_para_siempre, (dialogInterface, which) -> {

                    if(!esInvitado) {
                        usuarioService.eliminarCuenta(uidUsuario).addOnSuccessListener(unused -> {
                            logOut();
                            Toast.makeText(requireContext(), R.string.cuenta_eliminada, Toast.LENGTH_LONG).show();
                        }).addOnFailureListener(e -> {
                            Log.e("Error al eliminar cuenta", e.getMessage(), e);
                            Toast.makeText(requireContext(), R.string.error_al_eliminar, Toast.LENGTH_SHORT).show();
                        });
                    }

                })
                .setNegativeButton(R.string.cancelar, (dialogInterface, which) -> dialogInterface.dismiss())
                .show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.RED);
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.BLACK);
    }

    /**
     * Cierra la sesión del usuario y lo devuelve a la pantalla de inicio.
     */
    private void logOut() {
        if (!esInvitado) {
            mAuth.signOut();
            // Reseteamos preferencias
            sharedPreferences.edit().clear().apply();
        }
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(requireContext(), R.string.despedida_app, Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
    private void configTheme() {
        if (getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(
                    androidx.core.content.ContextCompat.getColor(requireContext(), R.color.admin_bottom_nav));
        }
    }
}