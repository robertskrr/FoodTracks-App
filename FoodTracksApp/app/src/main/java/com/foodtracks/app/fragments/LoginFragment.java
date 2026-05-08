/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.foodtracks.app.R;
import com.foodtracks.app.activities.admin.AdminActivity;
import com.foodtracks.app.activities.cliente.HomeActivity;
import com.foodtracks.app.activities.local.DashBoardLocalActivity;
import com.foodtracks.app.models.Usuario;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.interfaces.IUsuarioService;

import com.google.firebase.auth.FirebaseAuth;

/**
 * @author Robert
 * @since 17/02
 */
public class LoginFragment extends DialogFragment {

    private EditText email, password;
    private TextView irARegistro;
    private Button btnLogin;

    private FirebaseAuth mAuth;
    private IUsuarioService usuarioService;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        inicializar(v);

        irARegistro.setOnClickListener(
                v1 -> {
                    dismiss();
                    TipoRegistroFragment registroFragment = new TipoRegistroFragment();
                    registroFragment.show(getParentFragmentManager(), "Fragment registro");
                });

        btnLogin.setOnClickListener(
                v2 -> {
                    String emailLogin = email.getText().toString().trim();
                    String passwordLogin = password.getText().toString().trim();

                    if (emailLogin.isEmpty() || passwordLogin.isEmpty()) {
                        Toast.makeText(
                                        getContext(),
                                        R.string.complete_todos_los_campos,
                                        Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    // Evitamos dobles clicks
                    btnLogin.setEnabled(false);
                    loginUser(emailLogin, passwordLogin);
                });

        return v;
    }

    /**
     * Asigna los componentes a la interfaz
     *
     * @param v Vista del fragmento
     */
    private void inicializar(View v) {
        btnLogin = v.findViewById(R.id.btnAcceder);
        email = v.findViewById(R.id.txtLoginEmail);
        password = v.findViewById(R.id.txtLoginPassword);
        irARegistro = v.findViewById(R.id.txtVolverARegistro);

        mAuth = FirebaseAuth.getInstance();
        usuarioService = ServiceFactory.provideUsuarioService(requireContext());
    }

    /**
     * Proceso de login del usuario
     * @param emailLogin Correo electrónico
     * @param passwordLogin Contraseña
     */
    private void loginUser(String emailLogin, String passwordLogin) {
        mAuth.signInWithEmailAndPassword(emailLogin, passwordLogin)
                .addOnSuccessListener(
                        authResult -> {
                            if (authResult.getUser() != null) {
                                String uid = authResult.getUser().getUid();

                                usuarioService
                                        .getPerfil(uid)
                                        .addOnSuccessListener(this::cambiarActivity)
                                        .addOnFailureListener(
                                                e -> {
                                                    Toast.makeText(
                                                                    getContext(),
                                                                    getString(
                                                                            R.string
                                                                                    .loading_profile_error_message),
                                                                    Toast.LENGTH_SHORT)
                                                            .show();
                                                    Log.d(
                                                            "ERROR",
                                                            getString(
                                                                            R.string
                                                                                    .loading_profile_error_message)
                                                                    + e.getMessage());
                                                    btnLogin.setEnabled(true);
                                                });
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            Toast.makeText(
                                            getContext(),
                                            R.string.login_error_message,
                                            Toast.LENGTH_SHORT)
                                    .show();
                            btnLogin.setEnabled(true);
                        });
    }

    /**
     * Cambia a la activity correspondiente
     * @param usuario Usuario logueado
     */
    private void cambiarActivity(Usuario usuario) {
        Intent intent = getIntent(usuario);

        // Limpiamos historial de activities
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        dismiss();
        startActivity(intent);
    }

    /**
     * Filtra la activity resultante según el rol
     * @param usuario Usuario logueado
     * @return Intent de la activity filtrada
     */
    @NonNull
    private Intent getIntent(Usuario usuario) {
        Intent intent;
        String rol = usuario.getRol();

        if ("admin".equals(rol)) {
            intent = new Intent(getContext(), AdminActivity.class);
        } else if ("local".equals(rol)) {
            intent = new Intent(getContext(), DashBoardLocalActivity.class);
        } else {
            intent = new Intent(getContext(), HomeActivity.class);
        }
        return intent;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            int width = getResources().getDisplayMetrics().widthPixels;
            int desiredWidth = (int) (width * 0.85);
            getDialog().getWindow().setLayout(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
