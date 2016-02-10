package com.npi.appgpsqr;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

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

/* Para utilizar los mapas hemos seguido en un principio el tutorial presente en
   http://expocodetech.com/usar-google-maps-en-aplicaciones-android-mapa/ y le hemos
   hecho las modificaciones necesarias para adaptarlo a nuestras necesidades.
   El resto de información la hemos sacado de las guías de la API de android developers,
   en concreto, como ir rastreando las posiciones del GPS:
   http://developer.android.com/intl/es/training/location/retrieve-current.html#last-known
 */

/*
 * Clase para mostrar el mapa y realizar la navegación
 */
public class Map extends AppCompatActivity
        implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnClickListener{

    // GoogleMap que será mostrado
    GoogleMap mMap;

    // GoogleApiClient que realizará las llamadas necesarias para trabajar con el mapa
    private GoogleApiClient mGoogleApiClient;

    // Última posición conocida
    private Location mLastLocation;

    // LocationRequest para conocer la posición del usuario
    private LocationRequest mLocationRequest;

    // ArrayList con los Marker que vamos visitando
    private ArrayList<Marker> destiny_markers;

    // ArrayList con los puntos que habrá que recorrer
    private ArrayList<LatLong> destinys;

    // Índice del último punto visitado
    int actual_destiny = 0;

    // Polilínea que pintaremos finalmente en el mapa
    private Polyline polyline;
    private PolylineOptions path_locations;

    // Boolean que determinará si la Polyline ha sido pintada o no
    private boolean polyline_drawn = false;

    // Botón para añadir un nuevo punto en el recorrido
    private Button new_qr_button;

    // Botón para pintar la ruta seguida una vez finalizado el recorrido.
    private Button show_rute_button;

    //Creación de la Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        setUpMapIfNeeded();

        //Obtención de las coordenadas
        Intent intent = getIntent();
        Bundle b = intent.getBundleExtra(QR.EXTRA_BUNDLE);
        destinys = b.getParcelableArrayList("Coordinates");

        // Instanciación del ArrayList de Marker
        destiny_markers = new ArrayList<>();

        // Situación en el mapa del primer punto
        if(destinys != null && !destinys.isEmpty())
            setDestiny(0);

        // Creación de las peticiones de localización
        createLocationRequest();

        // Instanciación de la polilínea que será la ruta
        path_locations = new PolylineOptions();

        // Instanciación de los botones y asignación del ClickListener
        new_qr_button = (Button) findViewById(R.id.new_qr);
        new_qr_button.setOnClickListener(this);

        show_rute_button = (Button) findViewById(R.id.show_rute);
        show_rute_button.setOnClickListener(this);
        show_rute_button.setClickable(false);
    }

    private void setDestiny(int destiny){
        float lat = destinys.get(destiny).lat;
        float lng = destinys.get(destiny).lng;

        LatLng latlng = new LatLng(lat,lng);
        setMarker(latlng, R.string.label_marker + Float.toString(destiny), Float.toString(lat), Float.toString(lng));
    }

    //Activamos el mapa
    private void setUpMapIfNeeded() {
        // Configuramos el objeto GoogleMaps con valores iniciales.
        if (mMap == null) {
            //Instanciamos el objeto mMap a partir del MapFragment definido bajo el Id "map"
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            // Comprobamos si se ha obtenido correctamente una referencia al objeto GoogleMap
            if (mMap != null) {
                //Establecemos el tipo de mapa
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                // Comprobamos si se han dado los permisos necesarios
                if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                }
                //Activamos la capa MyLocation
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    // Creación de la petición de la localización del usuario
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        // Establecemos el intervalo de actualización
        mLocationRequest.setInterval(4000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
    }

    // Método para añadir un marcador en una posición
    private void setMarker(LatLng position, String title, String lat, String lon) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)                  //Agrega un titulo al marcador
                .snippet(lat + ", " + lon));   //Agrega información detalle relacionada con el marcador

        destiny_markers.add(marker);
    }


    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    // Método para añadir el LocationRequest a la lista de servicios
    protected void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {}
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //Comprobamos si se han dado los permisos
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        // Tomamos la última posición conocida
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            // Centramos el mapa en la posición inicial del usuario
            LatLng latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 17);
            mMap.animateCamera(cameraUpdate);
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.null_position_toast, Toast.LENGTH_SHORT);
            toast.show();
        }

        //Comenzamos la localización del usuario
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    //Método que controla el cambio de localización del usuario
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LatLng latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        // Se añade la nueva localización a la polilínea
        path_locations.add(latlng);

        // Comprobamos si hemos llegado al siguiente destino
        if( Math.abs(mLastLocation.getLatitude()-destinys.get(actual_destiny).lat) < 0.0001 &&
                Math.abs(mLastLocation.getLongitude()-destinys.get(actual_destiny).lng) < 0.0001){

            //Comprobamos si hemos llegado al último punto del recorrido
            if(actual_destiny == destinys.size()-1){
                show_rute_button.setClickable(true);
                Toast toast = Toast.makeText(getApplicationContext(), R.string.next_point_msg, Toast.LENGTH_SHORT);
                toast.show();
            }
            else{
                // Cambiamos el color del Marker de la última localización visitada
                destiny_markers.get(actual_destiny).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                // Seguimos el recorrido avanzando al siguiente punto
                actual_destiny++;
                setDestiny(actual_destiny);
                Toast toast = Toast.makeText(getApplicationContext(), R.string.final_msg, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    // Se responde al evento click en cada uno de los botones
    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.show_rute){
            if(!polyline_drawn){
                polyline = mMap.addPolyline(path_locations);
                polyline_drawn = true;
                show_rute_button.setText(R.string.hide_rute_button);
            }
            else {
                polyline.remove();
                polyline_drawn = false;
                show_rute_button.setText(R.string.show_rute_button);
            }
        }

    }
}
