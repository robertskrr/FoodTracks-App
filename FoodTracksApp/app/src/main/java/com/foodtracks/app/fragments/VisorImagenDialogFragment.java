/** © FoodTracks Project ===robertskrr=== */

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

import com.foodtracks.app.R;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * Fragment de visor de imágenes en tamaño completo con opción de zoom.
 * Soporta URLs remotas y recursos locales.
 *
 * @author Robert
 * @since 16/05
 */
public class VisorImagenDialogFragment extends DialogFragment {

    private static final String ARG_IMAGE_URL = "image_url";
    private static final String ARG_IMAGE_RES = "image_res";

    private String imageUrl;
    private int imageResId = 0;
    private int colorOriginalBarra;

    public static VisorImagenDialogFragment newInstance(String imageUrl) {
        VisorImagenDialogFragment fragment = new VisorImagenDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    public static VisorImagenDialogFragment newInstance(int imageResId) {
        VisorImagenDialogFragment fragment = new VisorImagenDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_RES, imageResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrl = getArguments().getString(ARG_IMAGE_URL);
            imageResId = getArguments().getInt(ARG_IMAGE_RES, 0);
        }
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visor_imagen, container, false);

        PhotoView imgCompleta = view.findViewById(R.id.imgVisorCompleta);

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(this).load(imageUrl).into(imgCompleta);
        } else if (imageResId != 0) {
            Glide.with(this).load(imageResId).into(imgCompleta);
        }

        // Listener para detectar el deslizamiento hacia abajo o a los lados
        imgCompleta.setOnSingleFlingListener((e1, e2, velocityX, velocityY) -> {
            if (e1 != null && e2 != null) {
                float distanciaY = e2.getY() - e1.getY();
                float distanciaX = e2.getX() - e1.getX();

                // Si el deslizamiento es hacia abajo y con suficiente velocidad
                if (distanciaY > 150 && velocityY > 500) {
                    dismiss();
                    return true;
                }

                // Si el deslizamiento es lateral (izquierda a derecha) y con suficiente velocidad
                if (distanciaX > 150 && velocityX > 500) {
                    dismiss();
                    return true;
                }
            }
            return false;
        });

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
