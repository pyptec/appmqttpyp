package com.example.control4;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Inscripcion extends AppCompatActivity {

    Button Crear;
    EditText CajaUsuario,CajaClave;
    Button AtrasMisDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscripcion);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");

        actionBar.setDisplayShowHomeEnabled(true);

        Crear =  findViewById(R.id.iCrear);
        CajaUsuario =  findViewById(R.id.iCajaUsuario);
        CajaClave =   findViewById(R.id.iCajaClave);

        Crear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String NomUsuIdUser= CajaUsuario.getText().toString();
                String Clave =  CajaClave.getText().toString();
                String Param = "USU|" + NomUsuIdUser + "|" + NomUsuIdUser+ "|"
                                + NomUsuIdUser + "|" + Clave;
                deleteDatabase("TablaCredenciales");
                SQLiteDatabase DbCredenciales =  openOrCreateDatabase("TablaCredenciales",MODE_PRIVATE,
                        null);
                DbCredenciales.execSQL("CREATE TABLE IF NOT EXISTS Credenciales(Todos VARCHAR);");

                String palo = "INSERT INTO Credenciales (Todos)VALUES(?);";
                DbCredenciales.execSQL(palo,new String []{Param});
                DbCredenciales.close();

                finish();
                System.exit(0);
            }
        });
    }
}
