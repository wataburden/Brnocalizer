package com.brnocalizer.worker;


import android.os.Environment;

public class AppData {

    //debug
    public static final String DEBUG_WELCOME = "Welcome";
    public static final String DEBUG_RELAY= "Relay";
    public static final String DEBUG_SENSOR_SCREEN= "SensorScreen";
    public static final String DEBUG_SETUP_SCREEN= "Setup";
    public static final String DEBUG_POSTMAN= "Postman";


    //popup messages
    public static final String CONNECTION_FAILURE_TITLE = "Can't to connect to FPGA";
    public static final String CONNECTION_FAILURE_MESSAGE = "Please check your internet connection";

    public static final String PROVIDER_OUT_OF_SERVICE = "Provider out of service";
    public static final String PROVIDER_AVAILABLE = "Provider available";
    public static final String PROVIDER_UNAVAILABLE = "Provider temporarily unavailable";

    public static final String DISCONNECTION_TITLE = "connection interrupted !";

    public static final String ASK_LOCATION_PERMISSION = "Need permission to use location";

    public static final String ASK_SAVE_CHANGES = "Do you want to save the changes ?";
    public static final String NO_CHANGES = "No changes to save";

    public static final String SENDING_VIDEO_BACK_PRESSED = "please wait for the end of the process";


    //Broadcast Intents
    public static final String CONNECTION_INTENT = "connection";
    public static final String CONNECTION_INTENT_CONTENT = "state";
    public static final String CONNECTION_INTENT_SUCCESS= "connected";
    public static final String CONNECTION_INTENT_FAILURE= "failure";

    public static final String VIDEO_STATUS_INTENT = "VideoStatus";

    public static final String SETTINGS_INTENT = "settings";
    public static final String GPS_SETTINGS ="GPS";
    public static final String SENSOR_SETTINGS ="sensor";
    public static final String FPS_SETTINGS ="FPS";

    public static final String DISCONNECTION_INTENT = "disconnection";



    //relay requests' identifiers
    public static final int CONNECTION = 1;
    public static final int ACCELEROMETER = 2;
    public static final int PHOTO = 3;
    public static final int MAGNETIC = 4;
    public static final int GYROSCOPE = 5;
    public static final int GPS_DATA = 6;
    public static final int VIDEO_DATA = 7;
    public static final int ASK_SETTINGS = 8;
    public static final int SENSOR_RATE = 9;
    public static final int GPS_RATE = 10;
    public static final int FPS_RATE = 11;
    public static final int STOP = 12;
    public static final int APP_END = 13;


    public static final String PATH = Environment.getExternalStorageDirectory().getPath();
    public static final String FILENAME = "BrnocalizerVideo.mp4";




}
