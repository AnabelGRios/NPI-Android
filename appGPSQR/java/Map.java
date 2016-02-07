package com.npi.appgpsqr;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
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

import java.text.DateFormat;
import java.util.Date;


public class Map extends AppCompatActivity
        implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{
    GoogleMap mMap;
    GoogleApiClient apiClient;
    private LatLng destino = null;
    private GoogleApiClient mGoogleApiClient;
    Location mLastLocation = null;
    LocationRequest mLocationRequest;
    String mLastUpdateTime;

    private PolylineOptions path_locations;


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
        Intent intent = getIntent();
        Bundle b = intent.getBundleExtra(QR.EXTRA_BUNDLE);
        destino = new LatLng(b.getFloat("Latitud"), b.getFloat("Longitud"));
        String lat = Float.toString(b.getFloat("Latitud"));
        String lon = Float.toString(b.getFloat("Longitud"));
        setMarker(destino, "Destino", lat, lon);

        createLocationRequest();
        path_locations = new PolylineOptions();
    }

    private void setUpMapIfNeeded() {
// Configuramos el objeto GoogleMaps con valores iniciales.
        if (mMap == null) {
            //Instanciamos el objeto mMap a partir del MapFragment definido bajo el Id "map"
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            // Chequeamos si se ha obtenido correctamente una referencia al objeto GoogleMap
            if (mMap != null) {
                // El objeto GoogleMap ha sido referenciado correctamente
                //ahora podemos manipular sus propiedades

                //Seteamos el tipo de mapa
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                //Activamos la capa o layer MyLocation
                if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},2);
                   /* Toast toast = Toast.makeText(getApplicationContext(), "Permisos inválidos", Toast.LENGTH_SHORT);
                    toast.show();*/
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Permisos válidos", Toast.LENGTH_SHORT);
                    toast.show();
                }
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
    }

    private void setMarker(LatLng position, String titulo, String lat, String lon) {
        // Agregamos marcadores para indicar sitios de interés.
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(titulo)  //Agrega un titulo al marcador
                .snippet(lat + ", " + lon));   //Agrega información detalle relacionada con el marcador
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    protected void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {}
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            LatLng latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 17);
            mMap.animateCamera(cameraUpdate);
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(), "Posición nula", Toast.LENGTH_SHORT);
            toast.show();
        }

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        LatLng latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        path_locations.add(latlng);

        if( Math.abs(mLastLocation.getLatitude()-destino.latitude) < 0.0001 &&
                Math.abs(mLastLocation.getLongitude()-destino.longitude) < 0.0001){
            mMap.addPolyline(path_locations);
            Toast toast = Toast.makeText(getApplicationContext(), "Ha llegado a su destino", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

}
