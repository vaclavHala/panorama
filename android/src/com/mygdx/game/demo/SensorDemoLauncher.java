package com.mygdx.game.demo;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import static java.lang.String.format;

public class SensorDemoLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SensorManager mSensorManager = (SensorManager) getApplicationContext().getSystemService(Activity.SENSOR_SERVICE);

        for(Sensor s: mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
            Log.i("sensor", format("Available sensor: %s, %s, %s, %s" ,s.getName(), s.getVendor(), s.getVersion(), s.getType()));
        }

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        initialize(new SensorDemo(mSensorManager), config);
    }

}
