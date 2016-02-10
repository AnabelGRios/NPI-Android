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
   Las modificaciones y extensiones de estas guías son nuestras.
 */

/* Esta clase controla la interfaz y las opciones que tiene un jugador: recibir un mensaje codificado
   y enviarlo a un administrador para su decodificación.
 */

public class Player extends AppCompatActivity implements
        NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback{

    // TextViews donde se mostrarán el texto recibido decodificado
    private TextView textReceived;

    // Dispositivo NFC
    private NfcAdapter nfcAdapter;

    // Mensaje codificado que mandaremos.
    private String coded_string;

    // Boolean para saber si el usuario se ha registrado o no como Admin
    boolean is_registered = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Instanciación el TextView donde recibiremos el mensaje codificado
        textReceived= (TextView)findViewById(R.id.coded_text);

        // Instanciación del adaptador NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter==null){
            Toast.makeText(Player.this,R.string.nfc_not_found_msg,Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(Player.this,R.string.establish_conn_msg,Toast.LENGTH_LONG).show();
            nfcAdapter.setNdefPushMessageCallback(this, this);
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Obtenemos el Intent que ha activado a la aplicación
        Intent intent = getIntent();
        String action = intent.getAction();
        is_registered = intent.getBooleanExtra("Registered",false);
        if(action != null && action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED) && !is_registered){
            Parcelable[] parcelables =
                    intent.getParcelableArrayExtra(
                            NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage inNdefMessage = (NdefMessage)parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord NdefRecord_0 = inNdefRecords[0];

            // Obtenemos el String recibido y lo mostramos
            coded_string = new String(NdefRecord_0.getPayload());
            textReceived.setText(coded_string);
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
        byte[] bytesOut = coded_string.getBytes();

        NdefRecord ndefRecordOut = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(),
                new byte[]{},
                bytesOut);

        NdefMessage ndefMessageout = new NdefMessage(ndefRecordOut);
        return ndefMessageout;
    }



}
