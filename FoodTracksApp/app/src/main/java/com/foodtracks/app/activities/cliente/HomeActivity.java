/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.foodtracks.app.R;
import com.foodtracks.app.activities.MainActivity;
import com.foodtracks.app.fragments.SubirPublicacionFragment;

import com.google.firebase.auth.FirebaseAuth;

/**
 * @author Robert
 * @since 17/02
 */
public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Button btnLogOut, btnPerfil, btnSubirPublicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        asignarComponentes();
        mostrarInterfazCliente();
    }

    /** Asigna los componentes de la interfaz */
    private void asignarComponentes() {
        mAuth = FirebaseAuth.getInstance();
        btnLogOut = findViewById(R.id.btnLogOutCliente);
        btnPerfil = findViewById(R.id.btnPerfilCliente);
        btnSubirPublicacion = findViewById(R.id.btnPublicacionCliente);
    }

    /** Si no es un invitado muestra la interfaz de usuario */
    private void mostrarInterfazCliente() {
        if (mAuth.getCurrentUser() != null) {
            btnLogOut.setVisibility(View.VISIBLE);
            btnPerfil.setVisibility(View.VISIBLE);
            btnSubirPublicacion.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Cierra la sesión del usuario
     *
     * @param view
     */
    public void logOut(View view) {
        mAuth.signOut();
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);

        // Limpiamos historial de activities para que no pueda volver atrás
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(getApplicationContext(), R.string.despedida_app, Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }

    /**
     * Visualizar el perfil
     *
     * @param view
     */
    public void miPerfil(View view) {
        startActivity(new Intent(getApplicationContext(), PerfilClienteActivity.class));
    }

    /**
     * Muestra el fragment para subir una publicación
     *
     * @param view
     */
    public void publicar(View view) {
        SubirPublicacionFragment fm = new SubirPublicacionFragment();
        fm.show(getSupportFragmentManager(), "Fragment publicacion");
    }
}
