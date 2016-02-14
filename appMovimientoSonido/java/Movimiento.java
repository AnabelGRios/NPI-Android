package com.npi.appmovimientosonido;

import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

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
    private Sensor mGyroscope;

    // Reproductor del sonido
    private MediaPlayer reproductor;
    private MediaPlayer reproductorMaracas;

    // boolean para saber si se está golpeando el tambor
    private boolean hitting = false;
    private boolean girando = false;

    // TextView donde se muestra la aceleración sufrida en el eje Z
    TextView gyr_Z_text;
    TextView accel_Z_text;
    TextView prueba;

    float gyr_Z, accel_Z, accel_X, accel_Y, gyr_X, gyr_Y;


    // Creación de la Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimiento);

        // Instanciación del SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Obtención del acelerómetro
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        // Instanciación del reproductor
        reproductor = MediaPlayer.create(this, R.raw.tomacoustic01);

        reproductorMaracas = MediaPlayer.create(this, R.raw.maracas);

        // Instanciación del TextView
        gyr_Z_text = (TextView) findViewById(R.id.gyr_Z);
        accel_Z_text = (TextView) findViewById(R.id.accel_Z);
        prueba = (TextView) findViewById(R.id.prueba);
    }

    // Obtenemos el sensor cuando volvemos a la aplicación
    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Liberamos el recurso cuando dejamos la aplicación
    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
        if (reproductor.isPlaying()) {
            reproductor.pause();
            reproductorMaracas.pause();
           // pausado = true;
        }
    }

    // Manejamos el cambio en la aceleración lineal (sin contar la gravedad
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_GYROSCOPE:
                // Obtenemos la aceleración en el eje Z
                gyr_Z = event.values[2];
                gyr_X = event.values[0];
                gyr_Y = event.values[1];
                gyr_Z_text.setText(Float.toString(gyr_Z));
            break;

            case Sensor.TYPE_LINEAR_ACCELERATION:
                // Obtenemos la aceleración en el eje Z
                accel_Z = event.values[2];
                accel_X = event.values[0];
                accel_Y = event.values[1];
                accel_Z_text.setText(Float.toString(accel_Z));
            break;
        }

        if(gyr_Z < -2){
            girando = true;
        }

        if(accel_Z > 3){
            hitting = true;
        }

        if (girando && gyr_Z < 0.5) {
            if (gyr_X > gyr_Z || gyr_Z > gyr_Z) {
                girando = false;
            }
            else {
                reproductorMaracas.start();
                girando = false;
            }
        }

        // Si paramos, habremos golpeado
        if (hitting && accel_Z < 0.5){
            if(accel_Y > accel_Z || accel_X > accel_Z){
                hitting = false;
            }
            else {
                reproductor.start();
                hitting = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        reproductor.release();
        reproductor = null;
        reproductorMaracas.release();
        reproductorMaracas = null;
    }

}
