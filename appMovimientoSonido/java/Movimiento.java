package com.npi.appmovimientosonido;

import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Movimiento extends AppCompatActivity implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private MediaPlayer reproductor;

    private boolean golpeando = false;

    TextView acel_Z;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimiento);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        reproductor = new MediaPlayer();

        acel_Z = (TextView) findViewById(R.id.acel_Z);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_LINEAR_ACCELERATION:
                acel_Z.setText(Float.toString(event.values[2]));
                if(event.values[2] >3){
                    golpeando = true;
                }
                else if (golpeando && event.values[2] < 0.5){
                    golpeando = false;

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
