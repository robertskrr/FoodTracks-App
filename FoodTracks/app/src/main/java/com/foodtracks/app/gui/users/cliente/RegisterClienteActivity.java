package com.foodtracks.app.gui.users.cliente;

import android.content.Intent;
import android.os.Bundle;

import com.foodtracks.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
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
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    // Elementos de registro
    private EditText nombre, username, email, password, especifiqueOtro;

    private CheckBox esVegano, esVegetariano, sinLactosa, esCeliaco, otraPreferencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_cliente);

        asignarComponentes();
        mostrarOtraPreferencia();
    }

    /**
     * Asigna los componentes de la interfaz
     */
    private void asignarComponentes() {
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Campos
        nombre = findViewById(R.id.txtNombre);
        username = findViewById(R.id.txtUsername);
        email = findViewById(R.id.txtEmail);
        password = findViewById(R.id.txtPassword);

        // Preferencias alimenticias
        esVegano = findViewById(R.id.cbVegano);
        esVegetariano = findViewById(R.id.cbVegetariano);
        sinLactosa = findViewById(R.id.cbLactosa);
        esCeliaco = findViewById(R.id.cbCeliaco);
        otraPreferencia = findViewById(R.id.cbOtro);
        especifiqueOtro = findViewById(R.id.txtEspecifiqueOtro);
    }

    /**
     * Si se marca otra preferencia muestra el campo de texto para escribirla
     */
    private void mostrarOtraPreferencia() {
        otraPreferencia.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                especifiqueOtro.setVisibility(View.VISIBLE);
            } else {
                especifiqueOtro.setVisibility(View.GONE);
                especifiqueOtro.setText("");
            }
        });
    }

    /**
     * Acción al pulsar el registrar
     *
     * @param view
     */
    public void registroCliente(View view) {
        String nombreReg = nombre.getText().toString().trim();
        String usernameReg = username.getText().toString().trim();
        String emailReg = email.getText().toString().trim();
        String passwordReg = password.getText().toString().trim();

        // Si faltan campos por rellenar no registra
        if (nombreReg.isEmpty() || usernameReg.isEmpty() || emailReg.isEmpty() || passwordReg.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passwordReg.length() < 8) {
            Toast.makeText(getApplicationContext(), "La contraseña debe tener mínimo 8 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Registramos el usuario en firebase
        registrarUsuarioCliente(nombreReg, usernameReg, emailReg, passwordReg);
    }

    /**
     * Registra el usuario cliente en la base de datos de Firebase
     *
     * @param nombre
     * @param userName
     * @param userEmail
     * @param userPassword
     */
    private void registrarUsuarioCliente(String nombre, String userName, String userEmail, String userPassword) {
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // Si se completó correctamente guardamos los datos del nuevo usuario
                if (task.isSuccessful()) {
                    // Recogemos el UID del usuario
                    String uid = mAuth.getCurrentUser().getUid();
                    // Mapa con los datos del usuario para la BBDD
                    Map<String, Object> map = new HashMap<>(); // <Nombre campo, valor>
                    map.put("uid", uid);
                    map.put("nombre", nombre);
                    map.put("username", userName);
                    map.put("email", userEmail);
                    map.put("rol", "cliente");

                    // Preferencias alimenticias
                    map.put("esVegano", esVegano.isChecked());
                    map.put("esVegetariano", esVegetariano.isChecked());
                    map.put("sinLactosa", sinLactosa.isChecked());
                    map.put("esCeliaco", esCeliaco.isChecked());
                    if (otraPreferencia.isChecked()) {
                        String preferenciaAlternativa = especifiqueOtro.getText().toString().trim();
                        // Si ha marcado la casilla pero no ha puesto nada se da por hecho que no tiene otra
                        if (preferenciaAlternativa.isEmpty()) {
                            map.put("otraPreferencia", false);
                        } else {
                            map.put("otraPreferencia", preferenciaAlternativa.toLowerCase());
                        }
                    } else {
                        map.put("otraPreferencia", false);
                    }

                    // Lo guardamos en la colección de clientes
                    mFirestore.collection("usuarios")
                            .document(uid)
                            .set(map, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(), "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    // Limpiamos historial de activities para que no pueda volver atrás
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish(); // Cerramos la activity
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Error al registrar", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error al crear la cuenta de usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

}