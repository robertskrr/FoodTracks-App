/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.foodtracks.app.R;
import com.foodtracks.app.activities.MainActivity;
import com.foodtracks.app.fragments.SubirPublicacionFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * @author Robert
 * @since 17/02
 */
public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        asignarComponentes();
        mostrarInterfazCliente();
    }

    private void asignarComponentes() {
        mAuth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottomNavigation);
    }

    /** Si no es un invitado, muestra la barra y la configura */
    private void mostrarInterfazCliente() {
        if (mAuth.getCurrentUser() != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
            configurarNavegacion();
        } else {
            // Si entra un invitado, ocultamos la barra
            bottomNavigationView.setVisibility(View.GONE);
        }
    }

    private void configurarNavegacion() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // TODO: Cargar el Feed principal (publicaciones recientes)
                return true; // Devuelve true para que se marque visualmente

            } else if (itemId == R.id.nav_busqueda) {
                // TODO: Cargar el fragmento de Búsqueda de locales/usuarios
                Toast.makeText(this, "Sección de Búsqueda", Toast.LENGTH_SHORT).show();
                return true;

            } else if (itemId == R.id.nav_publicar) {
                SubirPublicacionFragment fm = new SubirPublicacionFragment();
                fm.show(getSupportFragmentManager(), "Fragment publicacion");
                return false;

            } else if (itemId == R.id.nav_perfil) {
                startActivity(new Intent(getApplicationContext(), PerfilClienteActivity.class));
                return false;

            } else if (itemId == R.id.nav_ajustes) {
                logOut();
                return false;
            }

            return false;
        });
    }

    /**
     * Cierra la sesión del usuario
     */
    private void logOut() {
        mAuth.signOut();
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(getApplicationContext(), R.string.despedida_app, Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }
}