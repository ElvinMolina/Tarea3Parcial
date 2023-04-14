package com.example.tareagrupo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private DatabaseReference mDatabase;
    private EditText mNombresField;
    private EditText mApellidosField;
    private EditText mFechaNField;

    private ImageView mImagenSeleccionada;
    private ImageButton mBtnSubirFoto;
    private Uri mImagenUri;

    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference("imagenes");

        mNombresField = findViewById(R.id.nombres);
        mApellidosField = findViewById(R.id.apellidos);
        mFechaNField = findViewById(R.id.fechaN);

        mImagenSeleccionada = findViewById(R.id.imagenSelecionada);
        mBtnSubirFoto = findViewById(R.id.btnSubirFoto);

        mBtnSubirFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirSeleccionadorDeImagenes();
            }
        });

        Button guardarButton = findViewById(R.id.btnGuardar);
        guardarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarDatos();
            }
        });
    }

    private void abrirSeleccionadorDeImagenes() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            mImagenUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImagenUri);
                mImagenSeleccionada.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void guardarDatos() {
        String nombres = mNombresField.getText().toString().trim();
        String apellidos = mApellidosField.getText().toString().trim();
        String fechaN = mFechaNField.getText().toString().trim();

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

        if (mImagenUri == null) {
            Toast.makeText(this, "Por favor, seleccione una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Subir imagen a Firebase Storage
        final StorageReference imagenRef = mStorageRef.child("usuarios").child(System.currentTimeMillis() + ".jpg");
        UploadTask uploadTask = imagenRef.putFile(mImagenUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Obtener URL de la imagen subida
                imagenRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Crear un objeto de datos con los valores del formulario y la URL de la imagen
                        Usuario usuario = new Usuario(nombres, apellidos, fechaN, uri.toString());

                        // Obtener una nueva clave para el usuario
                        String usuarioId = mDatabase.child("usuarios").push().getKey();

                        // Guardar los datos del usuario en la base de datos de Firebase
                        mDatabase.child("usuarios").child(usuarioId).setValue(usuario);

                        // Mostrar un mensaje de éxito
                        Toast.makeText(MainActivity.this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Clase para representar los datos del usuario
    private static class Usuario {
        public String nombres;
        public String apellidos;
        public String fechaN;
        public String imagenUrl;

        public Usuario() {
            // Constructor vacío requerido para llamadas a DataSnapshot.getValue(Usuario.class)
        }

        public Usuario(String nombres, String apellidos, String fechaN, String imagenUrl) {
            this.nombres = nombres;
            this.apellidos = apellidos;
            this.fechaN = fechaN;
            this.imagenUrl = imagenUrl;
        }
    }
}
