package com.npi.appfotovoz;

import android.content.Context;
import android.content.Intent;
import android.graphics.Interpolator;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class BrujulaVoz extends AppCompatActivity{

    public final static String EXTRA_BUNDLE = "com.npi.appfotovoz.BUNDLE";

    // Views donde se cargarán las cadenas de texto
    private TextView message_error;

    // Valores para el reconocimiento de voz

    private String language_model = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;

    private static final String LOGTAG = "ASRBEGIN";
    private static int ASR_CODE = 123;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brujula_voz);

        message_error = (TextView) findViewById(R.id.message_error);

        setSpeekButton();
    }


    private void listen(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, language_model);

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

        startActivityForResult(intent, ASR_CODE);
    }

    private void setSpeekButton() {
        Button speak = (Button) findViewById(R.id.speech_button);

        speak.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         if ("generic".equals(Build.BRAND.toLowerCase())) {
                                             Toast toast = Toast.makeText(getApplicationContext(), "ASR no es soportado en dispositivos virtuales", Toast.LENGTH_SHORT);
                                             toast.show();
                                             Log.d(LOGTAG, "Intento de ASR en un dispositivo virtual");
                                         } else {
                                             listen();
                                         }
                                     }
                                 }
        );
    }

    private void sendMessage(String cardinal_point, float tolerance){
        Intent intent = new Intent(this, Brujula.class);
        Bundle b = new Bundle();

        b.putString("Point", cardinal_point);
        b.putFloat("Tolerance",tolerance);

        intent.putExtra(EXTRA_BUNDLE, b);
        startActivity(intent);
    }

    private void findBestMatch(ArrayList<String> strings){
        String result = null;
        boolean found = false;
        float tolerance = -1;
        String cardinal_point = null;

        for(int i = 0; i < strings.size() && !found ; i++){
            String string = strings.get(i);

            if(string.contains(" ")) {
                String[] parts = string.split(" ");
                if (parts.length == 2){
                    cardinal_point = parts[0];
                    cardinal_point = cardinal_point.toLowerCase();

                    try{
                        tolerance = Float.parseFloat(parts[1]);
                    }
                    catch(Exception e){
                        tolerance = -1;
                    }
                    finally {
                        if ((cardinal_point.equals("norte") ||
                                cardinal_point.equals("sur") ||
                                cardinal_point.equals("este") ||
                                cardinal_point.equals("oeste")) && tolerance != -1) {
                            result = string;
                            found = true;
                        }
                        else {
                            tolerance = -1;
                        }
                    }

                }
            }

        }

        if( !found ){
            message_error.setText("La entrada debe ser de la forma: \"Punto Cardinal\" \"tolerancia\" ");
        }
        else{
            sendMessage(cardinal_point,tolerance);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == ASR_CODE){
            if (resultCode == RESULT_OK){
                if (data != null) {
                    ArrayList<String> strings = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    findBestMatch(strings);
                }
            }
            else{
                //Reports error in recognition error in log
                Log.e(LOGTAG, "Recognition was not successful");
            }
        }
    }
}

