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
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class Player extends AppCompatActivity implements
        NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback{

    // TextViews donde se mostrarán el texto recibido decodificado y el texto codificado que mandaremos
    private TextView textReceived;

    // Dispositivo NFC
    private NfcAdapter nfcAdapter;

    // Mensaje codificado que mandaremos.
    private String coded_string;

    boolean is_registered = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        textReceived= (TextView)findViewById(R.id.coded_text);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter==null){
            Toast.makeText(Player.this,"NFC no encontrado",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(Player.this,"Establecer conexión",Toast.LENGTH_LONG).show();
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
