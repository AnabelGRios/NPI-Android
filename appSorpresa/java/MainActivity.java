package com.npi.appsorpresa;

import android.annotation.TargetApi;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public boolean is_registered = false;
    private String password = "password";

    private EditText editText;
    private Button omitBtn;
    private Button enterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        enterBtn = (Button) findViewById(R.id.button);
        enterBtn.setOnClickListener(this);
        editText = (EditText) findViewById(R.id.editText);
        omitBtn = (Button) findViewById(R.id.button2);
        omitBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //Se responde al evento click
        if(v.getId()==R.id.button2){
            Intent intent = new Intent(this, Player.class);
            is_registered = false;
            intent.putExtra("Registered",is_registered);
            startActivity(intent);
        }
        if(v.getId()==R.id.button && password.equals(editText.getText().toString())){
            Intent intent = new Intent(this, Admin.class);
            is_registered = true;
            intent.putExtra("Registered",is_registered);
            startActivity(intent);
        }
    }
}
