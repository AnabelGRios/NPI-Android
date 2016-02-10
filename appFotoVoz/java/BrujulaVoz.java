package com.npi.appfotovoz;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

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

/* Los métodos listen(), setSpeekButton() y parte de onActivityResult() están obtenidos del material
   facilitado por Zoraida Callejas bajo licencia GNU versión 3.
 */

/* Esta clase se encarga de la primera pantalla visible de la aplicación, en la que aparece el botón
   que permite el reconocimiento de voz al pulsarlo, busca una coincidencia en lo reconocido que tenga
   el formato "punto_cardinal tolerancia", donde punto_cardinal será norte, sur, oeste o este y tolerancia
   será un número y se encarga de pasárselo a la clase "Brújula.java", que será la que se encargue de mostrar
   la brújula por pantalla.
 */
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
        //Inicializamos y enlazamos los Views con los del xml
        setContentView(R.layout.activity_brujula_voz);

        message_error = (TextView) findViewById(R.id.message_error);

        // Llamamos al método para poner el botón para hablar al dispositivo
        setSpeekButton();
    }

    /* Método que reconoce la voz cuando pulsamos el botón para hablar.
     */
    private void listen(){
        // Creamos el intent para reconocer lo que se dice
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Le decimos el idioma en el que se le está hablando y el número máximo de
        // resultados que queremos que nos dé, fijado a 10.
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, language_model);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

        startActivityForResult(intent, ASR_CODE);
    }

    /* Método para poner el botón para poder hablar al dispositivo.
     */
    private void setSpeekButton() {
        Button speak = (Button) findViewById(R.id.speech_button);

        speak.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if ("generic".equals(Build.BRAND.toLowerCase())) {
                     Toast toast = Toast.makeText(getApplicationContext(), R.string.no_ASR, Toast.LENGTH_SHORT);
                     toast.show();
                     Log.d(LOGTAG, "Intento de ASR en un dispositivo virtual");
                 } else {
                     listen();
                 }
             }
        });
    }

    /* Método para, una vez tenemos el punto cardinal y el margen de error reconocidos
       de lo que se ha dicho por voz, mandarle el punto cardinal y la tolerancia a la
       clase "Brujula.java" a través de un Bundle.
       Recibe como argumentos el punto cardinal en un String y un float con la tolerancia.
    */
    private void sendMessage(String cardinal_point, float tolerance){
        Intent intent = new Intent(this, Brujula.class);
        Bundle b = new Bundle();

        b.putString("Point", cardinal_point);
        b.putFloat("Tolerance",tolerance);

        intent.putExtra(EXTRA_BUNDLE, b);
        startActivity(intent);
    }

    /* Método para encontrar, del máximo de 10 resultados reconocidos que se han reconocido
       por voz, aquel que tenga primero un punto cardinal y después un número, que será la
       tolerancia.
       Recibe como parámetros con array de strings con los mensajes reconocidos por voz.
    */
    private void findBestMatch(ArrayList<String> strings){
        boolean found = false;
        float tolerance = -1;
        String cardinal_point = null;

        // Buscamos entre los resultados hasta encontrar alguno con el formato
        // "punto_cardinal tolerancia"
        for(int i = 0; i < strings.size() && !found ; i++){
            String string = strings.get(i);

            if(string.contains(" ")) {
                String[] parts = string.split(" ");
                if (parts.length == 2){
                    cardinal_point = parts[0];
                    cardinal_point = cardinal_point.toLowerCase();

                    // Si la segunda parte del mensaje reconocido no es un número no podremos
                    // pasarlo a float. Controlamos esto con una excepción.
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
                            found = true;
                        }
                        else {
                            tolerance = -1;
                        }
                    }

                }
            }

        }

        // Si no encontramos ninguno con ese formato, indicamos cuál debe ser el formato.
        if( !found ){
            message_error.setText(R.string.error_entrada);
        }
        // Si lo encontramos, llamamos al método que le pasa el punto cardinal y la tolerancia
        // a la clase "Brujula.java".
        else{
            sendMessage(cardinal_point,tolerance);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == ASR_CODE){
            if (resultCode == RESULT_OK){
                if (data != null) {
                    // Recogemos los resultados que se han reconocido.
                    ArrayList<String> strings = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    // Buscamos algún resultado que tenga el formato pedido.
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

