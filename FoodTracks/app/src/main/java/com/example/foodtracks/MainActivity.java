package com.example.foodtracks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodtracks.gui.HomeActivity;
import com.example.foodtracks.gui.RegisterClienteActivity;
import com.example.foodtracks.gui.fragments.LoginFragment;
import com.example.foodtracks.gui.fragments.TipoRegistroFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * @author Robert
 * @since 20/01
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore mFirestore; // TRABAJARA CON LOS DATOS
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        asignarComponentes();
    }

    /**
     * Asigna los componentes de la interfaz
     */
    private void asignarComponentes() {
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Muestra el fragment con los datos de inicio de sesión
     * @param view
     */
    public void login(View view){
        LoginFragment fm = new LoginFragment();
        fm.show(getSupportFragmentManager(), "Fragment login");
    }

    /**
     * Muestra las opciones de registro en un fragment
     * @param view
     */
    public void register(View view){
        TipoRegistroFragment fm = new TipoRegistroFragment();
        fm.show(getSupportFragmentManager(), "Fragment registro");
    }


    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            // Limpiamos historial de activities para que no pueda volver atrás
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Cerramos la activity
        }
    }

}