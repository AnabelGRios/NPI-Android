package com.npi.appmovimientosonido;

import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

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

/**
 * Realizado usando el tutorial sobre reproducción de sonido de la wiki:
 * https://nuevos-paradigmas-de-interaccion.wikispaces.com/Reproducci%C3%B3n+de+audio+sobre+Android
 * y la referencia de Android para el sensor de LinearAcelleration
 * http://developer.android.com/intl/es/guide/topics/sensors/sensors_motion.html
 **/

/*
 * Clase que recoge un movimiento usando el acelerómetro y reproduce un sonido atendiendo a éste
 */
public class Movimiento extends AppCompatActivity implements SensorEventListener{

    // Administrador de los sensores
    private SensorManager mSensorManager;

    // Sensor con el que sabremos la aceleración que sufre el dispositivo
    private Sensor mAccelerometer;

    // Reproductor del sonido
    private MediaPlayer reproductor;

    // boolean para saber si se está golpeando el tambor
    private boolean hitting = false;

    // TextView donde se muestra la aceleración sufrida en el eje Z
    TextView acel_Z_text;

    // Creación de la Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimiento);

        // Instanciación del SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Obtención del acelerómetro
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        // Instanciación del reproductor
        reproductor = new MediaPlayer();

        // Instanciación del TextView
        acel_Z_text = (TextView) findViewById(R.id.acel_Z);
    }

    // Obtenemos el sensor cuando volvemos a la aplicación
    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Liberamos el recurso cuando dejamos la aplicación
    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    // Manejamos el cambio en la aceleración lineal (sin contar la gravedad
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_LINEAR_ACCELERATION:
                // Obtenemos la aceleración en el eje Z
                float aceleration_Z = event.values[2];
                acel_Z_text.setText(Float.toString(aceleration_Z));

                if(event.values[2] >3){
                    hitting = true;
                }
                // Si paramos, habremos golpeado
                else if (hitting && event.values[2] < 0.5){
                    hitting = false;

                    //Reproducimos el sonido
                    try{
                        AssetFileDescriptor afd = Movimiento.this.getResources().openRawResourceFd( R.raw.tomacoustic01 );
                        reproductor.reset();

                        reproductor.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
                        reproductor.prepare();

                        reproductor.start();
                    }
                    catch (Exception e){
                        Log.e("001","Excepción intentando reproducir sonido " + e.getMessage(),e);
                    }
                }
                else {
                    reproductor.stop();
                }
            break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
