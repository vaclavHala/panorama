package com.mygdx.game.demo;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mygdx.game.SimpleAndroidLocationServicePull;
import com.mygdx.game.service.LocationServiceException;

public class LocationDemoLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.d("PANO_PROV", "All providers: " + locationManager.getAllProviders());
        Log.d("PANO_PROV", "Enabled providers: " + locationManager.getProviders(true));

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        SimpleAndroidLocationServicePull gps = new SimpleAndroidLocationServicePull(locationManager);
        try {
            gps.start();
            Toast.makeText(getApplication(), "GPS is go :)", Toast.LENGTH_LONG).show();
            Log.e("LOC", "Got GPS :)");
        } catch (LocationServiceException e) {
            Log.e("LOC", "No GPS :(");
            Toast.makeText(getApplication(), "No GPS :(", Toast.LENGTH_LONG).show();
        }

        initialize(new LocationDemo(gps), config);
    }

}
