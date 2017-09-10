package com.mygdx.game.service;

public interface LocationServicePush {

    void addListener(LocationListener listener) throws LocationServiceException;

    void removeListener(LocationListener listener);

    public interface LocationListener {

        void update(float lon, float lat);

        void error(LocationServiceException err);

    }

}
