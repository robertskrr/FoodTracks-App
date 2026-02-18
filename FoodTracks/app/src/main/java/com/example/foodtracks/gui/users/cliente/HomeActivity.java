package com.example.foodtracks.gui.users.cliente;

import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodtracks.MainActivity;
import com.example.foodtracks.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * @author Robert
 * @since 17/02
 */
public class HomeActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    private Button btnLogOut;
    private Button btnPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        asignarComponentes();
        mostrarInterfaz();
    }

    /**
     * Asigna los componentes de la interfaz
     */
    private void asignarComponentes(){
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        btnLogOut = findViewById(R.id.btnLogOutCliente);
        btnPerfil = findViewById(R.id.btnPerfilCliente);
    }

    /**
     * Si no es un invitado muestra la interfaz de usuario
     */
    private void mostrarInterfaz(){
        if (mAuth.getCurrentUser() != null){
            btnLogOut.setVisibility(View.VISIBLE);
            btnPerfil.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Cierra la sesión del usuario
     * @param view
     */
    public void logOut(View view){
        mAuth.signOut();
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);

        // Limpiamos historial de activities para que no pueda volver atrás
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(getApplicationContext(), "¡Hasta pronto!", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }

    /**
     * Visualizar el perfil
     * @param view
     */
    public void miPerfil(View view){
        startActivity(new Intent(getApplicationContext(), PerfilClienteActivity.class));
    }

}