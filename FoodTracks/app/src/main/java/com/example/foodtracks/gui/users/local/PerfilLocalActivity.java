package com.example.foodtracks.gui.users.local;

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
public class PerfilLocalActivity extends AppCompatActivity {

    private TextView tvNombre, tvUsername, tvEmail, tvDireccion, tvTelefono, tvOpciones;
    private String uidLocal;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_local);

        asignarComponentes();
        mostrarDatosLocal();
    }

    /**
     * Asigna los componentes a la interfaz
     */
    private void asignarComponentes() {
        // Firebase
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uidLocal = mAuth.getCurrentUser().getUid();
        // TextViews
        tvNombre = findViewById(R.id.tvNombreLocal);
        tvUsername = findViewById(R.id.tvUsernameLocal);
        tvEmail = findViewById(R.id.tvEmailLocal);
        tvDireccion = findViewById(R.id.tvDirLocal);
        tvTelefono = findViewById(R.id.tvTlfLocal);
        tvOpciones = findViewById(R.id.tvOpcionesLocal);
    }


    /**
     * Mostrar los datos del local
     */
    private void mostrarDatosLocal() {
        mFirestore.collection("usuarios")
                .document(uidLocal)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) { // Verificamos que el documento exista
                        // Datos básicos
                        tvNombre.setText(document.getString("nombre"));
                        tvUsername.setText(document.getString("username"));
                        tvEmail.setText(document.getString("email"));
                        tvDireccion.setText(document.getString("direccion"));
                        tvTelefono.setText(document.getString("telefono"));

                        // Opciones alimenticias con StringBuilder
                        StringBuilder sb = new StringBuilder();

                        // Usamos Boolean.TRUE.equals para evitar NullPointerException
                        if (Boolean.TRUE.equals(document.getBoolean("esVegano")))
                            sb.append("\uD83C\uDF31 Vegano  \n");
                        if (Boolean.TRUE.equals(document.getBoolean("esVegetariano")))
                            sb.append("\uD83C\uDF3F Vegetariano  \n");
                        if (Boolean.TRUE.equals(document.getBoolean("sinLactosa")))
                            sb.append("\uD83E\uDD5B Sin Lactosa  \n");
                        if (Boolean.TRUE.equals(document.getBoolean("esCeliaco")))
                            sb.append("\uD83C\uDF3E Celíaco  \n");

                        // Manejo de otra preferencia marcada
                        Object otra = document.get("otraPreferencia");
                        if (otra instanceof String) {
                            sb.append("\uD83D\uDCDD ").append(otra.toString());
                        }

                        String resultado = sb.toString().trim();

                        // Asigna el texto depende del resultado
                        if (resultado.isEmpty()){
                            tvOpciones.setText("Ninguna seleccionada");
                        } else {
                            tvOpciones.setText(resultado);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar tu perfil", Toast.LENGTH_SHORT).show();
                });
    }

}