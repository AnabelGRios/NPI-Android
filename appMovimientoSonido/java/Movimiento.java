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
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;

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
 * Clase que recoge un movimiento usando el acelerómetro y reproduce un sonido atendiendo a éste;
 * y recoge la velocidad al girar el dispositivo y reproduce un sonido distinto al hacerlo.
 */
public class Movimiento extends AppCompatActivity implements SensorEventListener{

    // Administrador de los sensores
    private SensorManager mSensorManager;

    // Sensor con el que sabremos la aceleración que sufre el dispositivo
    // y sensor con el que sabremos la velocidad angular (giroscopio).
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    // Reproductores del sonido
    private MediaPlayer reproductor;
    private MediaPlayer reproductorMaracas;

    // boolean para saber si se está golpeando el tambor o
    // si se está girando el dispositivo para tocar las maracas
    private boolean hitting = false;
    private boolean girando = false;

    // TextView donde se muestra la aceleración sufrida en el eje Z,
    // la velocidad angular del eje Z y los mensajes fijos explicativos
    TextView gyr_Z_text;
    TextView accel_Z_text;
    TextView msg_gyr, msg_accel;

    // Variables en las que guardaremos los giros y aceleraciones en
    // cada eje.
    float gyr_Z, accel_Z, accel_X, accel_Y, gyr_X, gyr_Y;


    // Creación de la Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimiento);

        // Instanciación del SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Obtención del acelerómetro y giroscopio
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Creación de los reproductores
        reproductor = MediaPlayer.create(this, R.raw.tomacoustic01);
        reproductorMaracas = MediaPlayer.create(this, R.raw.maracas);

        // Instanciación de los TextView
        gyr_Z_text = (TextView) findViewById(R.id.gyr_Z);
        accel_Z_text = (TextView) findViewById(R.id.accel_Z);
        msg_gyr = (TextView) findViewById(R.id.msg_gyr);
        msg_accel = (TextView) findViewById(R.id.msg_accel);

        // Ponemos los mensajes explicativos, que no variarán a lo largo
        // de la ejecución de la aplicación.
        msg_gyr.setText(R.string.message_gyr);
        msg_accel.setText(R.string.message_accel);
    }

    // Obtenemos los sensores cuando volvemos a la aplicación
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
        }
    }

    // Manejamos el cambio en la aceleración lineal (sin contar la gravedad)
    // y la velocidad angular que estén presentes en el dispositivo.
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_GYROSCOPE:
                // Obtenemos la velocidad angular en todos los ejes
                gyr_Z = event.values[2];
                gyr_X = event.values[0];
                gyr_Y = event.values[1];

                // Mostramos por pantalla la velocidad angular del eje Z
                gyr_Z_text.setText(Float.toString(gyr_Z));
            break;

            case Sensor.TYPE_LINEAR_ACCELERATION:
                // Obtenemos la aceleración en todos los ejes.
                accel_Z = event.values[2];
                accel_X = event.values[0];
                accel_Y = event.values[1];

                // Mostramos por pantalla la aceleración en el eje Z.
                accel_Z_text.setText(Float.toString(accel_Z));
            break;
        }

        // Estaremos girando en el eje Z cuando la velocidad sea mayor que menos 5 (estemos girando hacia
        // la derecha) y el giro registrado en los otros ejes sea menor que el registrado en el eje Z.
        if(gyr_Z < -5 && Math.abs(gyr_Z) > Math.abs(gyr_X) && Math.abs(gyr_Z) > Math.abs(gyr_Y)){
            girando = true;
        }

        // Estaremos moviendo el dispositivo en el eje Z cuando la aceleración sea mayor de 5 y la
        // aceleración en el resto de los ejes sea menor que la aceleración en el eje Z.
        if(accel_Z > 5 && accel_Z > accel_X && accel_Z >accel_Y){
            hitting = true;
        }

        // Si estamos girando, reproducimos el sonido y volvemos a poner la variable
        // girando a false.
        if (girando) {
            reproductorMaracas.start();
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.repr_maracas, Toast.LENGTH_SHORT);
            toast.show();
            girando = false;
        }

        // Si estamos golpeando y no estamos girando, reproducimos el sonido y ponemos
        // la variable hitting a false.
        if (hitting && gyr_X < 0.5 && gyr_Y < 0.5 && gyr_Z < 0.5){
            reproductor.start();
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.repr_tambor, Toast.LENGTH_SHORT);
            toast.show();
            hitting = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // Al dejar la aplicación, liberamos los reproductores y los ponemos a null.
    @Override
    public void onDestroy() {
        super.onDestroy();
        reproductor.release();
        reproductor = null;
        reproductorMaracas.release();
        reproductorMaracas = null;
    }

}
