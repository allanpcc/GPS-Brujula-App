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
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public TextView txtDegree, txtLat, txtAlt, txtHora, txtDia, txtAngX, txtAngY;
    public Location loc;
    public LocationManager myLocationManager;
    public double altCoord, latCoord;
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

    private int currentHour = 1;
    private int currentMonth = 1;
    private int currentDay =1;

    private int xDegree = 0;
    private int yDegree = 0;

    private String currentTimeString = "";
    private String currentDateString = "";

    private boolean isXDegInc = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtDegree = findViewById(R.id.Contenido_degree);
        txtLat = findViewById(R.id.Contenido_latitude);
        txtAlt = findViewById(R.id.Contenido_altitude);
        txtHora = findViewById(R.id.Hora_content);
        txtDia = findViewById(R.id.Dia_content);
        txtAngX = findViewById(R.id.txtAngX);
        txtAngY = findViewById(R.id.txtAngY);

        setSocket();

        setLocationManager();

        sendDegrees();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    public class CustomLocationListener implements LocationListener {

       public void onLocationChanged(Location location) {
            try{
                myLocationManager.removeUpdates(myLocationListener);

                setLocationTexts(location);
                setDateTexts();

                xDegree = (int) getHorizontalDegree();
                yDegree = (int) getVerticalDegree();

                sendDegrees();
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

    public void setSocketAction(View view){
        setSocket();
    }

    private void setSocket() {
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
            socket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
            socket.connect();
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            makeToast("Bluetooth done goof'd.");
            e.printStackTrace();
        }

        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setLocationManager() {
        myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocationListener = new CustomLocationListener();

        try{
            myLocationManager.requestLocationUpdates(myLocationManager.NETWORK_PROVIDER, 5, 1, myLocationListener);
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    private double getHorizontalDegree() //ASIMUTAL
    {
        // TODO Change to degrees
        double degCosNum = (getSinFromDegree(getDeclinationDegree())-(getSinFromDegree(latCoord)*getSinFromDegree(altCoord)));
        double degCosDen = (getCosFromDegree(latCoord)*getCosFromDegree(altCoord));
        return degCosNum/degCosDen;
    }

    private double getDeclinationDegree()
    {
        return getCosFromDegree((currentDay + 10) * 0.9863) * -23.44; // 0.9863 = 365/360
                                                                    // -23.44 = inclinacion eje tierra en grados
    }

    private double getSinFromDegree(Double degree){
        return Math.sin(Math.toRadians(degree));
    }

    private double getCosFromDegree(Double degree){
        return Math.cos(Math.toRadians(degree));
    }

    private double getVerticalDegree() {
        return 0.0;
    }

    public void sendDegrees() {
        String datos = xDegree + "," + yDegree;

        try {
            outputStream.write(datos.getBytes());
        } catch (IOException e) {
            makeToast("Bluetooth disconnected.");
            e.printStackTrace();
        }
    }

    private void setLocationTexts(Location location){
        latCoord = location.getLatitude();
        altCoord = location.getAltitude();

        txtLat.setText(Double.toString(location.getLatitude()));
        txtAlt.setText(Double.toString(location.getAltitude()));

        txtAngX.setText(Double.toString(getHorizontalDegree()));
        txtAngY.setText(Double.toString(getVerticalDegree()));
    }

    private void setDateTexts(){
        Calendar now = Calendar.getInstance();

        currentHour = now.get(Calendar.HOUR_OF_DAY);
        currentMonth = now.get(Calendar.MONTH);
        currentDay = now.get(Calendar.DAY_OF_YEAR);

//        currentDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        currentDateString = Integer.toString(currentMonth);
//        currentTimeString = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        currentTimeString = Integer.toString(currentHour);

        isXDegInc = currentMonth < 7;

        txtHora.setText(currentTimeString);
        txtDia.setText(currentDateString);
    }

    public void addAnHour(View view) {
        currentHour++;

        yDegree = yDegree + 180/16;

        if(currentHour > 24) {
            currentHour = 0;
        }

        if(yDegree > 180){
            yDegree = 0;
        }

        txtHora.setText(Integer.toString(currentHour));

        sendDegrees();
    }

    public void addMonth(View view){
        currentMonth++;

        if(currentMonth > 12){
            currentMonth = 1;
        }

        int step = 180/6;

        if(xDegree + step > 180){
            isXDegInc = false;
        }

        if(xDegree - step < 0){
            isXDegInc = true;
        }

        if(isXDegInc){
            xDegree += step;
        } else {
            xDegree -= step;
        }

        txtDia.setText(Integer.toString(currentMonth));

        sendDegrees();
    }

    private void makeToast(CharSequence msg){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, msg, duration);
        toast.setGravity(Gravity.TOP, 0, 150);
        toast.show();
    }
}
