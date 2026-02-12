package com.example.foodtracks.gui.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.foodtracks.R;
import com.example.foodtracks.gui.RegisterClienteActivity;

/**
 * @author Robert
 * @since 12/02
 */
public class TipoRegistroFragment extends DialogFragment {

    private Button btnCliente;
    private Button btnLocal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_tipo_registro, container, false);


        return v;
    }

    public void registroCliente(View view) {

    }

    public void registroLocal(View view) {

    }
}