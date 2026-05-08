/**
 * © FoodTracks Project ===robertskrr===
 */

package com.foodtracks.app.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.foodtracks.app.R;
import com.foodtracks.app.activities.MainActivity;
import com.foodtracks.app.fragments.cliente.PerfilClienteFragment;
import com.foodtracks.app.fragments.SubirPublicacionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * @author Robert
 * @since 17/02
 */
public class MainAdminActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        configTheme();
        inicializar();
        mostrarInterfazAdmin();

        if (savedInstanceState == null && mAuth.getCurrentUser() != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    /**
     * Asigna los componentes a la interfaz.
     */
    private void inicializar() {
        mAuth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottomNavigation);
    }

    /**
     * Muestra la barra de navegación configurada.
     */
    private void mostrarInterfazAdmin() {
        bottomNavigationView.setVisibility(View.VISIBLE);
        configurarNavegacion();
    }

    /**
     * Motor para intercambiar fragmentos en el FrameLayout central
     */
    private void cargarFragmento(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorPrincipalAdmin, fragment)
                .commit();
    }

    /**
     * Configura la barra de navegación.
     * Cambia el FrameLayout central por el fragment concreto.
     */
    private void configurarNavegacion() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // TODO: cargarFragmento(new FeedFragment());
                return true;

            } else if (itemId == R.id.nav_admin_panel) {
                // TODO: cargarFragmento(new AdminPanelFragment());
                return true;
            } else if (itemId == R.id.nav_busqueda) {
                // TODO: cargarFragmento(new BusquedaFragment());
                return true;

            } else if (itemId == R.id.nav_publicar) {
                SubirPublicacionFragment fm = new SubirPublicacionFragment();
                fm.show(getSupportFragmentManager(), "Fragment publicacion");
                return false;

            } else if (itemId == R.id.nav_perfil) {
                cargarFragmento(new PerfilClienteFragment());
                return true;

            } else if (itemId == R.id.nav_ajustes) {
                logOut();
                return false;
            }

            return false;
        });
    }

    private void logOut() {
        mAuth.signOut();
        Intent intent = new Intent(MainAdminActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(getApplicationContext(), R.string.despedida_app, Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }

    private void configTheme() {
        getWindow()
                .setStatusBarColor(
                        androidx.core.content.ContextCompat.getColor(this, R.color.black));
        getWindow()
                .setNavigationBarColor(
                        androidx.core.content.ContextCompat.getColor(this, R.color.admin_bottom_nav));
    }
}