/** © FoodTracks Project ===robertskrr=== */

package com.foodtracks.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.foodtracks.app.R;
import com.foodtracks.app.activities.cliente.MainClienteActivity;
import com.foodtracks.app.fragments.LoginFragment;
import com.foodtracks.app.fragments.TipoRegistroFragment;

/**
 * @author Robert
 * @since 20/01
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Muestra el fragment con los datos de inicio de sesión.
     */
    public void login(View view) {
        LoginFragment fm = new LoginFragment();
        fm.show(getSupportFragmentManager(), "Fragment login");
    }

    /**
     * Muestra las opciones de registro en un fragment
     */
    public void register(View view) {
        TipoRegistroFragment fm = new TipoRegistroFragment();
        fm.show(getSupportFragmentManager(), "Fragment registro");
    }

    /**
     * Acceso como invitado.
     */
    public void invitado(View view) {
        startActivity(new Intent(getApplicationContext(), MainClienteActivity.class));
    }
}
