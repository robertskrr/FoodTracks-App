/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.widget.LinearLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;

import com.foodtracks.app.R;
import com.foodtracks.app.activities.MainActivity;
import com.foodtracks.app.activities.VideoPlayerActivity;
import com.foodtracks.app.activities.admin.MainAdminActivity;
import com.foodtracks.app.activities.cliente.EditarPerfilClienteActivity;
import com.foodtracks.app.activities.cliente.MainClienteActivity;
import com.foodtracks.app.activities.local.EditarPerfilLocalActivity;
import com.foodtracks.app.activities.local.MainLocalActivity;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.interfaces.IUsuarioService;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Fragment de ajustes de la aplicación.
 *
 * @author Robert
 * @since 14/05
 */
public class SettingsFragment extends Fragment {

    private View rootView;
    private SwitchMaterial switchNotificaciones, switchSonidos;
    private Button btnEditarPerfil,
            btnCerrarSesion,
            btnEliminarCuenta,
            btnCambiarPassword,
            btnVideo,
            btnPremium;
    private FrameLayout overlayCarga;

    private String uidUsuario;
    private IUsuarioService usuarioService;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private boolean esInvitado, esLocal, esCliente, esAdmin;

    // Nombres para SharedPreferences
    private static final String PREFS_NAME = "FoodTracksSettings";
    private static final String KEY_NOTIF = "notificaciones_activas";
    private static final String KEY_SOUNDS = "sonidos_silenciados";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        configTheme();
        inicializar();
        cargarPreferencias();
        configurarListeners();

