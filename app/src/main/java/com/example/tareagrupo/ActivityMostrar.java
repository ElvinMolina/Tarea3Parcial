package com.example.tareagrupo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ActivityMostrar extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private List<MainActivity.Usuario> listPerson = new ArrayList<MainActivity.Usuario>();
    ArrayAdapter<MainActivity.Usuario> arrayAdapterPersona;
    ListView listV_personas;
    public MainActivity.Usuario personasSelected;
    ImageButton btnregresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar);
        FirebaseApp.initializeApp(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        listV_personas = findViewById(R.id.Lista);
        listV_personas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                personasSelected = (MainActivity.Usuario) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(getApplicationContext(), ActivityActualizar.class);
                intent.putExtra("nombre", personasSelected.getNombres());
                intent.putExtra("apellido", personasSelected.getApellidos());
                intent.putExtra("fecha",personasSelected.getFechaN());
                intent.putExtra("id",personasSelected.getid());
                startActivity(intent);
            }
        });
        listarDatos();
        btnregresar = findViewById(R.id.btnregresar);
        btnregresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

    }
    private void listarDatos() {
        mDatabase.child("usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot ) {
                listPerson.clear();
                for (DataSnapshot objSnaptshot : dataSnapshot.getChildren()){
                    MainActivity.Usuario p = objSnaptshot.getValue(MainActivity.Usuario.class);
                    listPerson.add(p);

                    arrayAdapterPersona = new ArrayAdapter<MainActivity.Usuario>(ActivityMostrar.this, android.R.layout.simple_list_item_1, listPerson);
                    listV_personas. setAdapter(arrayAdapterPersona);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError ) {

            }
        });
    }
}