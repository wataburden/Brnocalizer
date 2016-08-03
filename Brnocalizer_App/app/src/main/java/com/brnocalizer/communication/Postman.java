/**
 * @class Postman.java
 *
 * @brief Establish connection between the device and the server.
 * Sends data to the server
 *
 *
 *
 * @version 1.0
 * @date 06/06/2016
 * @author Thomas Fardeau
*/

package com.brnocalizer.communication;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.brnocalizer.worker.AppData;
import com.brnocalizer.worker.Relay;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;


public class Postman {

    private Socket monSocket;
    private DataOutputStream myDataOutputStream;

    private Relay myRelay;

    /**
     * Constructor of the Postman class
     */
    private Postman()
    {
        myRelay = new Relay();
    }


    /**
     * creates a singleton of postman
     */
    private static class PostmanHolder
    {
        private final static Postman instance = new Postman();
    }

    /**
     * way to acces postman's singleton
     */
    public static Postman getInstance()
    {
        return PostmanHolder.instance;
    }






    /**
     * called when wanting to send a message to the server
     * starts the SendingTask after converting the given Strings into byte arrays
     * @param code : String identifying the nature of the message
     * @param message : String containing the data to send to the server
     */
    public void sendMessage(String code, String message){

        byte[] actionCode = code.getBytes(Charset.forName("UTF-8"));
        byte[] convert = message.getBytes(Charset.forName("UTF-8"));
        byte[] toSend = new byte[1024];
        System.arraycopy(actionCode, 0, toSend, 0, actionCode.length);
        System.arraycopy(convert,0, toSend, actionCode.length, convert.length);
        SendingTask t = new SendingTask();
        t.execute(toSend);

    }


    /**
     * AsyncTask sending data to the server via socket
     *
     */
    private class SendingTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            Log.d(AppData.DEBUG_POSTMAN, "Postman");
            try {
                myDataOutputStream.write(data[0]);
                myDataOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                myRelay.informServerCrash();
            }
            return null;
        }

    }


    /**
     * called when the application wants to bind to the server's socket
     */
    public void initializeConnection(){

        new TaskInit().execute("147.251.47.248");

    }


    /**
     * AsyncTask initiating the connection to the server
     * creates a dataOutputStream to send data to the server
     *
     */
    private class TaskInit extends AsyncTask<String, Integer , Integer> {


        @Override
        protected Integer doInBackground(String... strings) {

            String ip = strings[0];

            try
            {
                //Creating socket and dataOutputStream
                monSocket = new Socket(ip,1248);
                myDataOutputStream = new DataOutputStream(monSocket.getOutputStream());

            }
            catch (UnknownHostException e1)
            {
                e1.printStackTrace();
                //Returns 0 if the connection fails
                return 0;
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
                //Returns 0 if the connection fails
                return 0;
            }

            //Returns 1 if the connection is completed
            return 1;

        }


        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            //inform the app of the connection status
            myRelay.informConnection(integer);

        }
    }



    /**
     * launch the closing task
     */
    public void stopCommunication(){

        ClosingTask t = new ClosingTask();
        t.execute();

    }





    /**
     * AsyncTask closing the connection to the server
     * Closes the DataOutputStream and the socket
     *
     */
    private class ClosingTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... voids) {

            try
            {
                //closes all connections
                myDataOutputStream.close();
                monSocket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }


    }


    /**
     *
     */
    public void sendVideo(){
        Log.d(AppData.DEBUG_POSTMAN, "sendVideo: ça va commencer");
        SendingVideoTask video = new SendingVideoTask();
        video.execute();
    }


    /**
     *
     */
    private class SendingVideoTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            FileInputStream fis;
            BufferedInputStream bis;

            //get the data from the file that was saved previously in the device
            File myFile = new File(AppData.PATH, AppData.FILENAME);
            byte [] mybytearray  = new byte [(int)myFile.length()];

            try {
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                bis.read(mybytearray,0,mybytearray.length);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(AppData.DEBUG_POSTMAN, "doInBackground: j'ai le fichier");

            try {
                myDataOutputStream.write(mybytearray, 0, mybytearray.length);
                myDataOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            myFile.delete();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            myRelay.informVideoSendingStatus();

        }
    }


}
