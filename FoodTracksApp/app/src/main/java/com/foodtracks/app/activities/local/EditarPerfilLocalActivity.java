/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.activities.local;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.foodtracks.app.R;
import com.foodtracks.app.models.UsuarioLocal;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.exceptions.FoodTracksNotFoundException;
import com.foodtracks.app.services.exceptions.FoodTracksValidationException;
import com.foodtracks.app.services.interfaces.IUsuarioService;
import com.foodtracks.app.utils.GeolocalizacionHelper;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * @author Robert
 * @since 15/05
 */
public class EditarPerfilLocalActivity extends AppCompatActivity {

    private EditText nombre, username, ciudad, direccion, telefono, web, especifiqueOtro;
    private CheckBox vegano, vegetariano, lactosa, celiaco, otro;
    private ShapeableImageView imgFoto;
    private View layoutContenido, progress, layoutEspecifiqueOtro;

    private IUsuarioService usuarioService;
    private UsuarioLocal localActual;
    private Uri nuevaFotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil_local);

        configTheme();
        inicializar();
        cargarDatos();
        mostrarOtraPreferencia();
    }

    private void inicializar() {
        usuarioService = ServiceFactory.provideUsuarioService(this);

        nombre = findViewById(R.id.txtEditNombreLocal);
        username = findViewById(R.id.txtEditUsernameLocal);
        ciudad = findViewById(R.id.txtEditCiudadLocal);
        direccion = findViewById(R.id.txtEditDireccionLocal);
        telefono = findViewById(R.id.txtEditTelefonoLocal);
        web = findViewById(R.id.txtEditWebLocal);
        vegano = findViewById(R.id.cbEditVeganoL);
        vegetariano = findViewById(R.id.cbEditVegetarianoL);
        lactosa = findViewById(R.id.cbEditLactosaL);
        celiaco = findViewById(R.id.cbEditCeliacoL);
        otro = findViewById(R.id.cbEditOtroL);
        especifiqueOtro = findViewById(R.id.txtEditEspecifiqueOtroL);
        layoutEspecifiqueOtro = findViewById(R.id.layoutEditEspecifiqueOtroL);
        imgFoto = findViewById(R.id.imgEditarFotoLocal);
        layoutContenido = findViewById(R.id.layoutEditarLocal);
        progress = findViewById(R.id.progressEditarLocal);

        imgFoto.setOnClickListener(
                v ->
                        pickMedia.launch(
                                new PickVisualMediaRequest.Builder()
                                        .setMediaType(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                                        .INSTANCE)
                                        .build()));

        findViewById(R.id.btnGuardarCambiosLocal).setOnClickListener(v -> actualizar());
        findViewById(R.id.btnVolverSettingsL).setOnClickListener(v -> finish());
    }

    private void mostrarOtraPreferencia() {
        otro.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        layoutEspecifiqueOtro.setVisibility(View.VISIBLE);
                    } else {
                        layoutEspecifiqueOtro.setVisibility(View.GONE);
                        especifiqueOtro.setText("");
                    }
                });
    }

    private void cargarDatos() {
        String uid = FirebaseAuth.getInstance().getUid();
        usuarioService
                .getPerfil(uid)
                .addOnSuccessListener(
                        usuario -> {
                            if (usuario instanceof UsuarioLocal) {
                                localActual = (UsuarioLocal) usuario;
                                nombre.setText(localActual.getNombre());
                                username.setText(localActual.getUsername());
                                ciudad.setText(localActual.getCiudad());
                                direccion.setText(localActual.getDireccion());
                                telefono.setText(localActual.getTelefono());
                                web.setText(localActual.getSitioWeb());
                                vegano.setChecked(localActual.isEsVegano());
                                vegetariano.setChecked(localActual.isEsVegetariano());
                                lactosa.setChecked(localActual.isSinLactosa());
                                celiaco.setChecked(localActual.isEsCeliaco());

                                if (localActual.getOtraPreferencia() instanceof String otraPref
                                        && !otraPref.trim().isEmpty()) {
                                    otro.setChecked(true);
                                    especifiqueOtro.setText(otraPref);
                                    layoutEspecifiqueOtro.setVisibility(View.VISIBLE);
                                } else {
                                    otro.setChecked(false);
                                    layoutEspecifiqueOtro.setVisibility(View.GONE);
                                }

                                if (localActual.getFotoPerfil() != null) {
                                    Glide.with(this)
                                            .load(localActual.getFotoPerfil())
                                            .into(imgFoto);
                                }
                                progress.setVisibility(View.GONE);
                                layoutContenido.setVisibility(View.VISIBLE);
                            }
                        });
    }

    private void actualizar() {
        String direccionTexto = direccion.getText().toString();
        String ciudadTexto = ciudad.getText().toString();

        double[] coordenadas =
                GeolocalizacionHelper.obtenerCoordenadas(this, direccionTexto, ciudadTexto);

        if (coordenadas != null) {
            localActual.setLatitud(coordenadas[0]);
            localActual.setLongitud(coordenadas[1]);
        } else {
            Toast.makeText(this, R.string.address_not_found_error_message, Toast.LENGTH_LONG)
                    .show();
            return;
        }

        localActual.setNombre(nombre.getText().toString());
        localActual.setUsername(username.getText().toString());
        localActual.setCiudad(ciudadTexto);
        localActual.setDireccion(direccionTexto);
        localActual.setTelefono(telefono.getText().toString());
        localActual.setSitioWeb(web.getText().toString());
        localActual.setEsVegano(vegano.isChecked());
        localActual.setEsVegetariano(vegetariano.isChecked());
        localActual.setSinLactosa(lactosa.isChecked());
        localActual.setEsCeliaco(celiaco.isChecked());
        localActual.setOtraPreferencia(
                otro.isChecked() ? especifiqueOtro.getText().toString() : false);

        layoutContenido.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        usuarioService
                .actualizarPerfil(localActual, nuevaFotoUri)
                .addOnSuccessListener(
                        unused -> {
                            Toast.makeText(this, R.string.perfil_actualizado, Toast.LENGTH_SHORT)
                                    .show();
                            finish();
                        })
                .addOnFailureListener(
                        e -> {
                            progress.setVisibility(View.GONE);
                            layoutContenido.setVisibility(View.VISIBLE);
                            Log.e("Actualizar perfil LOCAL", e.getMessage(), e);
                            if (e instanceof FoodTracksValidationException ex) {
                                Toast.makeText(this, ex.getErrorResId(), Toast.LENGTH_LONG).show();
                            } else if (e instanceof FoodTracksNotFoundException ex) {
                                Toast.makeText(this, ex.getErrorResId(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(
                                                this,
                                                R.string.error_al_actualizar,
                                                Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(
                    new ActivityResultContracts.PickVisualMedia(),
                    uri -> {
                        if (uri != null) {
                            nuevaFotoUri = uri;
                            imgFoto.setImageURI(uri);
                        }
                    });

    /**
     * Configura el tema de la pantalla.
     */
    private void configTheme() {
        getWindow()
                .setStatusBarColor(
                        ContextCompat.getColor(this, R.color.fondo_perfil_local));
        getWindow()
                .setNavigationBarColor(
                        ContextCompat.getColor(this, R.color.fondo_perfil_local));
    }
}
