package com.example.foodtracks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodtracks.gui.users.admin.AdminActivity;
import com.example.foodtracks.gui.users.cliente.HomeActivity;
import com.example.foodtracks.gui.fragments.LoginFragment;
import com.example.foodtracks.gui.fragments.TipoRegistroFragment;
import com.example.foodtracks.gui.users.local.DashBoardLocalActivity;
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

    /**
     * Acceso como invitado
     * @param view
     */
    public void invitado(View view){
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
    }


    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            // Dependiendo del tipo de usuario abre una activity u otra
            String uid = mAuth.getCurrentUser().getUid();
            // Consultamos el tipo de usuario en la colección
            mFirestore.collection("usuarios")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(document -> {
                        String rol = document.getString("rol");
                        Intent intent;

                        if (rol.equals("admin")) {
                            intent = new Intent(getApplicationContext(), AdminActivity.class);
                        } else if (rol.equals("local")) {
                            intent = new Intent(getApplicationContext(), DashBoardLocalActivity.class);
                        } else {
                            intent = new Intent(getApplicationContext(), HomeActivity.class);
                        }

                        // Limpiamos historial de activities para que no pueda volver atrás
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
        }
    }

}