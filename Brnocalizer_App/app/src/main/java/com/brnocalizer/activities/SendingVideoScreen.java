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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.brnocalizer.R;
import com.brnocalizer.worker.AppData;
import com.brnocalizer.worker.Relay;

public class SendingVideoScreen extends Activity {

    private Messenger serviceMessenger;


    /**
     * Listener set to receive the disconnection info from Postman
     */
    private BroadcastReceiver disconnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Construction of a popup if the connection was interrupted
            AlertDialog.Builder adb = new AlertDialog.Builder(SendingVideoScreen.this);

            adb.setTitle(AppData.DISCONNECTION_TITLE);

            adb.setPositiveButton("Validate", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    Intent intent = new Intent(SendingVideoScreen.this, Welcome.class);
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
     * Listener set to receive the end status of the SendingVideo Task
     */
    private BroadcastReceiver videoStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            intent = new Intent(SendingVideoScreen.this, StarterScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sending_video_screen);
    }

    public void sendVideo() {

        if (serviceMessenger != null) {
            try {
                Message msg = Message.obtain(null, AppData.VIDEO_DATA, 0);
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
            }
        }

    }


    /**
     * called when the activity pauses
     */
    @Override
    protected void onPause() {
        super.onPause();

        //unregister the service and the broadcastReceivers
        LocalBroadcastManager.getInstance(SendingVideoScreen.this).unregisterReceiver(disconnectionReceiver);
        LocalBroadcastManager.getInstance(SendingVideoScreen.this).unregisterReceiver(videoStatusReceiver);
        getApplicationContext().unbindService(connection);

    }

    /**
     * called when the activity resumes
     */
    @Override
    protected void onResume() {
        super.onResume();

        //register the broadcast receivers
        LocalBroadcastManager.getInstance(this).registerReceiver(disconnectionReceiver, new IntentFilter(AppData.DISCONNECTION_INTENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(videoStatusReceiver, new IntentFilter(AppData.VIDEO_STATUS_INTENT));

        // Bind the activity to the service
        final Intent intent = new Intent(this, Relay.class);
        getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        //Set the screen in immersive mode
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);


    }

    /**
     * Called when the connection to the service changes
     */
    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            serviceMessenger = new Messenger(service);
            sendVideo();
        }

        public void onServiceDisconnected(ComponentName className) {
            serviceMessenger = null;
        }
    };

    @Override
    public void onBackPressed() {
        Toast.makeText(this, AppData.SENDING_VIDEO_BACK_PRESSED, Toast.LENGTH_SHORT).show();
    }
}
