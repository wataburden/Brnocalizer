/**
 * @class Relay.java
 *
 * @brief Ce module permet de faire le relais entre les activités et la communication
 *
 * Vous pouvez creer plusieurs paragraphes en laissant une ligne vide apres
 * le paragraphe precedent.
 *
 * Vous noterez que les identifiants du code source sont en anglais, tandis
 * que les commentaires sont ecrits en français.
 *
 * @version 1.0
 * @date 09/05/2016
 * @author Gautier MARTIN
 * @copyright (c) BSD 2 clauses, <2016>, <A1, ESEO>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 *
 *
 */

package com.brnocalizer.worker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.brnocalizer.communication.FPGAProxy;

/**
 * Service class to connect the activities and the other object of the application
 */
public class Relay extends Service
{

    private static Intent broadcastIntent;

    private static Context context;


    Messenger messenger = new Messenger(new HandlerReception());

    private static Context getRelayContext(){
        return context;
    }

    /**
     * called when the activity binds to the service
     * @param intent :
     * @return the identifier of the messenger that has to be reached
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(AppData.DEBUG_RELAY, "onBind: binding");
        return messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }


    /**
     * This HandlerReception receive messages from the activities and transfer them to the other classes
     *
     */
    private static class HandlerReception extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            String message;
            switch (msg.what) {
                case AppData.CONNECTION:
                    FPGAProxy.getInstance().getPostman().initializeConnection();
                    break;

                case AppData.ACCELEROMETER :
                    message = b.getString("data");
                    FPGAProxy.getInstance().sendAccelerometerData(message);
                    break;

                case AppData.PHOTO :
                    break;

                case AppData.MAGNETIC :
                    message = b.getString("data");
                    FPGAProxy.getInstance().sendCompassData(message);
                    break;

                case AppData.GYROSCOPE :
                    message = b.getString("data");
                    FPGAProxy.getInstance().sendGyroscopeData(message);
                    break;

                case AppData.GPS_DATA :
                    message = b.getString("data");
                    FPGAProxy.getInstance().sendGPSData(message);
                    break;

                case AppData.VIDEO_DATA :
                    FPGAProxy.getInstance().getPostman().sendVideo();
                    break;

                case AppData.ASK_SETTINGS :
                    broadcastIntent = new Intent(AppData.SETTINGS_INTENT);
                    broadcastIntent.putExtra(AppData.SENSOR_SETTINGS, Settings.getInstance().getSensorRate());
                    broadcastIntent.putExtra(AppData.GPS_SETTINGS, Settings.getInstance().getGPSRate());
                    broadcastIntent.putExtra(AppData.FPS_SETTINGS, Settings.getInstance().getVideoRate());
                    LocalBroadcastManager.getInstance(getRelayContext()).sendBroadcast(broadcastIntent);
                    break;

                case AppData.SENSOR_RATE:
                    int sensorRate = b.getInt("rate");
                    Settings.getInstance().setSensorRate(sensorRate);
                    break;

                case AppData.GPS_RATE :
                    long GPSRate = b.getLong("rate");
                    Log.d(AppData.DEBUG_RELAY, "handleMessage: "+GPSRate);
                    Settings.getInstance().setGPSRate(GPSRate);
                    break;

                case AppData.FPS_RATE:
                    int FPSRate = b.getInt("rate");
                    Settings.getInstance().setVideoRate(FPSRate);
                    break;

                case AppData.STOP :
                    FPGAProxy.getInstance().sendStop();
                    break;

                case AppData.APP_END:
                    FPGAProxy.getInstance().getPostman().stopCommunication();
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * inform the activities that have registered an associated listener about the current connection status
     */
    public void informConnection(int state) {

        if(state == 1){
            broadcastIntent = new Intent(AppData.CONNECTION_INTENT);
            broadcastIntent.putExtra(AppData.CONNECTION_INTENT_CONTENT, AppData.CONNECTION_INTENT_SUCCESS);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        }else{
            broadcastIntent = new Intent(AppData.CONNECTION_INTENT);
            broadcastIntent.putExtra(AppData.CONNECTION_INTENT_CONTENT, AppData.CONNECTION_INTENT_FAILURE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        }

    }

    /**
     * Inform the activities that have registered an associated listener when the video has been sent
     */
    public void informVideoSendingStatus() {

        broadcastIntent = new Intent(AppData.VIDEO_STATUS_INTENT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

    }

    /**
     * Inform the activities that have registered an associated listener when the connection
     * with the server has crashed
     */
    public void informServerCrash(){
        broadcastIntent = new Intent(AppData.DISCONNECTION_INTENT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }


}

