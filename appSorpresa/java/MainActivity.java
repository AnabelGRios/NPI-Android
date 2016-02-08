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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements
        NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    TextView textReceived;
    EditText textOut;
    TextView textCoded;

    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textReceived = (TextView)findViewById(R.id.receivedtext);
        textOut = (EditText)findViewById(R.id.textout);
        textCoded = (TextView)findViewById(R.id.codedtext);


        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter==null){
            Toast.makeText(MainActivity.this,"NFC no encontrado",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(MainActivity.this,"Establecer conexión",Toast.LENGTH_LONG).show();
            nfcAdapter.setNdefPushMessageCallback(this, this);
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String action = intent.getAction();
        if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            Parcelable[] parcelables =
                    intent.getParcelableArrayExtra(
                            NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage inNdefMessage = (NdefMessage)parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord NdefRecord_0 = inNdefRecords[0];
            String inMsg = new String(NdefRecord_0.getPayload());
            String result = decode(inMsg);
            textReceived.setText(result);
            textCoded.setText(inMsg);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {

        final String eventString = "onNdefPusComplete\n" + event.toString();
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),eventString,Toast.LENGTH_LONG).show();
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        String stringOut = code(textOut.getText().toString());
        byte[] bytesOut = stringOut.getBytes();

        NdefRecord ndefRecordOut = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(),
                new byte[]{},
                bytesOut);

        NdefMessage ndefMessageout = new NdefMessage(ndefRecordOut);
        return ndefMessageout;
    }

    String code(String msg){
        String base64 = null;
        try {
            byte[] data = msg.getBytes("UTF-8");//msg.getBytes("UTF-8");
            base64 = Base64.encodeToString(data, Base64.DEFAULT);
        }
        catch (Exception e){
            Toast.makeText(MainActivity.this,"Excepción codificando",Toast.LENGTH_LONG).show();
        }
        return base64;
    }

    String decode(String msg){
        String text = null;
        try{
            byte[] data = Base64.decode(msg, Base64.DEFAULT);
            text = new String(data, "UTF-8");
        }
        catch (Exception e){
            Toast.makeText(MainActivity.this,"Excepción decodificando",Toast.LENGTH_LONG).show();
        }

        return text;
    }
}
