package com.example.foodtracks.gui;

import android.content.Intent;
import android.os.Bundle;

import com.example.foodtracks.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert
 * @since 01/02
 */
public class RegisterClienteActivity extends AppCompatActivity {
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void asignarComponentes() {
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void registrarUsuario(String nombre, String userName, String userEmail, String userPass) {
        mAuth.createUserWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // Recogemos el UID del usuario
                String uid = mAuth.getCurrentUser().getUid();
                // Mapa con los datos del usuario para la BBDD
                Map<String, Object> map = new HashMap<>(); // <Nombre campo, valor>
                map.put("id", uid);
                map.put("nombre", nombre);
                map.put("username", userName);
                map.put("email", userEmail);
                map.put("password", userPass);
                // Lo guardamos en la colección de clientes
                mFirestore.collection("usuarioCliente")
                        .document(uid)
                        .set(map, SetOptions.merge())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                finish();
                                startActivity(new Intent(RegisterClienteActivity.this, MainActivity.class));
                                Toast.makeText(RegisterClienteActivity.this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RegisterClienteActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

}