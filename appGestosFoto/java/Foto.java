package com.npi.appgestosfoto;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
 * Clase realizada siguiendo el tutorial de Android para el uso de la cámara:
 *  http://developer.android.com/intl/es/guide/topics/media/camera.html
 * Inicio de la cuenta atrás, implementado en el método setTimer(), basado en
 * http://mobiledevtuts.com/android/android-sdk-how-to-make-an-automatic-snapshot-android-app/
 */

/*
 * Clase para tomar una foto
 */
public class Foto extends AppCompatActivity {

    // Camera que tomará la foto
    private Camera mCamera;

    // Vista previa donde mostramos la imagen que tomaríamos en cada momento
    private CameraPreview mPreview;

    // Método para guardar la imagen tomada
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File destination = new File(Environment.getExternalStorageDirectory() + "/Pictures", "mPicture.jpg");
            try{
                Bitmap userImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                FileOutputStream out = new FileOutputStream(destination);
                userImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
                Toast.makeText(Foto.this,"Se ha tomado la foto y guardado en \"/Pictures\"", Toast.LENGTH_SHORT).show();
                finish();
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }
    };


    // Creación de una instancia de la clase
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto);

        // Obtención de la instancia de la cámara
        mCamera = getCameraInstance();

        // Creación de la vista previa de la cámara y puesta como el contenido de la Activity
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Giramos la cámara
        mCamera.setDisplayOrientation(90);

        // Iniciamos la cuenta atrás
        startTimer();
    }

    // Obtención de una instancia de la cámara
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            // Intentamos obtener la cámara
            c = Camera.open();
        } catch (Exception e) {
            // Camera no está disponible (está en uso o no existe)
            Toast.makeText(this, R.string.cam_not_available_msg, Toast.LENGTH_SHORT).show();
            finish();
        }
        return c;
    }


    // Inicio de la cuenta atrás
    public void startTimer() {
        new CountDownTimer(3000,1000) {
            // Cuando pasan tres segundos, se toma la foto
            @Override
            public void onFinish() {
                mCamera.takePicture(null, null, mPicture);
            }

            @Override
            public void onTick(long msUntilFinished){
            }
        }.start();
    }
}
