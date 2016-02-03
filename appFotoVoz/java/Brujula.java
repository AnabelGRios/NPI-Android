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

/**
 * Created by jacin on 03/02/2016.
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
        setContentView(R.layout.activity_brujula);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magnetic_field_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        grados = (TextView) findViewById(R.id.grados);
        vientos = (ImageView) findViewById(R.id.imgViewCompass);
        listened = (TextView) findViewById(R.id.listened);
        message = (TextView) findViewById(R.id.message);

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

        listened.setText( cardinal_point + " " + Float.toString(tolerance));
    }

    @Override
    protected void onResume(){
        super.onResume();

        sensorManager.registerListener(this, accelerometer_sensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetic_field_sensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause(){
        super.onPause();

        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if ( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER )
            acel_data = event.values;
        else if ( event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD )
            magnetic_data = event.values;

        if( (acel_data != null) && (magnetic_data != null)){
            float rotation_matrix[] = new float[16];
            boolean success = SensorManager.getRotationMatrix(rotation_matrix, null, acel_data, magnetic_data);

            if(success){
                float orientation[] = new float[3];
                SensorManager.getOrientation(rotation_matrix, orientation);
                new_degree = orientation[0] * (180 / (float) Math.PI) ;
            }
        }

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

        if( Math.abs( new_degree - objective) < tolerance ||
                ( objective == 180f && Math.abs( objective + new_degree ) < tolerance )    ){

            message.setText("ENHORABUENA, LO HAS CONSEGUIDO");
        }
        else{
            message.setText(" ");
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
