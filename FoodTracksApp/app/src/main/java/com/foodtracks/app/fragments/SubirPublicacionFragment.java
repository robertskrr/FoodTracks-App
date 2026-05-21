/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.foodtracks.app.R;
import com.foodtracks.app.models.Publicacion;
import com.foodtracks.app.models.Usuario;
import com.foodtracks.app.services.ServiceFactory;
import com.foodtracks.app.services.exceptions.FoodTracksValidationException;
import com.foodtracks.app.services.interfaces.IPublicacionService;
import com.foodtracks.app.services.interfaces.IUsuarioService;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

/**
 * @author Robert
 * @since 07/05
 */
public class SubirPublicacionFragment extends DialogFragment {

    private TextView tvUsernameAutor;
    private TextInputEditText txtTextoPublicacion;
    private MaterialAutoCompleteTextView txtMencionarLocal;
    private TextInputLayout layoutMencionarLocal;
    private ArrayAdapter<String> adapterLocales;
    private final List<String> sugerenciasLocales = new ArrayList<>();
    private ShapeableImageView imgVistaPreviaFoto;
    private Button btnAdjuntarFoto, btnPublicar;

    private FirebaseAuth mAuth;
    private IUsuarioService usuarioService;
    private IPublicacionService publicacionService;

    private String uidUsuarioActual;
    private Uri uriFotoSeleccionada = null;
    private Uri uriFotoCamaraTemporal = null;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subir_publicacion, container, false);
        inicializar(v);
        cargarDatosAutor();
        configurarListeners();
        return v;
    }

    /**
     * Asigna los componentes a la interfaz
     * @param v Vista del fragmento
     */
    private void inicializar(View v) {
        tvUsernameAutor = v.findViewById(R.id.tvUsernameAutor);
        txtTextoPublicacion = v.findViewById(R.id.txtTextoPublicacion);
        txtMencionarLocal = v.findViewById(R.id.txtMencionarLocal);
        layoutMencionarLocal = v.findViewById(R.id.layoutMencionarLocal);
        imgVistaPreviaFoto = v.findViewById(R.id.imgVistaPreviaFoto);
        btnAdjuntarFoto = v.findViewById(R.id.btnAdjuntarFoto);
        btnPublicar = v.findViewById(R.id.btnPublicar);
        // Configuramos el adaptador para que muestre exactamente lo que traemos
        adapterLocales =
                new ArrayAdapter<String>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        sugerenciasLocales) {
                    @NonNull
                    @Override
                    public Filter getFilter() {
                        return new Filter() {
                            @Override
                            protected FilterResults performFiltering(CharSequence constraint) {
                                FilterResults results = new FilterResults();
                                results.values = sugerenciasLocales;
                                results.count = sugerenciasLocales.size();
                                return results;
                            }

                            @Override
                            protected void publishResults(
                                    CharSequence constraint, FilterResults results) {
                                notifyDataSetChanged();
                            }
                        };
                    }
                };
        txtMencionarLocal.setAdapter(adapterLocales);

        mAuth = FirebaseAuth.getInstance();
        uidUsuarioActual = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        usuarioService = ServiceFactory.provideUsuarioService(requireContext());
        publicacionService = ServiceFactory.providePublicacionService(requireContext());
    }

    /**
     * Consulta el perfil del usuario actual para mostrar su username y configurar la vista según su rol.
     */
    private void cargarDatosAutor() {
        if (uidUsuarioActual == null) {
            dismiss();
            return;
        }

        usuarioService
                .getPerfil(uidUsuarioActual)
                .addOnSuccessListener(
                        usuario -> {
                            tvUsernameAutor.setText("@" + usuario.getUsername());

                            // Solo muestra el layout de mención si no es un local
                            if (!"local".equals(usuario.getRol())) {
                                layoutMencionarLocal.setVisibility(View.VISIBLE);
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            tvUsernameAutor.setText(R.string.usuario_desconocido);
                        });
    }

    /**
     * Lanzador para abrir la galería.
     * Muestra la imagen seleccionada.
     */
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            uriFotoSeleccionada = uri;
                            imgVistaPreviaFoto.setImageURI(uri);
                            imgVistaPreviaFoto.setVisibility(View.VISIBLE);
                        }
                    });

    /**
     * Lanzador para abrir la cámara.
     */
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    success -> {
                        if (success && uriFotoCamaraTemporal != null) {
                            uriFotoSeleccionada = uriFotoCamaraTemporal;
                            imgVistaPreviaFoto.setImageURI(uriFotoSeleccionada);
                            imgVistaPreviaFoto.setVisibility(View.VISIBLE);
                        }
                    });

    /**
     * Lanzador para pedir el permiso de la cámara.
     */
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            abrirCamara();
                        } else {
                            Toast.makeText(
                                            getContext(),
                                            R.string.camera_permissions_error,
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });

    /**
     * Configura los listeners de los componentes de la interfaz.
     */
    private void configurarListeners() {
        btnAdjuntarFoto.setOnClickListener(v -> mostrarOpcionesImagen());

        // Muestra una lista desplegable con las coincidencias de locales
        txtMencionarLocal.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (txtMencionarLocal.isPerformingCompletion()) return;

                        String query = s.toString().trim();
                        String cleanQuery = query.replace("@", "");

                        // A partir de 2 caracteres limpios busca
                        if (!cleanQuery.isEmpty()) {
                            usuarioService
                                    .buscarLocalesPorUsername(cleanQuery)
                                    .addOnSuccessListener(
                                            locales -> {
                                                if (!isAdded()) return;
                                                sugerenciasLocales.clear();
                                                for (Usuario local : locales) {
                                                    sugerenciasLocales.add(
                                                            "@" + local.getUsername());
                                                }

                                                // Actualizamos la lista
                                                adapterLocales.notifyDataSetChanged();

                                                // Forzamos a abrir el menú cuando lleguen los datos
                                                if (!sugerenciasLocales.isEmpty()) {
                                                    txtMencionarLocal.showDropDown();
                                                }
                                            });
                        } else {
                            // Si borra vaciamos la lista
                            sugerenciasLocales.clear();
                            adapterLocales.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

        btnPublicar.setOnClickListener(
                v -> {
                    String texto =
                            txtTextoPublicacion.getText() != null
                                    ? txtTextoPublicacion.getText().toString().trim()
                                    : "";
                    String localMencionado =
                            txtMencionarLocal.getText() != null
                                    ? txtMencionarLocal.getText().toString().trim()
                                    : "";

                    btnPublicar.setEnabled(false);
                    setCancelable(false);
                    btnPublicar.setText(R.string.subiendo);

                    // Si ha intentado mencionar a un local
                    if (!localMencionado.isEmpty()) {
                        // Quitamos el @ por si el usuario lo ha puesto
                        String cleanMention = localMencionado.replace("@", "");

                        usuarioService
                                .getUsuarioByUsernameExacto(cleanMention)
                                .addOnSuccessListener(
                                        usuario -> {
                                            // Comprobamos que el mencionado sea un local
                                            if ("local".equals(usuario.getRol())) {
                                                subirPublicacion(texto, usuario.getUid());
                                            } else {
                                                Toast.makeText(
                                                                getContext(),
                                                                R.string.user_is_not_a_local,
                                                                Toast.LENGTH_SHORT)
                                                        .show();
                                                restaurarBotonPublicar();
                                            }
                                        })
                                .addOnFailureListener(
                                        e -> {
                                            Toast.makeText(
                                                            getContext(),
                                                            R.string
                                                                    .local_not_found_with_exact_username,
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                            restaurarBotonPublicar();
                                        });

                    } else {
                        // Si no se menciona se pasa null
                        subirPublicacion(texto, null);
                    }
                });
    }

    /**
     *  Sube la publicación y la foto a la base de datos.
     */
    private void subirPublicacion(String texto, String uidLocal) {
        Publicacion nuevaPub =
                Publicacion.builder()
                        .uidUsuario(uidUsuarioActual)
                        .uidLocal(uidLocal)
                        .texto(texto)
                        .build();

        publicacionService
                .subirPublicacion(nuevaPub, uriFotoSeleccionada)
                .addOnSuccessListener(
                        unused -> {
                            Toast.makeText(
                                            getContext(),
                                            R.string.publicacion_subida,
                                            Toast.LENGTH_SHORT)
                                    .show();
                            dismiss();
                        })
                .addOnFailureListener(
                        e -> {
                            if (e instanceof FoodTracksValidationException ex) {
                                Toast.makeText(getContext(), ex.getErrorResId(), Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                Toast.makeText(
                                                getContext(),
                                                getString(R.string.subir_publicacion_error_message)
                                                        + e.getMessage(),
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                            restaurarBotonPublicar();
                        });
    }

    /**
     * Devuelve el botón a su estado normal
     */
    private void restaurarBotonPublicar() {
        btnPublicar.setEnabled(true);
        setCancelable(true);
        btnPublicar.setText(R.string.publicar);
    }

    /**
     * Muestra un diálogo inferior para elegir entre Cámara o Galería
     */
    private void mostrarOpcionesImagen() {
        String[] opciones = {
            getString(R.string.hacer_foto), getString(R.string.elegir_de_la_galeria)
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.adjuntar_imagen)
                .setItems(
                        opciones,
                        (dialog, which) -> {
                            if (which == 0) {
                                solicitarPermisoCamara();
                            } else {
                                galleryLauncher.launch("image/*");
                            }
                        })
                .show();
    }

    /**
     * Comprueba si tenemos permiso y lo solicita si es necesario
     */
    private void solicitarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Prepara un archivo temporal en la caché y abre la cámara
     */
    private void abrirCamara() {
        File fotoTemporal = new File(requireContext().getCacheDir(), "temp_foodtracks_pub.jpg");

        uriFotoCamaraTemporal =
                FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        fotoTemporal);

        // Lanzamos la cámara con esa URI
        cameraLauncher.launch(uriFotoCamaraTemporal);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            int width = getResources().getDisplayMetrics().widthPixels;
            getDialog()
                    .getWindow()
                    .setLayout((int) (width * 0.90), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
