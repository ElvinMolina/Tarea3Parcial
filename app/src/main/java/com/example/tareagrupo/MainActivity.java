package com.example.tareagrupo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private EditText mNombresField;
    private EditText mApellidosField;
    private EditText mFechaNField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa Firebase
        FirebaseApp.initializeApp(this);

        // Obtiene una referencia a la base de datos de Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Obtiene referencias a los campos del formulario
        mNombresField = findViewById(R.id.nombres);
        mApellidosField = findViewById(R.id.apellidos);
        mFechaNField = findViewById(R.id.fechaN);

        // Agrega un listener al botón "Guardar" para guardar los datos en Firebase
        Button guardarButton = findViewById(R.id.btnGuardar);
        guardarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarDatos();
            }
        });
    }

    private void guardarDatos() {
        // Obtiene los valores de los campos del formulario
        String nombres = mNombresField.getText().toString().trim();
        String apellidos = mApellidosField.getText().toString().trim();
        String fechaN = mFechaNField.getText().toString().trim();

        // Verifica que se hayan ingresado valores para los campos obligatorios
        if (TextUtils.isEmpty(nombres)) {
            mNombresField.setError("Campo requerido");
            return;
        }

        if (TextUtils.isEmpty(apellidos)) {
            mApellidosField.setError("Campo requerido");
            return;
        }

        if (TextUtils.isEmpty(fechaN)) {
            mFechaNField.setError("Campo requerido");
            return;
        }

        // Crea un objeto de datos con los valores del formulario
        Usuario usuario = new Usuario(nombres, apellidos, fechaN);

        // Obtiene una nueva clave para el usuario
        String usuarioId = mDatabase.child("usuarios").push().getKey();

        // Guarda los datos del usuario en la base de datos de Firebase
        mDatabase.child("usuarios").child(usuarioId).setValue(usuario);

        // Muestra un mensaje de éxito
        Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
    }

    // Clase para representar los datos del usuario
    private static class Usuario {
        public String nombres;
        public String apellidos;
        public String fechaN;

        public Usuario() {
            // Constructor vacío requerido para llamadas a DataSnapshot.getValue(Usuario.class)
        }

        public Usuario(String nombres, String apellidos, String fechaN) {
            this.nombres = nombres;
            this.apellidos = apellidos;
            this.fechaN = fechaN;
        }
    }
}