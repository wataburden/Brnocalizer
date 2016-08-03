package com.brnocalizer.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.brnocalizer.R;
import com.brnocalizer.worker.AppData;
import com.brnocalizer.worker.Relay;
import com.brnocalizer.worker.Settings;

public class SetupScreen extends Activity {

    private Messenger serviceMessenger = null;
    private String previousActivity;

    private EditText sensorText;
    private EditText GPSText;
    private EditText FPSText;

    public static final String SENSOR_SCREEN = "SensorScreen";
    public static final String STARTER_SCREEN = "StarterScreen";

    /**
     * Listener of the answer of the service to the request of the settings
     */
    private BroadcastReceiver settingsListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch(intent.getIntExtra(AppData.SENSOR_SETTINGS, SensorManager.SENSOR_DELAY_NORMAL)){
                case SensorManager.SENSOR_DELAY_NORMAL:
                    sensorText.setHint(String.valueOf(Settings.NORMAL_RATE));
                    break;

                case SensorManager.SENSOR_DELAY_FASTEST:
                    sensorText.setHint(String.valueOf(Settings.FASTEST_RATE));
                    break;

                case SensorManager.SENSOR_DELAY_UI:
                    sensorText.setHint(String.valueOf(Settings.UI_RATE));
                    break;

                case SensorManager.SENSOR_DELAY_GAME:
                    sensorText.setHint(String.valueOf(Settings.GAME_RATE));
                    break;

                default:
                    sensorText.setHint(String.valueOf(intent.getIntExtra(AppData.SENSOR_SETTINGS, SensorManager.SENSOR_DELAY_NORMAL)));
                    break;
            }

            GPSText.setHint(String.valueOf(intent.getLongExtra(AppData.GPS_SETTINGS, 0)));
            FPSText.setHint(String.valueOf(intent.getIntExtra(AppData.FPS_SETTINGS, 30)));


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen);

        previousActivity = getIntent().getStringExtra("activity");

        sensorText = (EditText) findViewById(R.id.sensorEdit);
        GPSText = (EditText) findViewById(R.id.GPSEdit);
        FPSText = (EditText) findViewById(R.id.FPSEdit);


    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(settingsListener);
        getApplicationContext().unbindService(connection);
    }

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        LocalBroadcastManager.getInstance(this).registerReceiver(settingsListener, new IntentFilter(AppData.SETTINGS_INTENT));


        final Intent intent = new Intent(this, Relay.class);
        getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                askForSettings();
            }
        }, 1000L);

    }

    public void askForSettings(){
        try {
            Message msg = Message.obtain(null, AppData.ASK_SETTINGS, 0);
            serviceMessenger.send(msg);
        }
        catch (RemoteException e) {
        }
    }


    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            serviceMessenger = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            serviceMessenger = null;
        }
    };

    /**
     * Setting a specific action when the back button is pressed
     */
    @Override
    public void onBackPressed() {
        AlertDialog.Builder adb = new AlertDialog.Builder(SetupScreen.this);

        adb.setTitle(AppData.ASK_SAVE_CHANGES);

        adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                saveChanges();
                getBackToBusiness();

            }
        });

        adb.setNegativeButton("No", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getBackToBusiness();
            }
        });

        if (sensorText.getText().length()!= 0 || GPSText.getText().length()!= 0 || FPSText.getText().length()!= 0){
            adb.show();
        }else{
            getBackToBusiness();
        }

    }

    /**
     * Action to execute when the button "save changes" is pressed
     * @param view : identifying the activity's view
     */
    public void clickSaveChanges(View view){
        saveChanges();
    }

    /**
     * Saving the data id=f some changes have been made
     */
    public void saveChanges(){


        if (sensorText.getText().length()!= 0){

            int rate = Integer.parseInt(sensorText.getText().toString());
            if (serviceMessenger != null) {
                try {
                    Bundle b = new Bundle();
                    b.putInt("rate", rate);
                    Message msg = Message.obtain(null, AppData.SENSOR_RATE, 0);
                    msg.setData(b);
                    serviceMessenger.send(msg);
                }
                catch (RemoteException e) {
                }
            }

        }

        if (GPSText.getText().length()!= 0){

            long rate = Long.parseLong(GPSText.getText().toString());
            if (serviceMessenger != null) {
                try {
                    Bundle b = new Bundle();
                    b.putLong("rate", rate);
                    Message msg = Message.obtain(null, AppData.GPS_RATE, 0);
                    msg.setData(b);
                    serviceMessenger.send(msg);
                }
                catch (RemoteException e) {
                }
            }

        }

        if (FPSText.getText().length()!= 0){

            int rate = Integer.parseInt(FPSText.getText().toString());
            if (serviceMessenger != null) {
                try {
                    Bundle b = new Bundle();
                    b.putInt("rate", rate);
                    Message msg = Message.obtain(null, AppData.FPS_RATE, 0);
                    msg.setData(b);
                    serviceMessenger.send(msg);
                }
                catch (RemoteException e) {
                }
            }

        }

        if (sensorText.getText().length()== 0 || GPSText.getText().length()== 0 || FPSText.getText().length()== 0){
            finish();
            startActivity(getIntent());
        }else{
            Toast.makeText(getApplicationContext(), AppData.NO_CHANGES, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * returning to the previous activity
     */
    public void getBackToBusiness(){

        Intent intent;

        switch(previousActivity) {

            case SENSOR_SCREEN:
                intent = new Intent(this, SensorScreen.class);
                startActivity(intent);
                finish();
                break;

            case STARTER_SCREEN :
                intent = new Intent(this, StarterScreen.class);
                startActivity(intent);
                finish();
                break;

        }
    }
}
