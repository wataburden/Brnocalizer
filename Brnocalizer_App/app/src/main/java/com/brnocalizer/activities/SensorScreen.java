package com.brnocalizer.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.brnocalizer.R;
import com.brnocalizer.communication.Protocol;
import com.brnocalizer.worker.AppData;
import com.brnocalizer.worker.Relay;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class SensorScreen extends Activity implements SurfaceHolder.Callback {

    //Text view containing the current value of the acceleration (x,y,z)
    private TextView accX;
    private TextView accY;
    private TextView accZ;

    //Text view containing the current value of the magnetic field (x,y,z)
    private TextView magX;
    private TextView magY;
    private TextView magZ;

    //Text view containing the current value of the gyroscope (x,y,z)
    private TextView gyrX;
    private TextView gyrY;
    private TextView gyrZ;

    //Text view containing the current latitude and longitude of the device
    private TextView GPSView;

    //object manager of the GPS and sensors
    private SensorManager sensorManager;
    private LocationManager locationManager;

    //Sensors registered
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magneticField;

    //settings for the different info taken (sensors, GPS, video)
    private static int sensorRate;
    private static long GPSRate;
    private static int FPSRate;

    //object needed to record a video stream
    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording = false;

    private static final int PERMISSION_FINE_LOCATION = 1;

    private Messenger serviceMessenger = null;

    private Camera camera = null;


    /**
     * listener of the accelerometer sensor
     */
    private SensorEventListener event1 = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (serviceMessenger != null) {
                try {
                    Bundle b = new Bundle();
                    b.putString("data", sensorEvent.values[0]+Protocol.DATA_SPLITTER +sensorEvent.values[1]+Protocol.DATA_SPLITTER +sensorEvent.values[2]+Protocol.SEPARATOR);
                    Message msg = Message.obtain(null, AppData.ACCELEROMETER, 0);
                    msg.setData(b);
                    serviceMessenger.send(msg);
                }
                catch (RemoteException e) {
                }
            }
            setAccelerometerView("X : "+sensorEvent.values[0],"Y : "+sensorEvent.values[1], "Z : "+sensorEvent.values[2]);
            
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    /**
     * Listener of the linear accelerometer sensor
     */
    private SensorEventListener event2 = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (serviceMessenger != null) {
                try {
                    Bundle b = new Bundle();
                    b.putString("data", sensorEvent.values[0]+Protocol.DATA_SPLITTER +sensorEvent.values[1]+Protocol.DATA_SPLITTER +sensorEvent.values[2]+ Protocol.SEPARATOR);
                    Message msg = Message.obtain(null, AppData.GYROSCOPE, 0);
                    msg.setData(b);
                    serviceMessenger.send(msg);
                }
                catch (RemoteException e) {
                }
            }
            setGyroscopeView("X : "+sensorEvent.values[0],"Y : "+sensorEvent.values[1], "Z : "+sensorEvent.values[2]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    /**
     * Listener of the magnectic field sensor
     */
    private SensorEventListener magneticEvent = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (serviceMessenger != null) {
                try {
                    Bundle b = new Bundle();
                    b.putString("data", sensorEvent.values[0]+Protocol.DATA_SPLITTER +sensorEvent.values[1]+Protocol.DATA_SPLITTER +sensorEvent.values[2]+Protocol.SEPARATOR);
                    Message msg = Message.obtain(null, AppData.MAGNETIC, 0);
                    msg.setData(b);
                    serviceMessenger.send(msg);
                }
                catch (RemoteException e) {
                }
            }
            setMagneticView("X : "+sensorEvent.values[0],"Y : "+sensorEvent.values[1], "Z : "+sensorEvent.values[2]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            setGPSView("lat : "+location.getLatitude()+" / lon : "+location.getLongitude());
            if (serviceMessenger != null) {
                try {
                    Bundle b = new Bundle();
                    b.putString("data", location.getLatitude()+","+location.getLongitude()+","+location.getAltitude()+" ");
                    Message msg = Message.obtain(null, AppData.GPS_DATA, 0);
                    msg.setData(b);
                    serviceMessenger.send(msg);
                }
                catch (RemoteException e) {
                }
            }

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            switch (i){
                case LocationProvider.OUT_OF_SERVICE :
                    Toast.makeText(getApplicationContext(), AppData.PROVIDER_OUT_OF_SERVICE, Toast.LENGTH_SHORT).show();
                    break;

                case LocationProvider.AVAILABLE:
                    Toast.makeText(getApplicationContext(), AppData.PROVIDER_AVAILABLE, Toast.LENGTH_SHORT).show();
                    break;

                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Toast.makeText(getApplicationContext(), AppData.PROVIDER_UNAVAILABLE, Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    /**
     * Listener set to receive the disconnection info from Postman
     */
    private BroadcastReceiver disconnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            unregisterBusiness();

            Log.d(AppData.DEBUG_SENSOR_SCREEN, "onReceive: Receiving result");

            //Construction of a popup if the connection was interrupted
            AlertDialog.Builder adb = new AlertDialog.Builder(SensorScreen.this);

            adb.setTitle(AppData.DISCONNECTION_TITLE);

            adb.setPositiveButton("Validate", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    if (ActivityCompat.checkSelfPermission(SensorScreen.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(SensorScreen.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(AppData.DEBUG_SENSOR_SCREEN, "registerBusiness: permission granted");
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPSRate, 0, locationListener);
                        sensorManager.registerListener(magneticEvent, magneticField, sensorRate);
                        sensorManager.registerListener(event1, accelerometer, sensorRate);
                        sensorManager.registerListener(event2, gyroscope, sensorRate);
                    }

                    if (serviceMessenger != null) {
                        try {
                            Message msg = Message.obtain(null, AppData.APP_END, 0);
                            serviceMessenger.send(msg);
                        }
                        catch (RemoteException e) {
                        }
                    }
                    Intent intent = new Intent(SensorScreen.this, Welcome.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }
            });

            //The user can't cancel the popup
            adb.setCancelable(false);

            adb.show();

        }
    };

    /**
     * This is the listener set to listen to the answer of the service when the
     * activity ask for the settings.
     */
    private BroadcastReceiver settingsListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        sensorRate = intent.getIntExtra(AppData.SENSOR_SETTINGS, SensorManager.SENSOR_DELAY_NORMAL);
        GPSRate = intent.getLongExtra(AppData.GPS_SETTINGS, 0);
        FPSRate = intent.getIntExtra(AppData.FPS_SETTINGS, 30);

        registerBusiness();

        }
    };




    /**
     * creation of the screen and the different objects needed during the execution of the activity
     * @param savedInstanceState :
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_screen);

        accX= (TextView) findViewById(R.id.AccX);
        accY= (TextView) findViewById(R.id.AccY);
        accZ= (TextView) findViewById(R.id.AccZ);

        magX= (TextView) findViewById(R.id.MagX);
        magY= (TextView) findViewById(R.id.MagY);
        magZ= (TextView) findViewById(R.id.MagZ);


        gyrX= (TextView) findViewById(R.id.GyrX);
        gyrY= (TextView) findViewById(R.id.GyrY);
        gyrZ= (TextView) findViewById(R.id.GyrZ);

        GPSView = (TextView) findViewById(R.id.GPSText);


        camera = Camera.open();
        camera.setDisplayOrientation(90);
        camera.unlock();

        SurfaceView cameraView = (SurfaceView) findViewById(R.id.surfaceView);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        recorder = new MediaRecorder();
        recorder.setCamera(camera);
        initRecorder();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        getLastLocation();

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


    }

    /**
     * called when the activity pauses
     */
    @Override
    protected void onPause() {
        super.onPause();

        //unregister the service and the broadcastReceivers
        LocalBroadcastManager.getInstance(SensorScreen.this).unregisterReceiver(disconnectionReceiver);
        LocalBroadcastManager.getInstance(SensorScreen.this).unregisterReceiver(settingsListener);
        getApplicationContext().unbindService(connection);

        unregisterBusiness();

        camera.release();
    }

    /**
     * called when the activity resumes
     */
    @Override
    protected void onResume() {
        super.onResume();

        //Set the screen in immersive mode
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        //register the broadcast receivers
        LocalBroadcastManager.getInstance(this).registerReceiver(disconnectionReceiver, new IntentFilter(AppData.DISCONNECTION_INTENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(settingsListener, new IntentFilter(AppData.SETTINGS_INTENT));

        // Bind the activity to the service
        final Intent intent = new Intent(this, Relay.class);
        getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);


    }

    /**
     * asking the service for the settings to apply to the different sensors, GPS, and video stream
     */
    public void askForSettings(){
        try {
            Message msg = Message.obtain(null, AppData.ASK_SETTINGS, 0);
            serviceMessenger.send(msg);
        }
        catch (RemoteException e) {
        }
    }

    /**
     * This is initiating the video recorder
     */
    private void initRecorder() {

        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);
        recorder.setOrientationHint(90);
        recorder.setOutputFile(new File(AppData.PATH, AppData.FILENAME).getPath());
        recorder.setMaxDuration(50000); // 50 seconds
        recorder.setMaxFileSize(50000000); // Approximately 50 megabytes
        //recorder.setVideoFrameRate(FPSRate);


    }

    /**
     * This is preparing the media recorder
     */
    private void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }


    public void surfaceCreated(SurfaceHolder holder) {
        prepareRecorder();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    /**
     * Called when the surfaceView is destroyed
     * @param holder :
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
        finish();
    }

    /**
     * Called when the connection to the service changes
     */
    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(AppData.DEBUG_SENSOR_SCREEN, "onServiceConnected: connected to the service");
            serviceMessenger = new Messenger(service);
            askForSettings();
        }

        public void onServiceDisconnected(ComponentName className) {
            serviceMessenger = null;
        }
    };

    /**
     * This is getting the last GPS location saved by the device
     */
    public void getLastLocation(){
        Log.d(AppData.DEBUG_SENSOR_SCREEN, "getLastLocation: trying to get last location");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(AppData.DEBUG_SENSOR_SCREEN, "getLastLocation: GPS permission not granted");
            askPermission();
            return;
        }
        if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null){
            Log.d(AppData.DEBUG_SENSOR_SCREEN, "getLastLocation: getting last location ...");
            setGPSView("lat: "+locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude()+" / lon : "+locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude());
        }
    }


    /**
     * Method used by API 23+
     * This is asking the user the permission to use GPS location of the device
     */
    public void askPermission() {

        Log.d(AppData.DEBUG_SENSOR_SCREEN, "askPermission: permission not granted");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(getApplicationContext(), AppData.ASK_LOCATION_PERMISSION, Toast.LENGTH_SHORT).show();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        }
    }


    /**
     * registration of the different listeners
     */
    public void registerBusiness() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(AppData.DEBUG_SENSOR_SCREEN, "registerBusiness: permission granted");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPSRate, 0, locationListener);
            sensorManager.registerListener(magneticEvent, magneticField, sensorRate);
            sensorManager.registerListener(event1, accelerometer, sensorRate);
            sensorManager.registerListener(event2, gyroscope, sensorRate);

            recorder.start();
        }
    }

    /**
     * listeners' destruction
     */
    public void unregisterBusiness() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askPermission();
            return;
        }
        locationManager.removeUpdates(locationListener);
        sensorManager.unregisterListener(event1);
        sensorManager.unregisterListener(event2);
        sensorManager.unregisterListener(magneticEvent);

    }

    /**
     *  changes the text describing the values of the accelerometer textview
     * @param X :string value of X axis
     * @param Y :string value of Y axis
     * @param Z :string value of Z axis
     */
    public void setAccelerometerView(String X, String Y, String Z){
        accX.setText(X);
        accY.setText(Y);
        accZ.setText(Z);
    }

    /**
     * changes the text describing the values of the gyroscope textview
     * @param X :string value of X axis
     * @param Y :string value of Y axis
     * @param Z :string value of Z axis
     */
    public void setGyroscopeView(String X, String Y, String Z){
        gyrX.setText(X);
        gyrY.setText(Y);
        gyrZ.setText(Z);
    }

    /**
     *  changes the text describing the values of the magnetic field textview
     * @param X :string value of X axis
     * @param Y :string value of Y axis
     * @param Z :string value of Z axis
     */
    public void setMagneticView(String X, String Y, String Z){
        magX.setText(X);
        magY.setText(Y);
        magZ.setText(Z);
    }

    /**
     *  changes the text describing the values of the GPS textview
     * @param change : String to insert in the text view
     */
    public void setGPSView(String change){
        GPSView.setText(change);
    }

    /**
     * Listener set to receive the answer of the user to the permission request
     * @param requestCode : identifying the request that the user has answered
     * @param permissions :
     * @param grantResults :
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {


        switch (requestCode){
            case PERMISSION_FINE_LOCATION :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                   registerBusiness();

                } else {

                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
            
            default :
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
                

        }
    }

    /**
     * called when the button "settings" is pressed
     * @param view : identifying the activity's view
     */
    public void goToSettings(View view){
        Intent intent = new Intent(this, SetupScreen.class);
        intent.putExtra("activity", SetupScreen.SENSOR_SCREEN);
        startActivity(intent);
        finish();
    }

    /**
     * called when the button "stop" is pressed
     * @param view : identifying the activity's view
     */
    public void stopAction(View view){
        unregisterBusiness();
        recorder.stop();
        if (serviceMessenger != null) {
            try {
                Message msg = Message.obtain(null, AppData.STOP, 0);
                serviceMessenger.send(msg);
            }
            catch (RemoteException e) {
            }
        }

        Intent intent = new Intent (this, SendingVideoScreen.class);
        startActivity(intent);
        finish();

    }
    
}