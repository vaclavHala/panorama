package com.mygdx.game.orientation;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import java.util.Arrays;
import static java.lang.String.format;

public class AndroidAccelerometerOrientation implements SensorEventListener {

    private final SensorManager sensors;

    private final Sensor accelerometer;

    public AndroidAccelerometerOrientation(Context context) {
        //            mWindowManager = activity.getWindow().getWindowManager();
        sensors = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);

        accelerometer = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            throw new IllegalStateException("Expected accelerometer to be available");
        }

        Log.i("ORIENTATION", "Registering orientation sensor listener");
        sensors.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        //        mSensorManager.unregisterListener(this);

    }

    static final int axisSwap[][] = {
                                     {1, -1, 0, 1}, // ROTATION_0
                                     {-1, -1, 1, 0}, // ROTATION_90
                                     {-1, 1, 0, 1}, // ROTATION_180
                                     {1, 1, 1, 0}}; // ROTATION_270

    void canonicalOrientationToScreenOrientation(int displayRotation, float[] canVec, float[] screenVec)
    {

        final int[] as = axisSwap[displayRotation];
        screenVec[0] = (float) as[0] * canVec[as[2]];
        screenVec[1] = (float) as[1] * canVec[as[3]];
        screenVec[2] = canVec[2];
    }

    float[] can = new float[3];
    float[] screen = new float[3];

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.equals(this.accelerometer)) {
            // we always run in landscape
            canonicalOrientationToScreenOrientation(1, event.values, screen);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
