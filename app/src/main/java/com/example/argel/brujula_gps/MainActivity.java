package com.example.argel.brujula_gps;

import android.app.Activity;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public TextView txtDegree, txtLat, txtAlt, txtHora, txtDia;
    public Location loc;
    public LocationManager myLocationManager;
    public CustomLocationListener myLocationListener;
    public Criteria criteria;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    //Poner dirección del dispositivo aquí
    public String DEVICE_ADDRESS = "00:15:83:35:82:1B";
    private BluetoothDevice device;
    public boolean found;
    private final UUID PORT_UUID = UUID.fromString("9fe884f0-d499-11e7-8f1a-0800200c9a66");
    private BluetoothSocket socket;
    private OutputStream outputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String currentDateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String currentTimeString = new SimpleDateFormat("HH:mm:ss").format(new Date());
        txtDegree = findViewById(R.id.Contenido_degree);
        txtLat = findViewById(R.id.Contenido_latitude);
        txtAlt = findViewById(R.id.Contenido_altitude);
        txtHora = findViewById(R.id.Hora_content);
        txtDia = findViewById(R.id.Dia_content);

        txtHora.setText(currentTimeString);
        txtDia.setText(currentDateString);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
        {
            Toast.makeText(getApplicationContext(), "No Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enable_adapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enable_adapter, 0);
        }

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty()){
            Toast.makeText(getApplicationContext(),"Formar par entre dispositivos", Toast.LENGTH_SHORT).show();
        }else{
            for( BluetoothDevice iterator : bondedDevices){
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device=iterator;

                    found = true;

                    break;
                }

            }
        }

        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String datos = "a";
        try {
            outputStream.write(datos.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocationListener = new CustomLocationListener();

        try{
            myLocationManager.requestLocationUpdates(myLocationManager.NETWORK_PROVIDER, 5, 1, myLocationListener);
            Log.d("Prueba", "Entra");
        }catch(SecurityException e){
            e.printStackTrace();
        }

    }

    public class CustomLocationListener implements LocationListener {

       public void onLocationChanged(Location location) {
            try{
                myLocationManager.removeUpdates(myLocationListener);
                txtLat.setText(Double.toString(location.getLatitude()));
                txtAlt.setText(Double.toString(location.getAltitude()));
            }catch(SecurityException e){
                e.printStackTrace();
            }

        }

        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        public void onProviderEnabled(String s) {

        }

        public void onProviderDisabled(String s) {

        }
    };
    public void onSensorChanged(SensorEvent event){
        float degree = Math.round(event.values[0]);
        txtDegree.setText(Float.toString(degree));
    }
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }
}
