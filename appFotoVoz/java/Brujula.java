package com.npi.appfotovoz;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
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

/* Lo relativo a sensores está obtenido de las guías de la API de Android Developers:
   http://developer.android.com/intl/es/guide/topics/sensors/sensors_motion.html
   http://developer.android.com/intl/es/guide/topics/sensors/sensors_overview.html y
   http://developer.android.com/intl/es/reference/android/hardware/SensorManager.html
 */

/* Esta clase es la responsable de orientar al usuario: mostrar la brújula y animarla, indicando en un mensaje
   cómo tiene que orientarse (que será lo escuchado en la clase BrujulaVoz.java) e indicando con un
   mensaje en pantalla que ya lo ha hecho cuando lo consiga.

 */

public class Brujula extends AppCompatActivity implements SensorEventListener {

    // Views donde se cargarán las cadenas de texto y la brújula
    private TextView grados;
    private ImageView vientos;
    private TextView listened;
    private TextView message;

    // Sensor manager del dispositivo
    SensorManager sensorManager;

    // Sensores que se utilizarán
    Sensor magnetic_field_sensor;
    Sensor accelerometer_sensor;

    // Ángulo actual de la brújula
    private float current_degree = 0f;

    // Ángulo del movimiento de la flecha que señala al norte
    float new_degree;

    // Valores devueltos por los sensores
    float[] acel_data;
    float[] magnetic_data;

    // Valores para fijar y determinar el objetivo
    private float objective = 0f;
    private float tolerance = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializamos y enlazamos el view con el del xml
        setContentView(R.layout.activity_brujula);
        grados = (TextView) findViewById(R.id.grados);
        vientos = (ImageView) findViewById(R.id.imgViewCompass);
        listened = (TextView) findViewById(R.id.listened);
        message = (TextView) findViewById(R.id.message);

        // Inicilizamos los sensores y el sensorManager.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magnetic_field_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Obtenemos el punto cardinal y la tolerancia que se reciben de la clase BrujulaVoz.java
        // a través del Bundle.
        Intent intent = getIntent();
        Bundle b = intent.getBundleExtra(BrujulaVoz.EXTRA_BUNDLE);

        String cardinal_point = b.getString("Point");
        tolerance = b.getFloat("Tolerance");

        if(cardinal_point.equals("norte"))
            objective = 0f;
        else if (cardinal_point.equals("sur"))
            objective = 180f;
        else if (cardinal_point.equals("oeste"))
            objective = -90f;
        else if (cardinal_point.equals("este"))
            objective = 90f;

        // Mostramos por pantalla el objetivo al que le vamos a llevar y con qué tolerancia
        // (lo que se supone que ha dicho el usuario).
        listened.setText( cardinal_point + " " + Float.toString(tolerance));
    }

    @Override
    protected void onResume(){
        super.onResume();

        // Volvemos a recoger información de los sensores cuando la aplicación vuelve a ponerse en marcha.
        sensorManager.registerListener(this, accelerometer_sensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetic_field_sensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause(){
        super.onPause();
        // Paramos de recoger información de los sensores si la aplicación se pone en pausa.
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Cogemos los valores de los sensores
        if ( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER )
            acel_data = event.values;
        else if ( event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD )
            magnetic_data = event.values;

        if( (acel_data != null) && (magnetic_data != null)){
            float rotation_matrix[] = new float[16];
            // Calculamos la rotación del móvil y si dicho cálculo ha tenido éxito o no con la función
            // getRotationMatrix().
            boolean success = SensorManager.getRotationMatrix(rotation_matrix, null, acel_data, magnetic_data);

            if(success){
                float orientation[] = new float[3];
                // Obtenemos la orientación.
                SensorManager.getOrientation(rotation_matrix, orientation);
                new_degree = orientation[0] * (180 / (float) Math.PI) ;
            }
        }

        // Los grados van de 0 a 180, momento en el que van de 180 a 0 pero negativos.
        // Según los grados obtenidos, mostramos si es el norte, sur...
        if (new_degree < 22.5 && new_degree > -22.5)
            grados.setText(Float.toString(new_degree) + "º" + " N");
        else if(new_degree > 22.5 && new_degree < 68)
            grados.setText(Float.toString(new_degree) + "º" + " NE");
        else if(new_degree > 68 && new_degree < 112.5)
            grados.setText(Float.toString(new_degree) + "º" + " E");
        else if(new_degree > 112.5 && new_degree < 158)
            grados.setText(Float.toString(new_degree) + "º" + " SE");
        else if(new_degree > 158 || new_degree < -158)
            grados.setText(Float.toString(new_degree) + "º" + " S");
        else if(new_degree > -158 && new_degree < -112.5)
            grados.setText(Float.toString(new_degree) + "º" + " SO");
        else if(new_degree > -112.5 && new_degree < -68)
            grados.setText(Float.toString(new_degree) + "º" + " O");
        else if(new_degree > -68 && new_degree < -22.5)
            grados.setText(Float.toString(new_degree) + "º" + " NO");

        // Animamos la imagen de la brújula según los grados que ya tenía y
        // hacia los que tiene que ir.
        RotateAnimation ra = new RotateAnimation(
                current_degree,
                new_degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        // el tiempo durante el cual la animación se llevará a cabo
        ra.setDuration(1000);
        // establecer la animación después del final de la estado de reserva
        ra.setFillAfter(true);

        // Inicio de la animación
        vientos.startAnimation(ra);
        current_degree = -new_degree;

        // Cuando se orienta en la dirección indicada que había dado por voz, avisamos de que
        // ya se ha orientado como quería.
        if( Math.abs( new_degree - objective) < tolerance ||
                ( objective == 180f && Math.abs( objective + new_degree ) < tolerance )    ){

            message.setText(R.string.successful_msg);
        }
        else{
            message.setText(" ");
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
