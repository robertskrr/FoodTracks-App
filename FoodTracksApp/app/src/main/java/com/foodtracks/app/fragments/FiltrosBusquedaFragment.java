/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.foodtracks.app.R;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * @author Robert
 * @since 12/05
 */
public class FiltrosBusquedaFragment extends BottomSheetDialogFragment {

    private EditText etCiudad, etOtraPreferencia;
    private CheckBox cbVegano, cbVegetariano, cbSinLactosa, cbCeliaco;
    private Button btnAplicarManual, btnUsarMisPreferencias;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filtros, container, false);

        inicializar(view);
        configurarListeners();

        return view;
    }

    /**
     * Asigna los componentes a la interfaz.
     */
    private void inicializar(View view) {
        etCiudad = view.findViewById(R.id.etFiltroCiudad);
        etOtraPreferencia = view.findViewById(R.id.etFiltroOtra);
        cbVegano = view.findViewById(R.id.cbFiltroVegano);
        cbVegetariano = view.findViewById(R.id.cbFiltroVegetariano);
        cbSinLactosa = view.findViewById(R.id.cbFiltroLactosa);
        cbCeliaco = view.findViewById(R.id.cbFiltroCeliaco);
        btnAplicarManual = view.findViewById(R.id.btnAplicarFiltros);
        btnUsarMisPreferencias = view.findViewById(R.id.btnMisPreferencias);
    }

    /**
     * Configura los listeners de los componentes.
     */
    private void configurarListeners() {
        // Aplica filtros personalizados
        btnAplicarManual.setOnClickListener(
                v -> {
                    Bundle result = new Bundle();
                    result.putString("TIPO_BUSQUEDA", "MANUAL");
                    result.putString("ciudad", etCiudad.getText().toString());
                    result.putBoolean("vegano", cbVegano.isChecked());
                    result.putBoolean("vegetariano", cbVegetariano.isChecked());
                    result.putBoolean("lactosa", cbSinLactosa.isChecked());
                    result.putBoolean("celiaco", cbCeliaco.isChecked());
                    result.putString("otra", etOtraPreferencia.getText().toString());

                    // Enviamos los datos a BusquedaFragment
                    getParentFragmentManager().setFragmentResult("CLAVE_FILTROS", result);
                    dismiss();
                });

        // Envía las preferencias del usuario logueado
        btnUsarMisPreferencias.setOnClickListener(
                v -> {
                    Bundle result = new Bundle();
                    result.putString("TIPO_BUSQUEDA", "MIS_PREFERENCIAS");
                    result.putString("ciudad", etCiudad.getText().toString());

                    getParentFragmentManager().setFragmentResult("CLAVE_FILTROS", result);
                    dismiss();
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog()
                    .getWindow()
                    .setNavigationBarColor(
                            ContextCompat.getColor(
                                    requireContext(), R.color.fondo_filtro_busqueda));
        }
    }
}
