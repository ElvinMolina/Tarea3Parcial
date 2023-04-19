package com.example.tareagrupo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ActivityActualizar extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private DatabaseReference mDatabase;
    private EditText mNombres;
    private EditText mApellidos;
    private EditText mFechaN;

    private ImageView ImagenSeleccionada;
    String nombre,apellido,fechan,id;
    Button btnactua,btnelimnar;
    private ImageView mImagenSeleccionada;
    private ImageButton mBtnSubirFoto;
    private Uri mImagenUri;
    ImageButton btnregresar;

    private StorageReference mStorageRef;
    FirebaseDatabase firebaseDatabase;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar);

        btnregresar = (ImageButton) findViewById(R.id.btn_regresar);

        btnregresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ActivityMostrar.class);
                startActivity(intent);
            }
        });

        FirebaseApp.initializeApp(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference("imagenes");
        ImagenSeleccionada = findViewById(R.id.imagenSelecionada);

        mNombres = findViewById(R.id.nombres);
        mApellidos = findViewById(R.id.apellidos);
        mFechaN = findViewById(R.id.fechaN);
        Intent intent = getIntent();
        nombre = intent.getStringExtra("nombre");
        apellido = intent.getStringExtra("apellido");
        fechan = intent.getStringExtra("fecha");
        id = intent.getStringExtra("id");

        mNombres.setText(nombre);
        mApellidos.setText(apellido);
        mFechaN.setText(fechan);

        mBtnSubirFoto = findViewById(R.id.btnSubirFoto);
        btnactua = findViewById(R.id.btnactualizar);
        btnelimnar = findViewById(R.id.btneliminar);
        btnactua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActualizarDatos();
                Intent list = new Intent(getApplicationContext(),ActivityMostrar.class);
                startActivity(list);
            }
        });
        mBtnSubirFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirSeleccionadorDeImagenes();
            }
        });
        btnelimnar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminar();
                Intent list = new Intent(getApplicationContext(),ActivityMostrar.class);
                startActivity(list);
            }
        });
        mostrarImagen();
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
                ImagenSeleccionada.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void mostrarImagen() {
        // Obtener la URL de la imagen del usuario con ID "userId"
        mDatabase.child("usuarios").child(id).child("imagenUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageUrl = snapshot.getValue(String.class);

                // Cargar la imagen en la ImageView usando Picasso
                Picasso.get().load(imageUrl).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        // Almacenar la imagen en la variable miembro mBitmap
                        mBitmap = bitmap;
                        ImagenSeleccionada.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }

                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void ActualizarDatos()
    {
        if(mImagenUri != null)
        {
            // Convertir la imagen seleccionada en un objeto Bitmap
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImagenUri);
                mBitmap = bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(mBitmap != null)
        {
            // Convertir el objeto Bitmap en un archivo JPEG
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(bitmapdata);

            // Subir imagen a Firebase Storage
            final StorageReference imagenRef = mStorageRef.child("usuarios").child(System.currentTimeMillis() + ".jpg");
            UploadTask uploadTask = imagenRef.putStream(bis);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Obtener URL de la imagen subida
                    imagenRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Crear un objeto de datos con los valores del formulario y la URL de la imagen
                            MainActivity.Usuario pp = new MainActivity.Usuario();
                            pp.setid(id);
                            pp.setNombres(mNombres.getText().toString().trim());
                            pp.setApellidos(mApellidos.getText().toString().trim());
                            pp.setFechaN(mFechaN.getText().toString().trim());
                            pp.setImagenUrl(uri.toString());
                            mDatabase.child("usuarios").child(pp.getid()).setValue(pp);

                            // Mostrar un mensaje de éxito
                            Toast.makeText(ActivityActualizar.this, "Se ha actualizado correctamente", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ActivityActualizar.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            // La variable mBitmap es nula, mostrar un mensaje de error
            Toast.makeText(ActivityActualizar.this, "Debe seleccionar una imagen", Toast.LENGTH_SHORT).show();
        }
    }

    public void eliminar()
    {
        MainActivity.Usuario pp = new MainActivity.Usuario();
        pp.setid(id);
        mDatabase.child("usuarios").child(pp.getid()).removeValue();
        Toast.makeText(this, "Se ha eliminado correctamente", Toast.LENGTH_SHORT).show();
    }

}