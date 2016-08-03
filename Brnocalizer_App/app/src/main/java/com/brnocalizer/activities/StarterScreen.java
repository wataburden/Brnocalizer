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
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.brnocalizer.R;
import com.brnocalizer.worker.AppData;
import com.brnocalizer.worker.Relay;

public class StarterScreen extends Activity {

    Messenger serviceMessenger = null;

    /**
     * Listener set to receive the disconnection info from Postman
     */
    private BroadcastReceiver disconnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(AppData.DEBUG_SENSOR_SCREEN, "onReceive: Receiving result");

            //Construction of a popup if the connection was interrupted
            AlertDialog.Builder adb = new AlertDialog.Builder(StarterScreen.this);

            adb.setTitle(AppData.DISCONNECTION_TITLE);

            adb.setPositiveButton("Validate", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    Intent intent = new Intent(StarterScreen.this, Welcome.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }
            });

            //The user can't cancel the popup
            adb.setCancelable(false);

            adb.show();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.starter_screen);
    }

    /**
     * called when the activity pauses
     */
    @Override
    protected void onPause() {
        super.onPause();

        //unregister the service and the broadcastReceivers
        LocalBroadcastManager.getInstance(StarterScreen.this).unregisterReceiver(disconnectionReceiver);

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

        LocalBroadcastManager.getInstance(this).registerReceiver(disconnectionReceiver, new IntentFilter(AppData.DISCONNECTION_INTENT));

        // Bind the activity to the service
        final Intent intent = new Intent(this, Relay.class);
        getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Called when the connection to the service changes
     */
    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(AppData.DEBUG_SENSOR_SCREEN, "onServiceConnected: connected to the service");
            serviceMessenger = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            serviceMessenger = null;
        }
    };

    public void startAction(View view){
        Intent intent = new Intent(this, SensorScreen.class);
        startActivity(intent);
    }

    /**
     * called when the button "settings" is pressed
     * @param view : identifying the activity's view
     */
    public void goToSettings(View view){
        Intent intent = new Intent(this, SetupScreen.class);
        intent.putExtra("activity", SetupScreen.STARTER_SCREEN);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        if (serviceMessenger != null) {
            try {
                Message msg = Message.obtain(null, AppData.APP_END, 0);
                serviceMessenger.send(msg);
            }
            catch (RemoteException e) {
            }
        }
        super.onBackPressed();
    }
}
