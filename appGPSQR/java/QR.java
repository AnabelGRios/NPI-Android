package com.npi.appgpsqr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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

/* El lector de código QR está hecho siguiendo el tutorial presente en la página web
   http://www.hablemosdeandroid.com/p/como-hacer-un-lector-de-codigos-con.html y
   utilizando el proyecto ZXING https://github.com/zxing/zxing de cuya licencia se
   encuentra una copia en la carpeta del proyecto. Cualquier cambio presente en este
   proyecto es nuestro.
 */

/*
 * Clase que se encarga de la lectura de los códigos QR para pasar las coordenadas a
 * la clase Map.
 */
public class QR extends AppCompatActivity implements View.OnClickListener{

    // Botón para leer un nuevo código QR
    private Button scanBtn;

    // Botón para iniciar la navegación
    private Button navigationBtn;

    // TextView para mostrar el formato y contenido del código QR
    private TextView formatTxt, contentTxt;

    // TextView para mostrar el número de localizaciones leídas.
    private TextView coord_numbers;

    // Identificador del Bundle que pasaremos a la Activity Map
    public final static String EXTRA_BUNDLE = "com.npi.appgpsqr.BUNDLE";

    // ArrayList de coordenadas que se pasarán a la Activity Map
    private ArrayList<LatLong> coordinates;

    // Creación de la Activity
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Se instancia el botón para leer un nuevo código
        scanBtn = (Button)findViewById(R.id.scan_button);
        //Se instancia el botón para pasar a la navegación
        navigationBtn = (Button)findViewById(R.id.navigation_button);
        //Se instancia el Campo de Texto para el nombre del formato de código de barra
        formatTxt = (TextView)findViewById(R.id.scan_format);
        //Se instancia el Campo de Texto para el contenido  del código de barra
        contentTxt = (TextView)findViewById(R.id.scan_content);
        //Se instancia el Campo de Texto para el contenido  del número de coordenadas leídas
        coord_numbers = (TextView)findViewById(R.id.coord_numbers);
        //Se agrega la clase MainActivity.java como Listener del evento click del botón de Scan
        scanBtn.setOnClickListener(this);
        //Se agrega la clase MainActivity.java como Listener del evento click del botón de Navigate
        navigationBtn.setOnClickListener(this);
        //Se instancia el ArrayList de coordenadas.
        coordinates = new ArrayList<>();
    }

    //Se responde al evento click en cada uno de los botones
    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.scan_button){
            //Se instancia un objeto de la clase IntentIntegrator
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            //Se procede con el proceso de scaneo
            scanIntegrator.initiateScan();
        }

        if(view.getId()==R.id.navigation_button){
            startNavigation();
        }
    }

    // Método para iniciar la navegación
    private void startNavigation(){
        Bundle b = new Bundle();

        //Se añaden en un Bundle las coordenadas
        b.putParcelableArrayList("Coordinates", coordinates);

        //Iniciamos la Activity del mapa
        Intent intent_map = new Intent(this, Map.class);
        intent_map.putExtra(EXTRA_BUNDLE, b);
        startActivity(intent_map);
    }

    private void addLatLng(float lat, float lng){
        LatLong coord = new LatLong();
        coord.lat = lat;
        coord.lng = lng;

        coordinates.add(coord);
        coord_numbers.setText(Integer.toString(coordinates.size()));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //Se obtiene el resultado del proceso de scaneo y se parsea
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            //Quiere decir que se obtuvo resultado por lo tanto:
            //Desplegamos en pantalla el contenido del código de barra scaneado
            String scanContent = scanningResult.getContents();
            contentTxt.setText(R.string.content + scanContent);
            //Desplegamos en pantalla el nombre del formato del código de barra scaneado
            String scanFormat = scanningResult.getFormatName();
            formatTxt.setText(R.string.format  + scanFormat);

            if(scanContent != null){
                String[] parts = scanContent.split("_");
                addLatLng(Float.parseFloat(parts[1]), Float.parseFloat(parts[3]));
            }
        }else{
            //Quiere decir que NO se obtuvo resultado
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.no_qr_msg, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
