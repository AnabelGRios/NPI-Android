package com.npi.appsorpresa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


/*
 * Clase en la que introduciremos la contraseña y estableceremos el rol a seguir
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // Boolean para controlar si se ha introducido la contraseña correctamente
    public boolean is_registered = false;

    // Contraseña que debe introducirse
    private String password = "password";

    // EditText donde introduciremos la contraseña
    private EditText editText;

    // Botón para entrar como Player
    private Button omitBtn;

    // Botón para entrar como Admin, habiendo introducido la contraseña
    private Button enterBtn;

    // Método para la creación de la clase
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Instanciación del botón Enter
        enterBtn = (Button) findViewById(R.id.enter_button);
        enterBtn.setOnClickListener(this);

        // Instanciación del botón Omit
        omitBtn = (Button) findViewById(R.id.omit_button);
        omitBtn.setOnClickListener(this);

        // Instanciación del EditText
        editText = (EditText) findViewById(R.id.editText);
    }

    //Se responde al evento click
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.omit_button){
            Intent intent = new Intent(this, Player.class);
            is_registered = false;
            intent.putExtra("Registered",is_registered);
            startActivity(intent);
        }
        if(v.getId()==R.id.enter_button && password.equals(editText.getText().toString())){
            Intent intent = new Intent(this, Admin.class);
            is_registered = true;
            intent.putExtra("Registered",is_registered);
            startActivity(intent);
        }
    }
}
