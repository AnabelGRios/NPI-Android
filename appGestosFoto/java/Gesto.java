package com.npi.appgestosfoto;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
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

/*
 * Clase implementada siguiendo el tutorial del enlace de la asignatura
 * https://nuevos-paradigmas-de-interaccion.wikispaces.com/Detecci%C3%B3n+de+patrones+en+Android+-+Gesture+Builder
 */

/*
 * Clase que gestiona la entrada de Gestures en la aplicación
 */
public class Gesto extends AppCompatActivity implements OnGesturePerformedListener {

    // Librería de gestos
    private GestureLibrary gestureLib;

    // Creación de la clase
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesto);

        // Creamos la capa donde dibujaremos el gesto
        GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
        View inflate = getLayoutInflater().inflate(R.layout.activity_gesto,null);
        gestureOverlayView.addView(inflate);

        // Añadimos el listener de gestos a esta capa
        gestureOverlayView.addOnGesturePerformedListener(this);

        // Cargamos la librería de gestos
        gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);

        if(!gestureLib.load()){
            // Cerramos la aplicación si no podemos cargar la librería
            finish();
        }

        // Hacemos visible la capa
        setContentView(gestureOverlayView);

    }

    //Reconocedor de gestos
    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
        for (Prediction prediction : predictions) {
            if (prediction.score > 1.0) {
                Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT).show();

                // Activamos la cámara si se ha dibujado la cámara
                if(prediction.name.equals("camera")){
                    Intent intent = new Intent(this, Foto.class);
                    startActivity(intent);
                }
            }
        }
    }

}