        return rootView;
    }

    /**
     * Asigna los componentes de la interfaz.
     */
    private void inicializar() {
        mAuth = FirebaseAuth.getInstance();
        usuarioService = ServiceFactory.provideUsuarioService(requireContext());
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (mAuth.getCurrentUser() != null) {
            uidUsuario = mAuth.getCurrentUser().getUid();
        } else {
            esInvitado = true;
        }

        switchNotificaciones = rootView.findViewById(R.id.switchNotificaciones);
        switchSonidos = rootView.findViewById(R.id.switchSonidos);
        btnEditarPerfil = rootView.findViewById(R.id.btnEditarPerfil);
        btnCerrarSesion = rootView.findViewById(R.id.btnCerrarSesion);
        btnEliminarCuenta = rootView.findViewById(R.id.btnEliminarCuenta);
        btnCambiarPassword = rootView.findViewById(R.id.btnCambiarPassword);
        btnVideo = rootView.findViewById(R.id.btnVideo);
        btnPremium = rootView.findViewById(R.id.btnPlanPremium);
        overlayCarga = rootView.findViewById(R.id.overlayCargaSettings);

        if (getActivity() instanceof MainLocalActivity) {
            esLocal = true;
            btnPremium.setVisibility(View.VISIBLE);
        } else if (getActivity() instanceof MainClienteActivity) {
            if (!esInvitado) {
                esCliente = true;
                btnPremium.setVisibility(View.VISIBLE);
            }
        } else if (getActivity() instanceof MainAdminActivity) {
            esAdmin = true;
        }

        if (esInvitado) {
            btnEditarPerfil.setVisibility(View.GONE);
            btnCambiarPassword.setVisibility(View.GONE);
            btnEliminarCuenta.setVisibility(View.GONE);
            btnPremium.setVisibility(View.GONE);
            btnCerrarSesion.setText(R.string.salir);
        }
    }

    /**
     * Carga las preferencias guardadas del usuario.
     */
    private void cargarPreferencias() {
        boolean notifActivas = sharedPreferences.getBoolean(KEY_NOTIF, true);
        boolean sonidosSilenciados = sharedPreferences.getBoolean(KEY_SOUNDS, false);

        switchNotificaciones.setChecked(notifActivas);
        switchSonidos.setChecked(sonidosSilenciados);
    }

    /**
     * Configura los listeners de los componentes.
     */
    private void configurarListeners() {
        // Guardar cambios en switches
        switchNotificaciones.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    sharedPreferences.edit().putBoolean(KEY_NOTIF, isChecked).apply();
                });

        switchSonidos.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    sharedPreferences.edit().putBoolean(KEY_SOUNDS, isChecked).apply();
                });

        // Editar perfil
        btnEditarPerfil.setOnClickListener(v -> redirigirAEditarPerfil());

        // Cerrar sesión
        btnCerrarSesion.setOnClickListener(
                v -> {
                    if (esInvitado) {
                        logOut();
                    } else {
                        mostrarDialogoCerrarSesion();
                    }
                });

        // Cambiar contraseña
        btnCambiarPassword.setOnClickListener(v -> mostrarDialogoCambiarPassword());

        // Eliminar cuenta
        btnEliminarCuenta.setOnClickListener(v -> mostrarDialogoEliminarCuenta());

        // Reproducir vídeo intolerancias y alergias
        btnVideo.setOnClickListener(
                v -> startActivity(new Intent(requireContext(), VideoPlayerActivity.class)));

        // Plan premium
        btnPremium.setOnClickListener(
                v -> {
                    VisorImagenDialogFragment visor;

                    if (esLocal) {
                        visor = VisorImagenDialogFragment.newInstance(R.drawable.premium_local);
                    } else if (esCliente) {
                        visor = VisorImagenDialogFragment.newInstance(R.drawable.premium_cliente);
                    } else {
                        return;
                    }

                    if (getActivity() != null) {
                        visor.show(getActivity().getSupportFragmentManager(), "VisorPremium");
                    }
                });
    }

    /**
     * Redirección a la activity de Editar Perfil según el rol.
     */
    private void redirigirAEditarPerfil() {
        if (esInvitado) return;

        Intent intent;
        if (esLocal) {
            intent = new Intent(requireContext(), EditarPerfilLocalActivity.class);
        } else {
            intent = new Intent(requireContext(), EditarPerfilClienteActivity.class);
        }

        startActivity(intent);
    }

    /**
     * Muestra el diálogo de confirmación de cierre de sesión.
     */
    private void mostrarDialogoCerrarSesion() {
        AlertDialog dialog =
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.cerrar_sesion)
                        .setMessage(R.string.confirm_cerrar_sesion)
                        .setPositiveButton(
                                R.string.salir,
                                (dialogInterface, which) -> {
                                    logOut();
                                })
                        .setNegativeButton(
                                R.string.cancelar,
                                (dialogInterface, which) -> dialogInterface.dismiss())
                        .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }

    /**
     * Muestra el diálogo de confirmación de eliminación de cuenta.
     */
    private void mostrarDialogoEliminarCuenta() {
        AlertDialog dialog =
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.eliminar_cuenta_warning)
                        .setMessage(R.string.confirm_eliminar_cuenta)
                        .setPositiveButton(
                                R.string.eliminar_para_siempre,
                                (dialogInterface, which) -> {
                                    if (!esInvitado) {

                                        // Bloqueamos la pantalla y aparece la animación de borrado
                                        overlayCarga.setVisibility(View.VISIBLE);

                                        usuarioService
                                                .eliminarCuenta(uidUsuario)
                                                .addOnSuccessListener(
                                                        unused -> {
                                                            logOut();
                                                            Toast.makeText(
                                                                            requireContext(),
                                                                            R.string
                                                                                    .cuenta_eliminada,
                                                                            Toast.LENGTH_LONG)
                                                                    .show();
                                                        })
                                                .addOnFailureListener(
                                                        e -> {
                                                            overlayCarga.setVisibility(View.GONE);
                                                            Log.e(
                                                                    "Error al eliminar cuenta",
                                                                    e.getMessage(),
                                                                    e);
                                                            Toast.makeText(
                                                                            requireContext(),
                                                                            R.string
                                                                                    .error_al_eliminar,
                                                                            Toast.LENGTH_SHORT)
                                                                    .show();
                                                        });
                                    }
                                })
                        .setNegativeButton(
                                R.string.cancelar,
                                (dialogInterface, which) -> dialogInterface.dismiss())
                        .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }

    /**
     * Muestra el diálogo de cambio de contraseña con reautenticación.
     */
    private void mostrarDialogoCambiarPassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        // Layout vertical para los dos campos de texto
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        // Contraseña actual
        EditText inputPassActual = new EditText(requireContext());
        inputPassActual.setHint(R.string.contrasenia_actual);
        inputPassActual.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputPassActual);

        // Nueva contraseña
        EditText inputPassNueva = new EditText(requireContext());
        inputPassNueva.setHint(R.string.nueva_contrasenia);
        inputPassNueva.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Margen superior al campo de nueva contraseña
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 30;
        inputPassNueva.setLayoutParams(params);
        layout.addView(inputPassNueva);

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.cambiar_contrasenia)
                        .setMessage(R.string.confirma_contrasenia_actual)
                        .setView(layout)
                        .setPositiveButton(
                                R.string.actualizar,
                                (dialogInterface, which) -> {
                                    String actualPass = inputPassActual.getText().toString().trim();
                                    String nuevaPass = inputPassNueva.getText().toString().trim();

                                    if (actualPass.isEmpty()) {
                                        Toast.makeText(requireContext(), R.string.debes_introducir_contrasenia_actual, Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    if (nuevaPass.length() < 8) {
                                        Toast.makeText(requireContext(), R.string.password_length_error_message, Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    overlayCarga.setVisibility(View.VISIBLE);

                                    // Reautenticar al usuario con la contraseña actual
                                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), actualPass);

                                    user.reauthenticate(credential)
                                            .addOnSuccessListener(unused -> {
                                                // Si la contraseña actual es correcta, actualizamos a la nueva
                                                user.updatePassword(nuevaPass)
                                                        .addOnSuccessListener(aVoid -> {
                                                            overlayCarga.setVisibility(View.GONE);
                                                            Toast.makeText(requireContext(), R.string.contrasenia_actualizada, Toast.LENGTH_SHORT).show();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            overlayCarga.setVisibility(View.GONE);
                                                            Log.e("Actualizar contraseña", e.getMessage(), e);
                                                            Toast.makeText(requireContext(), R.string.error_al_actualizar, Toast.LENGTH_SHORT).show();
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                overlayCarga.setVisibility(View.GONE);
                                                Toast.makeText(requireContext(), R.string.contrasenia_actual_incorrecta, Toast.LENGTH_LONG).show();
                                            });
                                })
                        .setNegativeButton(
                                R.string.cancelar,
                                (dialogInterface, which) -> dialogInterface.dismiss())
                        .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
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

    /**
     * Configura el tema de la interfaz.
     */
    private void configTheme() {
        if (getActivity() != null) {
            getActivity()
                    .getWindow()
                    .setStatusBarColor(
                            ContextCompat.getColor(requireContext(), R.color.admin_bottom_nav));
        }
    }
}
