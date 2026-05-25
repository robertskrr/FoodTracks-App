/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.DialogFragment;

import com.foodtracks.app.R;
import com.foodtracks.app.activities.cliente.RegisterClienteActivity;
import com.foodtracks.app.activities.local.RegisterLocalActivity;

/**
 * @author Robert
 * @since 12/02
 */
public class TipoRegistroFragment extends DialogFragment {

    private Button btnCliente;
    private Button btnLocal;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tipo_registro, container, false);
        inicializar(v);
        configurarBotones();

        return v;
    }

    /**
     * Asigna los componentes a la interfaz
     *
     * @param v Vista del fragmento
     */
    private void inicializar(View v) {
        btnCliente = v.findViewById(R.id.btnRegistroCliente);
        btnLocal = v.findViewById(R.id.btnRegistroLocal);
    }

    /**
     * Configura los botones del tipo de registro.
     */
    private void configurarBotones() {
        btnCliente.setOnClickListener(
                v1 -> startActivity(new Intent(getContext(), RegisterClienteActivity.class)));

        btnLocal.setOnClickListener(
                v2 -> startActivity(new Intent(getContext(), RegisterLocalActivity.class)));
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
