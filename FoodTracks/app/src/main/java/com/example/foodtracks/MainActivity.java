package com.example.foodtracks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodtracks.gui.fragments.TipoRegistroFragment;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * @author Robert
 * @since 20/01
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore mFirestore; // TRABAJARA CON LOS DATOS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(getApplicationContext(), "HOLAAA BUENAS TARDES", Toast.LENGTH_SHORT).show();
        asignarComponentes();
    }

    private void asignarComponentes() {
        mFirestore = FirebaseFirestore.getInstance();
    }

    public void login(View view){

    }

    public void register(View view){
        TipoRegistroFragment fm = new TipoRegistroFragment();
        fm.show(getSupportFragmentManager(), "Navegar a fragment");
    }


}