package com.foodtracks.app.fragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.foodtracks.app.R;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * Fragment de visor de imágenes en tamaño completo con opción de zoom.
 *
 * @author Robert
 * @since 16/05
 */
public class VisorImagenDialogFragment extends DialogFragment {

    private static final String ARG_IMAGE_URL = "image_url";
    private String imageUrl;

    private int colorOriginalBarra;

    public static VisorImagenDialogFragment newInstance(String imageUrl) {
        VisorImagenDialogFragment fragment = new VisorImagenDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrl = getArguments().getString(ARG_IMAGE_URL);
        }
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visor_imagen, container, false);

        // Con PhotoView podremos hacer zoom a la imagen
        PhotoView imgCompleta = view.findViewById(R.id.imgVisorCompleta);

        Glide.with(this).load(imageUrl).into(imgCompleta);

        view.findViewById(R.id.btnCerrarVisor).setOnClickListener(v -> dismiss());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Al abrir el visor, guardamos el color de la barra anterior y la pintamos de negro
        if (getActivity() != null && getActivity().getWindow() != null) {
            colorOriginalBarra = getActivity().getWindow().getStatusBarColor();
            getActivity().getWindow().setStatusBarColor(Color.BLACK);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // Al cerrar el visor restauramos el color de la barra
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().setStatusBarColor(colorOriginalBarra);
        }
    }
}