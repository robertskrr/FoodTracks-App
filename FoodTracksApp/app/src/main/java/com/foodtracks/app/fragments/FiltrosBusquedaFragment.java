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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filtros, container, false);

        inicializar(view);
        setListeners(view);

        return view;
    }

    private void setListeners(View view) {
        // Aplica filtros personalizados
        btnAplicarManual.setOnClickListener(v -> {
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
        btnUsarMisPreferencias.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString("TIPO_BUSQUEDA", "MIS_PREFERENCIAS");
            result.putString("ciudad", etCiudad.getText().toString());

            getParentFragmentManager().setFragmentResult("CLAVE_FILTROS", result);
            dismiss();
        });
    }

    private void inicializar(View view){
        etCiudad = view.findViewById(R.id.etFiltroCiudad);
        etOtraPreferencia = view.findViewById(R.id.etFiltroOtra);
        cbVegano = view.findViewById(R.id.cbFiltroVegano);
        cbVegetariano = view.findViewById(R.id.cbFiltroVegetariano);
        cbSinLactosa = view.findViewById(R.id.cbFiltroLactosa);
        cbCeliaco = view.findViewById(R.id.cbFiltroCeliaco);
        btnAplicarManual = view.findViewById(R.id.btnAplicarFiltros);
        btnUsarMisPreferencias = view.findViewById(R.id.btnMisPreferencias);
    }
}
