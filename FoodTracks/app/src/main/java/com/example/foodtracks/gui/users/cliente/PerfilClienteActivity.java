package com.example.foodtracks.gui.users.cliente;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodtracks.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * @author Robert
 * @since 18/02
 */
public class PerfilClienteActivity extends AppCompatActivity {

    private TextView tvNombre, tvUsername, tvEmail, tvPreferencias;
    private String uidCliente;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_cliente);

        asignarComponentes();
        mostrarDatosCliente();
    }

    /**
     * Asigna los componentes a la interfaz
     */
    private void asignarComponentes() {
        // Firebase
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uidCliente = mAuth.getCurrentUser().getUid();

        // TextView
        tvNombre = findViewById(R.id.tvNombreCliente);
        tvUsername = findViewById(R.id.tvUsernameCliente);
        tvEmail = findViewById(R.id.tvEmailCliente);
        tvPreferencias = findViewById(R.id.tvPrefCliente);
    }

    /**
     * Muestra los datos del cliente
     */
    private void mostrarDatosCliente() {
        mFirestore.collection("usuarios")
                .document(uidCliente)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Datos básicos
                        tvNombre.setText(document.getString("nombre"));
                        tvUsername.setText(document.getString("username"));
                        tvEmail.setText(document.getString("email"));

                        // Preferencias
                        StringBuilder sb = new StringBuilder();

                        if (Boolean.TRUE.equals(document.getBoolean("esVegano")))
                            sb.append("\uD83C\uDF31 Vegano  \n");
                        if (Boolean.TRUE.equals(document.getBoolean("esVegetariano")))
                            sb.append("\uD83C\uDF3F Vegetariano  \n");
                        if (Boolean.TRUE.equals(document.getBoolean("sinLactosa")))
                            sb.append("\uD83E\uDD5B Sin Lactosa  \n");
                        if (Boolean.TRUE.equals(document.getBoolean("esCeliaco")))
                            sb.append("\uD83C\uDF3E Celíaco  \n");

                        // Manejo de "otraPreferencia"
                        Object otra = document.get("otraPreferencia");
                        if (otra instanceof String) {
                            sb.append("\uD83D\uDCDD ").append(otra.toString());
                        }

                        String resultado = sb.toString().trim();

                        if (resultado.isEmpty()) {
                            tvPreferencias.setText("Ninguna");
                        } else {
                            tvPreferencias.setText(resultado);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar tu perfil", Toast.LENGTH_SHORT).show();
                });
    }
}