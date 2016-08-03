package com.brnocalizer.communication;

/**
 * class containing the different pieces of data implemented in the protocol
 * this class was created in order to change the communication's protocol easily
 */
public class Protocol {

    public static final String ACCELEROMETER_ID = "ACCELEROMETERDATA";
    public static final String GYROSCOPE_ID = "GYRODATA";
    public static final String COMPASS_ID ="COMPASSDATA";
    public static final String GPS_ID ="GPSDATA";
    public static final String STOP_ID = "STOP";


    public static final String SEPARATOR = " ";
    public static final String DATA_SPLITTER = ",";
}
