package com.brnocalizer.worker;

import android.hardware.SensorManager;

/**
 * @author Thomas FARDEAU
 */
public class Settings {

    private int sensorRate;
    private long GPSRate;
    private int videoRate;

    //official data of the sensor rate given by Android
    public static final int NORMAL_RATE = 200000;
    public static final int UI_RATE = 60000;
    public static final int GAME_RATE = 20000;
    public static final int FASTEST_RATE = 0;


    private Settings()
    {
        sensorRate = SensorManager.SENSOR_DELAY_NORMAL;
        GPSRate = 0;
        videoRate = 0;
    }


    /**
     * implementing a singleton
     */
    private static class SettingsHolder
    {
        private final static Settings instance = new Settings();
    }

    /**
     * provides a way to reach the singleton
     */
    public static Settings getInstance()
    {
        return SettingsHolder.instance;
    }

    public void setGPSRate(long rate) {
        GPSRate = rate;
    }

    public long getGPSRate() {
        return GPSRate;
    }

    public void setSensorRate(int rate) {
        sensorRate = rate;
    }

    public int getSensorRate() {
        return sensorRate;
    }

    public void setVideoRate(int rate) {
        videoRate = rate;
    }

    public int getVideoRate() {
        return videoRate;
    }
}
