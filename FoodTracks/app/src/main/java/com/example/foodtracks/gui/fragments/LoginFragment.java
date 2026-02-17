package com.example.foodtracks.gui.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodtracks.MainActivity;
import com.example.foodtracks.R;
import com.example.foodtracks.gui.HomeActivity;
import com.example.foodtracks.gui.RegisterClienteActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


/**
 * @author Robert
 * @since  17/02
 */
public class LoginFragment extends DialogFragment {

    private EditText email, password;
    private TextView irARegistro;
    private Button btnLogin;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        btnLogin = v.findViewById(R.id.btnAcceder);
        email = v.findViewById(R.id.txtLoginEmail);
        password = v.findViewById(R.id.txtLoginPassword);
        irARegistro = v.findViewById(R.id.txtVolverARegistro);

        mAuth = FirebaseAuth.getInstance();

        irARegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cerramos fragment
                dismiss();

                // Abrimos fragmento de registro
                TipoRegistroFragment registroFragment = new TipoRegistroFragment();
                registroFragment.show(getParentFragmentManager(), "Fragment registro");
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailLogin = email.getText().toString().trim();
                String passwordLogin = password.getText().toString().trim();

                if (emailLogin.isEmpty() || passwordLogin.isEmpty()) {
                    Toast.makeText(getContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginUser(emailLogin, passwordLogin);
            }
        });

        return v;
    }

    private void loginUser(String emailLogin, String passwordLogin) {
        mAuth.signInWithEmailAndPassword(emailLogin, passwordLogin).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    // Limpiamos historial de activities para que no pueda volver atrás
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    dismiss();
                    startActivity(intent);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Elimina el recuadro blanco que Android pone por defecto detrás del fragment
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            // Obtenemos el ancho de la pantalla actual
            int width = getResources().getDisplayMetrics().widthPixels;
            // Calculamos el 85% del ancho
            int desiredWidth = (int) (width * 0.85);
            // Lo asignamos al dialog del fragmento
            getDialog().getWindow().setLayout(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}