/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.foodtracks.app.R;
import com.foodtracks.app.activities.MainActivity;
import com.foodtracks.app.fragments.BusquedaFragment;
import com.foodtracks.app.fragments.FeedFragment;
import com.foodtracks.app.fragments.SettingsFragment;
import com.foodtracks.app.fragments.SubirPublicacionFragment;
import com.foodtracks.app.fragments.cliente.PerfilClienteFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * @author Robert
 * @since 17/02
 */
public class MainClienteActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_cliente);

        configTheme();
        inicializar();
        mostrarInterfazCliente();

        // Cargar un fragmento por defecto en home al iniciar si no hay estado previo guardado
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    /**
     * Asigna los componentes de la interfaz.
     */
    private void inicializar() {
        mAuth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottomNavigation);
    }

    /**
     * Muestra la barra de navegación configurada.
     */
    private void mostrarInterfazCliente() {
        bottomNavigationView.setVisibility(View.VISIBLE);
        configurarNavegacion();
    }

    /**
     * Motor para intercambiar fragmentos en el FrameLayout central
     */
    private void cargarFragmento(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorPrincipalCliente, fragment)
                .commit();
    }

    /**
     * Configura la barra de navegación.
     * Cambia el FrameLayout central por el fragment concreto.
     */
    private void configurarNavegacion() {
        bottomNavigationView.setOnItemSelectedListener(
                item -> {
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_home) {
                        cargarFragmento(new FeedFragment());
                        return true;

                    } else if (itemId == R.id.nav_busqueda) {
                        cargarFragmento(new BusquedaFragment());
                        return true;

                    } else if (itemId == R.id.nav_publicar) {
                        if (esInvitado()) {
                            Toast.makeText(
                                            this,
                                            R.string.inicia_sesion_para_usar_esto,
                                            Toast.LENGTH_SHORT)
                                    .show();
                            return false; // Bloquea la navegación
                        }

                        SubirPublicacionFragment fm = new SubirPublicacionFragment();
                        fm.show(getSupportFragmentManager(), "Fragment publicacion");
                        return false;

                    } else if (itemId == R.id.nav_perfil) {
                        if (esInvitado()) {
                            Toast.makeText(
                                            this,
                                            R.string.inicia_sesion_para_usar_esto,
                                            Toast.LENGTH_SHORT)
                                    .show();
                            return false;
                        }

                        // Cambia el centro de la pantalla al fragmento de perfil
                        cargarFragmento(new PerfilClienteFragment());
                        return true;

                    } else if (itemId == R.id.nav_ajustes) {
                        cargarFragmento(new SettingsFragment());
                        return true;
                    }

                    return false;
                });
    }

    /**
     * Revisa si el usuario que accede es invitado o no.
     * @return true si es invitado.
     */
    private boolean esInvitado() {
        return mAuth.getCurrentUser() == null;
    }

    /**
     * Cierra la sesión del usuario y lo devuelve a la pantalla de inicio.
     */
    private void logOut() {
        if (!esInvitado()) {
            mAuth.signOut();
        }
        Intent intent = new Intent(MainClienteActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(getApplicationContext(), R.string.despedida_app, Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }

    private void configTheme() {
        getWindow()
                .setStatusBarColor(
                        androidx.core.content.ContextCompat.getColor(this, R.color.fondo));
        getWindow()
                .setNavigationBarColor(
                        androidx.core.content.ContextCompat.getColor(this, R.color.secondary));
    }
}
