package com.example.argel.brujula_gps;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public TextView txtDegree, txtLat, txtAlt;
    public String provider;
    public Location loc;
    public LocationManager manager;
    public Criteria criteria;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtDegree = findViewById(R.id.Contenido_degree);
        txtLat = findViewById(R.id.Contenido_latitude);
        txtAlt = findViewById(R.id.Contenido_altitude);

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        criteria = new Criteria();
        provider = manager.getBestProvider(criteria, true);
        try{
            manager.requestLocationUpdates(provider, 5, 1, ubicacionListener);
            Log.d("Prueba", "Entra");
        }catch(SecurityException e){
            e.printStackTrace();
        }

    }
    public LocationListener ubicacionListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            try{
                loc = manager.getLastKnownLocation(provider);
                Log.d("Locprueba", "loc");
                txtLat.setText(Double.toString(loc.getLatitude()));
                txtAlt.setText(Double.toString(loc.getAltitude()));
            }catch(SecurityException e){
                e.printStackTrace();
            }

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
    public void onSensorChanged(SensorEvent event){
        float degree = Math.round(event.values[0]);
        txtDegree.setText(Float.toString(degree));
    }
}
