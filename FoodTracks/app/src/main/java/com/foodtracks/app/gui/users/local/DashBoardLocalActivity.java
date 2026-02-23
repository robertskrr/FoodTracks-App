package com.foodtracks.app.gui.users.local;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.foodtracks.app.MainActivity;
import com.foodtracks.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * @author Robert
 * @since 17/02
 */
public class DashBoardLocalActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    private TextView txtLocal;

    private String uidLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dash_board_local);

        asignarComponentes();
        mostrarDatosLocal();
    }

    /**
     * Asigna los componentes de la interfaz
     */
    private void asignarComponentes() {
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        txtLocal = findViewById(R.id.txtLocal);
        uidLocal = mAuth.getCurrentUser().getUid();
    }

    /**
     * Muestra los datos del local en el Dash Board
     */
    private void mostrarDatosLocal() {
        ponerNombreEnTexto(txtLocal);
    }

    /**
     * Pone el nombre del local en el TextView
     *
     * @param tv
     */
    private void ponerNombreEnTexto(TextView tv) {
        mFirestore.collection("usuarios")
                .document(uidLocal)
                .get()
                .addOnSuccessListener(document -> {
                    tv.setText(document.getString("nombre"));
                });
    }

    /**
     * Cierra la sesión del usuario
     *
     * @param view
     */
    public void logOut(View view) {
        mAuth.signOut();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        // Limpiamos historial de activities para que no pueda volver atrás
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(getApplicationContext(), "¡Hasta pronto!", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }

    /**
     * Visualizar el perfil
     *
     * @param view
     */
    public void miPerfil(View view) {
        startActivity(new Intent(getApplicationContext(), PerfilLocalActivity.class));
    }
}