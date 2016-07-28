package com.mygdx.game;

import android.location.LocationManager;

import com.badlogic.gdx.math.Vector2;

public class AndroidLocationService implements LocationService {

    private final LocationManager gps;

    public AndroidLocationService(LocationManager gps) {
        this.gps = gps;
    }

    @Override
    public Vector2 findCurrentLocation() {
//        this.gps.
        throw  new UnsupportedOperationException();
    }

}
