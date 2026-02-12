package com.example.foodtracks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * @author Robert
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore mFirestore; // TRABAJARA CON LOS DATOS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(getApplicationContext(), "HOLAAA BUENAS TARDES", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));

        asignarComponentes();
    }

    private void asignarComponentes() {
        mFirestore = FirebaseFirestore.getInstance();
    }


}