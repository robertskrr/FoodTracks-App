/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

import com.foodtracks.app.R;
import com.foodtracks.app.activities.admin.AdminActivity;
import com.foodtracks.app.activities.cliente.HomeActivity;
import com.foodtracks.app.activities.local.DashBoardLocalActivity;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.interfaces.IUsuarioService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * @author Robert
 * @since 02/05
 */
public class StartUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private IUsuarioService usuarioService;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        inicializar();
        comprobarSesion();
    }

    /**
     * Inicializa los elementos y componentes
     */
    private void inicializar() {
        mAuth = FirebaseAuth.getInstance();
        usuarioService = ServiceFactory.provideUsuarioService(this);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Comprueba si hay una sesión activa para redirigir a su activity principal
     */
    private void comprobarSesion() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Dependiendo del tipo de usuario abre una activity u otra
            String uid = user.getUid();

            progressBar.setVisibility(VISIBLE);

            // Consultamos el tipo de usuario en la colección
            usuarioService
                    .getPerfil(uid)
                    .addOnSuccessListener(
                            usuario -> {
                                progressBar.setVisibility(GONE);
                                String rol = usuario.getRol();
                                Intent intent;

                                if (rol.equals("admin")) {
                                    intent =
                                            new Intent(
                                                    getApplicationContext(), AdminActivity.class);
                                } else if (rol.equals("local")) {
                                    intent =
                                            new Intent(
                                                    getApplicationContext(),
                                                    DashBoardLocalActivity.class);
                                } else {
                                    intent =
                                            new Intent(getApplicationContext(), HomeActivity.class);
                                }

                                cambiarActivity(intent);
                            })
                    .addOnFailureListener(
                            e -> {
                                // Si falla (sin internet, usuario borrado en BD pero no en Auth...)
                                Log.e(
                                        "StartUpActivity",
                                        "Error crítico al cargar perfil: " + e.getMessage(),
                                        e);
                                progressBar.setVisibility(GONE);
                                mAuth.signOut(); // Cerramos la sesión "corrupta"
                                irALogin();
                            });
        } else {
            // No hay sesión activa
            progressBar.setVisibility(GONE);
            irALogin();
        }
    }

    /**
     * Redirige a otra activty
     * Borra el historial de activities para que no se pueda volver atrás
     */
    private void cambiarActivity(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Redirige a la pantalla principal de registro/login
     */
    private void irALogin() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        cambiarActivity(intent);
    }
}
