package com.mygdx.game.service;

public interface OrientationService {

    void addListener(OrientationListener listener);

    void removeListener(OrientationListener listener);

    public interface OrientationListener {

        void update(float yaw, float pitch, float roll);

    }

}
