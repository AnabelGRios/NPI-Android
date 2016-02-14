package com.npi.appsorpresa;

import android.annotation.TargetApi;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*  This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    See <http://www.gnu.org/licenses/> for a copy of the GNU General
    Public License.
    Autores: Jacinto Carrasco Castillo, Anabel Gómez Ríos.
    Fecha de la última modificación: 10/02/2016.
 */

/* Los líneas generales para la transmisión a través de NFC están obtenidos de las guías de
   la API de android: http://developer.android.com/intl/es/guide/topics/connectivity/nfc/index.html
   y http://developer.android.com/intl/es/guide/topics/connectivity/nfc/nfc.html y del tutorial
   presente en la web http://android-er.blogspot.com.es/2014/04/communication-between-android-using-nfc.html.
   Así mismo, los métodos para codificar y decodificar mensajes en base 64 hemos conseguido que
   funcionen gracias a http://www.iteramos.com/pregunta/48153/en-base-64-codificar-y-decodificar-un-codigo-de-ejemplo.
   Las modificaciones y extensiones de estas guías son nuestras.
 */

/* Esta clase controla la interfaz y las opciones que tiene un administrador: poder
   codificar y decodificar un mensaje.
 */

public class Admin extends AppCompatActivity implements View.OnClickListener,
        NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    // EditText donde escribiremos el mensaje a escribir
    private EditText editCodingText;

    // TextViews donde se mostrarán el texto recibido decodificado
    private TextView textReceived;

    // TextViews donde se mostrarán el texto recibido codificado
    private TextView codedText;

    // Dispositivo NFC
    private NfcAdapter nfcAdapter;

    // Botón para codificar el mensaje escrito
    private Button codeBtn;

    // String codificado recibido
    private String coded_string_received;

    // String codificado que enviaremos
    private String coded_string_sending;

    // Boolean para saber si el usuario se ha registrado o no como Admin
    private boolean is_registered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Instanciamos el TextView para el texto codificado
        codedText = (TextView) findViewById(R.id.coded_text);

        // Instanciamos el EditText donde escribiremos el texto a codificar
        editCodingText = (EditText) findViewById(R.id.editCodingText);

        // Instanciamos el TextView donde escribiremos el texto decodificado
        textReceived = (TextView) findViewById(R.id.decoded_text);

        // Instanciamos el botón para codificar
        codeBtn = (Button) findViewById(R.id.codeBtn);
        codeBtn.setOnClickListener(this);

        // Instanciamos el Adaptador NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter==null){
            Toast.makeText(Admin.this, R.string.nfc_not_found_msg, Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(Admin.this, R.string.establish_conn_msg,Toast.LENGTH_LONG).show();
            nfcAdapter.setNdefPushMessageCallback(this, this);
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }

        // Obtenemos si el objeto Admin ha sido creado por la clase Main introduciendo la contraseña
        Intent intent = getIntent();
        is_registered = intent.getBooleanExtra("Registered",false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Obtenemos el Intent que ha activado a la aplicación
        Intent intent = getIntent();
        String action = intent.getAction();
        
        if(action != null && action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED) && is_registered){
            Parcelable[] parcelables =
                    intent.getParcelableArrayExtra(
                            NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage inNdefMessage = (NdefMessage)parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord NdefRecord_0 = inNdefRecords[0];

            // Obtenemos el String recibido y lo mostramos
            coded_string_received = new String(NdefRecord_0.getPayload());
            String decoded_string = decode(coded_string_received);
            textReceived.setText(decoded_string);
        }
    }

    //Método para recibir el Intent.
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    // Método que permite el envío del mensaje por NFC
    @Override
    public void onNdefPushComplete(NfcEvent event) {

        final String eventString = "onNdefPusComplete\n" + event.toString();
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), eventString, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Método para crear el mensaje
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        byte[] bytesOut = coded_string_sending.getBytes();

        NdefRecord ndefRecordOut = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(),
                new byte[]{},
                bytesOut);

        NdefMessage ndefMessageout = new NdefMessage(ndefRecordOut);
        return ndefMessageout;
    }

    // Método para codificar un mensaje
    String code(String msg){
        String base64 = null;
        try {
            byte[] data = msg.getBytes("UTF-8");
            base64 = Base64.encodeToString(data, Base64.DEFAULT);
        }
        catch (Exception e){
            Toast.makeText(Admin.this,R.string.exception_coding_msg,Toast.LENGTH_LONG).show();
        }
        return base64;
    }

    // Método para decodificar un mensaje
    String decode(String msg){
        String text = null;
        try{
            byte[] data = Base64.decode(msg, Base64.DEFAULT);
            text = new String(data, "UTF-8");
        }
        catch (Exception e){
            Toast.makeText(Admin.this, R.string.exception_decoding_msg, Toast.LENGTH_LONG).show();
        }

        return text;
    }

    // Método para responder al evento Click
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.codeBtn){
            coded_string_sending = code(editCodingText.getText().toString());
            codedText.setText(coded_string_sending);
        }
    }
}