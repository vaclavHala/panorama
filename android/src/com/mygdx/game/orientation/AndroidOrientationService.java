package com.mygdx.game.orientation;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix3;
import com.mygdx.game.service.OrientationService;
import java.util.Arrays;
import static java.lang.String.format;

public class AndroidOrientationService implements OrientationService, SensorEventListener {

    private static final int SENSOR_DELAY_MICROS = 50 * 1000; // 50ms

    //        private final WindowManager mWindowManager;

    private final SensorManager mSensorManager;

    private final Sensor mRotationSensor;

    private int mLastAccuracy;

    public AndroidOrientationService(Context context) {
        //            mWindowManager = activity.getWindow().getWindowManager();
        mSensorManager = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);

        // Can be null if the sensor hardware is not available
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        for (Sensor s : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
            Log.i("shit", format("Available sensor: %s, %s, %s, %s", s.getName(), s.getVendor(), s.getVersion(), s.getType()));
        }

        if (mRotationSensor == null) {
            Log.w("shit", "Rotation vector sensor not available; will not provide orientation data.");
            return;
        }

        Log.i("ORIENTATION", "Registering orientation sensor listener");
        mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY_MICROS);
        //        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (mLastAccuracy != accuracy) {
            mLastAccuracy = accuracy;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }
        if (event.sensor == mRotationSensor) {
            updateOrientation(event.values);
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void updateOrientation(float[] rotationVector) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

        //        Gdx.app.log("SENS", "Vec: "+ Arrays.toString(rotationVector));
        //        Gdx.app.log("SENS", "Mat: "+ new Matrix3(rotationVector));

        //        final int worldAxisForDeviceAxisX;
        //        final int worldAxisForDeviceAxisY;

        //        // Remap the axes as if the device screen was the instrument panel,
        //        // and adjust the rotation matrix for the device orientation.
        //        switch (mWindowManager.getDefaultDisplay().getRotation()) {
        //            case Surface.ROTATION_0:
        //            default:
        //                worldAxisForDeviceAxisX = SensorManager.AXIS_X;
        //                worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
        //                break;
        //            case Surface.ROTATION_90:
        //                worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
        //                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
        //                break;
        //            case Surface.ROTATION_180:
        //                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
        //                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
        //                break;
        //            case Surface.ROTATION_270:
        //                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
        //                worldAxisForDeviceAxisY = SensorManager.AXIS_X;
        //                break;
        //        }

        //        float[] adjustedRotationMatrix = new float[9];
        //        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
        //                worldAxisForDeviceAxisY, adjustedRotationMatrix);
        //
        //        // Transform rotation matrix into azimuth/pitch/roll
        //        float[] orientation = new float[3];
        //        SensorManager.getOrientation(adjustedRotationMatrix, orientation);
        //
        //        // Convert radians to degrees
        //        float pitch = orientation[1] * -57;
        //        float roll = orientation[2] * -57;
        //
        //        mListener.onOrientationChanged(pitch, roll);
    }

    @Override
    public void addListener(OrientationListener listener) {

    }

    @Override
    public void removeListener(OrientationListener listener) {

    }
}
