package com.mygdx.game.desktop.service;

import com.mygdx.game.service.LocationServiceException;
import com.mygdx.game.service.LocationServicePush;

public class MockLocationServicePush implements LocationServicePush {

    private final float fixLon;
    private final float fixLat;

    public MockLocationServicePush(float fixLon, float fixLat) {
        this.fixLon = fixLon;
        this.fixLat = fixLat;
    }

    @Override
    public void addListener(LocationListener listener) throws LocationServiceException {
        listener.update(fixLon, fixLat);
    }

    @Override
    public void removeListener(LocationListener listener) {
    }

}
