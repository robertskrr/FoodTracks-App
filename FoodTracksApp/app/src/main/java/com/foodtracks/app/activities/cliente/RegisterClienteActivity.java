/**
 * © FoodTracks Project ===robertskrr===
 */

package com.foodtracks.app.activities.cliente;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.foodtracks.app.R;

import com.foodtracks.app.models.UsuarioCliente;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.exceptions.UsuarioValidationException;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;


/**
 * @author Robert
 * @since 01/02
 */
public class RegisterClienteActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private IUsuarioService usuarioService;

    // Elementos de registro
    private EditText nombre, username, email, password, confirmPassword, ciudad, especifiqueOtro;
    private CheckBox esVegano, esVegetariano, sinLactosa, esCeliaco, otraPreferencia;
    private String nombreAvatar;
    ShapeableImageView fotoPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_cliente);

        inicializar();
        mostrarOtraPreferencia();
    }


    /**
     * Acción al pulsar el registrar.
     *
     * @param view Vista de registro.
     */
    public void registroCliente(View view) {
        String emailReg = email.getText().toString().trim();
        String passwordReg = password.getText().toString().trim();
        String confirmReg = confirmPassword.getText().toString().trim();

        // Validación de credenciales
        int errorResId = usuarioService.validarCredenciales(emailReg, passwordReg, confirmReg);

        if (errorResId != 0) {
            Toast.makeText(getApplicationContext(), errorResId, Toast.LENGTH_SHORT).show();
            return;
        }

        // Llamada a Firebase Auth
        mAuth.createUserWithEmailAndPassword(emailReg, passwordReg)
                .addOnSuccessListener(authResult -> {
                    if (authResult.getUser() != null) {
                        procesarRegistro(authResult.getUser().getUid());
                    } else {
                        Toast.makeText(this, R.string.register_critic_error_message, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, R.string.registered_email_error_message, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, R.string.create_account_error_message + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Proceso de registro del usuario con el servicio.
     *
     * @param uid UID del usuario.
     */
    private void procesarRegistro(String uid) {
        UsuarioCliente newUsuario = UsuarioCliente.builder()
                .uid(uid)
                .nombre(nombre.getText().toString())
                .username(username.getText().toString())
                .email(email.getText().toString())
                .ciudad(ciudad.getText().toString())
                .esVegano(esVegano.isChecked())
                .esVegetariano(esVegetariano.isChecked())
                .sinLactosa(sinLactosa.isChecked())
                .esCeliaco(esCeliaco.isChecked())
                .otraPreferencia(otraPreferencia.isChecked() ? especifiqueOtro.getText().toString() : false)
                .build();

        usuarioService.registrarUsuario(newUsuario, nombreAvatar)
                .addOnSuccessListener(unused -> {
                    irAHome();
                })
                .addOnFailureListener(e -> {
                    if (mAuth.getCurrentUser() != null) {
                        String uidDelete = mAuth.getCurrentUser().getUid();

                        mAuth.getCurrentUser().delete() // Rollback: Elimina al usuario creado en Auth
                                .addOnCompleteListener(task -> {
                                    if (e instanceof UsuarioValidationException ex) {
                                        Toast.makeText(this, ex.getErrorResId(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, R.string.register_critic_error_message + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }

    /**
     * Inicializa los elementos y componentes
     */
    private void inicializar() {
        mAuth = FirebaseAuth.getInstance();
        usuarioService = ServiceFactory.provideUsuarioService();

        // Campos del usuario
        fotoPerfil = findViewById(R.id.imgPerfil);
        nombre = findViewById(R.id.txtNombre);
        username = findViewById(R.id.txtUsername);
        email = findViewById(R.id.txtEmail);
        password = findViewById(R.id.txtPassword);
        confirmPassword = findViewById(R.id.txtConfirmPassword);
        ciudad = findViewById(R.id.txtCiudad);

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
        otraPreferencia.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        especifiqueOtro.setVisibility(View.VISIBLE);
                    } else {
                        especifiqueOtro.setVisibility(View.GONE);
                        especifiqueOtro.setText("");
                    }
                });
    }

    /**
     * Navega a la activity Home
     */
    private void irAHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}


