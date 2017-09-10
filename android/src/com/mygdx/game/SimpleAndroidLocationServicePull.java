package com.mygdx.game;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.service.LocationServiceException;
import com.mygdx.game.service.LocationServicePull;
import java.util.List;

public class SimpleAndroidLocationServicePull implements LocationServicePull, LocationListener {

    private final LocationManager gps;
    private Vector2 lastKnown;

    public SimpleAndroidLocationServicePull(LocationManager gps) {
        this.gps = gps;
    }

    public void start() throws LocationServiceException {
        List<String> providers = gps.getProviders(true);
        Log.d("pano.loc", "All providers: " + gps.getAllProviders());
        Log.d("pano.loc", "Enabled providers: " + gps.getProviders(true));
        if (!providers.contains(LocationManager.GPS_PROVIDER)) {
            throw new LocationServiceException("GPS provider is not available. Enabled providers: " + providers);
        }
        gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        Location devLastKnown = gps.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (devLastKnown != null) {
            this.onLocationChanged(devLastKnown);
        }
    }

    public void stop() {
        gps.removeUpdates(this);
    }

    @Override
    public synchronized Vector2 getLocation(Vector2 setTo) throws LocationServiceException {
        if (this.lastKnown == null) {
            throw new LocationServiceException("Unknown location");
        }
        return setTo.set(this.lastKnown);
    }

    @Override
    public synchronized void onLocationChanged(Location location) {
        log("Location changed: %s", location);
        if (lastKnown == null) {
            lastKnown = new Vector2();
        }
        this.lastKnown.set((float) location.getLongitude(),
                           (float) location.getLatitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        log("Status changed: %s %s %s", provider, status, extras);
    }

    @Override
    public void onProviderEnabled(String provider) {
        log("Provider enabled: %s", provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        log("Provider disabled: %s", provider);
    }

    private static void log(String msg, Object... args) {
        Log.d("pano.loc.pull", String.format(msg, args));
    }

}
