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

import com.brnocalizer.R;
import com.brnocalizer.worker.AppData;
import com.brnocalizer.worker.Relay;

public class Welcome extends Activity {

    private Messenger serviceMessenger = null;

    private static int tries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(Welcome.this).unregisterReceiver(receveur);
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

        Log.d(AppData.DEBUG_WELCOME, "onStart: connecting ...");
        LocalBroadcastManager.getInstance(this).registerReceiver(receveur, new IntentFilter(AppData.CONNECTION_INTENT));

        final Intent intent = new Intent(this, Relay.class);
        getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        tries = 0;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                attemptConnection();
            }
        }, 3000L);
    }

    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            serviceMessenger = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            serviceMessenger = null;
        }
    };

    private void attemptConnection() {
        Log.d(AppData.DEBUG_WELCOME, "attemptConnection: attempting ...");
        if (serviceMessenger != null) {
            try {
                Log.d(AppData.DEBUG_WELCOME, "attemptConnection: sending message");
                Message msg = Message.obtain(null, AppData.CONNECTION, 0);
                serviceMessenger.send(msg);
            }
            catch (RemoteException e) {
            }
        }

    }

    private BroadcastReceiver receveur = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra(AppData.CONNECTION_INTENT_CONTENT);

            if (tries<3){

                if (message.equals(AppData.CONNECTION_INTENT_SUCCESS))
                {
                    Intent activityIntent = new Intent(Welcome.this, StarterScreen.class);
                    startActivity(activityIntent);
                }
                else
                {
                    tries++;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            attemptConnection();
                        }
                    }, 2000L);

                }

            }else{

                AlertDialog.Builder adb = new AlertDialog.Builder(Welcome.this);

                adb.setTitle(AppData.CONNECTION_FAILURE_TITLE);

                adb.setMessage(AppData.CONNECTION_FAILURE_MESSAGE);

                adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        finish();
                        startActivity(getIntent());

                    }
                });

                adb.setCancelable(false);

                adb.show();


            }


        }
    };
}
