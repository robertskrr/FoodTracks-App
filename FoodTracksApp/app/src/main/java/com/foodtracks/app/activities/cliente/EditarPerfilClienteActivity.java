package com.foodtracks.app.activities.cliente;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.foodtracks.app.R;
import com.foodtracks.app.models.Usuario;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.exceptions.FoodTracksNotFoundException;
import com.foodtracks.app.services.exceptions.FoodTracksValidationException;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * @author Robert
 * @since 15/05
 */
public class EditarPerfilClienteActivity extends AppCompatActivity {

    private EditText txtNombre, txtUsername, txtCiudad, txtEspecifiqueOtro;
    private CheckBox cbVegano, cbVegetariano, cbLactosa, cbCeliaco, cbOtro;
    private ShapeableImageView imgFoto;
    private ProgressBar progressBar;
    private View layoutContenido, layoutEspecifiqueOtro;

    private IUsuarioService usuarioService;
    private Usuario usuarioActual;
    private Uri nuevaFotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil_cliente);

        inicializar();
        cargarDatos();
        mostrarOtraPreferencia();
    }

    private void inicializar() {
        usuarioService = ServiceFactory.provideUsuarioService(this);

        txtNombre = findViewById(R.id.txtEditNombre);
        txtUsername = findViewById(R.id.txtEditUsername);
        txtCiudad = findViewById(R.id.txtEditCiudad);
        cbVegano = findViewById(R.id.cbEditVegano);
        cbVegetariano = findViewById(R.id.cbEditVegetariano);
        cbLactosa = findViewById(R.id.cbEditLactosa);
        cbCeliaco = findViewById(R.id.cbEditCeliaco);
        cbOtro = findViewById(R.id.cbEditOtro);
        txtEspecifiqueOtro = findViewById(R.id.txtEditEspecifiqueOtro);
        layoutEspecifiqueOtro = findViewById(R.id.layoutEditEspecifiqueOtro);
        imgFoto = findViewById(R.id.imgEditarFotoCliente);
        progressBar = findViewById(R.id.progressEditarCliente);
        layoutContenido = findViewById(R.id.layoutEditarCliente);

        imgFoto.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));

        findViewById(R.id.btnGuardarCambiosCliente).setOnClickListener(v -> guardar());
        findViewById(R.id.btnVolverSettings).setOnClickListener(v -> finish());
    }

    private void mostrarOtraPreferencia() {
        cbOtro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutEspecifiqueOtro.setVisibility(View.VISIBLE);
            } else {
                layoutEspecifiqueOtro.setVisibility(View.GONE);
                txtEspecifiqueOtro.setText("");
            }
        });
    }

    private void cargarDatos() {
        String uid = FirebaseAuth.getInstance().getUid();
        usuarioService.getPerfil(uid).addOnSuccessListener(usuario -> {
            usuarioActual = usuario;
            txtNombre.setText(usuario.getNombre());
            txtUsername.setText(usuario.getUsername());
            txtCiudad.setText(usuario.getCiudad());
            cbVegano.setChecked(usuario.isEsVegano());
            cbVegetariano.setChecked(usuario.isEsVegetariano());
            cbLactosa.setChecked(usuario.isSinLactosa());
            cbCeliaco.setChecked(usuario.isEsCeliaco());

            // Si hay otra preferencia escrita, la mostramos
            if (usuario.getOtraPreferencia() instanceof String otra && !otra.trim().isEmpty()) {
                cbOtro.setChecked(true);
                txtEspecifiqueOtro.setText(otra);
                layoutEspecifiqueOtro.setVisibility(View.VISIBLE);
            } else {
                cbOtro.setChecked(false);
                layoutEspecifiqueOtro.setVisibility(View.GONE);
            }

            if (usuario.getFotoPerfil() != null) {
                Glide.with(this).load(usuario.getFotoPerfil()).into(imgFoto);
            }

            progressBar.setVisibility(View.GONE);
            layoutContenido.setVisibility(View.VISIBLE);
        });
    }

    private void guardar() {
        usuarioActual.setNombre(txtNombre.getText().toString());
        usuarioActual.setUsername(txtUsername.getText().toString());
        usuarioActual.setCiudad(txtCiudad.getText().toString());
        usuarioActual.setEsVegano(cbVegano.isChecked());
        usuarioActual.setEsVegetariano(cbVegetariano.isChecked());
        usuarioActual.setSinLactosa(cbLactosa.isChecked());
        usuarioActual.setEsCeliaco(cbCeliaco.isChecked());

        usuarioActual.setOtraPreferencia(cbOtro.isChecked() ? txtEspecifiqueOtro.getText().toString() : false);

        layoutContenido.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        usuarioService.actualizarPerfil(usuarioActual, nuevaFotoUri)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, R.string.perfil_actualizado, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    layoutContenido.setVisibility(View.VISIBLE);
                    Log.e("Actualizar perfil CLIENTE", e.getMessage(), e);
                    if (e instanceof FoodTracksValidationException ex) {
                        Toast.makeText(this, ex.getErrorResId(), Toast.LENGTH_LONG).show();
                    } else if (e instanceof FoodTracksNotFoundException ex) {
                        Toast.makeText(this, ex.getErrorResId(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, R.string.error_al_actualizar, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    nuevaFotoUri = uri;
                    imgFoto.setImageURI(uri);
                }
            });
}