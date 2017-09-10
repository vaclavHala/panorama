package com.mygdx.game;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.service.LocationServiceException;
import com.mygdx.game.service.LocationServicePush;
import java.util.ArrayList;
import java.util.List;

public class AndroidLocationServicePush implements LocationServicePush, android.location.LocationListener {

    private final LocationManager gps;
    private final List<LocationListener> listeners;
    private final Looper panoLooper;
    private final Application sink;

    public AndroidLocationServicePush(
            LocationManager gps,
            Looper panoLooper,
            Application sink) {
        this.gps = gps;
        this.panoLooper = panoLooper;
        this.sink = sink;
        this.listeners = new ArrayList<LocationListener>();
    }

    public boolean isEnabled() {
        return gps.getProviders(true).contains(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void addListener(LocationListener listener) throws LocationServiceException {
        if (listeners.isEmpty()) {
            if (!isEnabled()) {
                throw new LocationServiceException("GPS provider is not available. Enabled providers: " + gps.getProviders(true));
            }
            Log.d("pano.loc", "Current thread: " + Thread.currentThread());
            gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this, this.panoLooper);
        }

        listeners.add(listener);

        // This causes extra event every time new listener is added, should not hurt anyone though
        Location devLastKnown = gps.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (devLastKnown != null) {
            this.onLocationChanged(devLastKnown);
        }
    }

    @Override
    public void removeListener(LocationListener listener) {
        boolean removed = this.listeners.remove(listener);

        if (!removed) {
            log("Listener was not registered: %s", listener);
        }
        if (listeners.isEmpty()) {
            gps.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        log("Location changed: %s", location);
        this.postUpdate((float) location.getLongitude(),
                        (float) location.getLatitude());
    }

    private void postUpdate(final float lon, final float lat) {
        if (this.listeners.isEmpty()) {
            return;
        }
        this.sink.postRunnable(new Runnable() {

            @Override
            public void run() {
                for (LocationListener l : AndroidLocationServicePush.this.listeners) {
                    l.update(lon, lat);
                }
            }
        });
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
        Gdx.app.log("pano.loc.push", String.format(msg, args));
    }

}
