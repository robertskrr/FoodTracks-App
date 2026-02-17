package com.example.foodtracks.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodtracks.MainActivity;
import com.example.foodtracks.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * @author Robert
 * @since 17/02
 */
public class HomeActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        asignarComponentes();
    }

    private void asignarComponentes(){
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
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
        Toast.makeText(this, "¡Hasta pronto!", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }

    public void miPerfil(View view){

    }

}