package com.foodtracks.app.gui.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.foodtracks.app.R;
import com.foodtracks.app.gui.users.cliente.RegisterClienteActivity;
import com.foodtracks.app.gui.users.local.RegisterLocalActivity;

/**
 * @author Robert
 * @since 12/02
 */
public class TipoRegistroFragment extends DialogFragment {

    private Button btnCliente;
    private Button btnLocal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tipo_registro, container, false);

        btnCliente = v.findViewById(R.id.btnIniciarSesion);
        btnLocal = v.findViewById(R.id.btnRegistroLocal);

        btnCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), RegisterClienteActivity.class));
            }
        });

        btnLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), RegisterLocalActivity.class));
            }
        });
        return v;
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