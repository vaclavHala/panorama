package com.mygdx.game.service;

import com.badlogic.gdx.math.Vector2;

public interface LocationServicePull {

    /**
     * @param setTo location is written in here, saves allocation
     * @return setTo updated to last known location
     */
    Vector2 getLocation(Vector2 setTo) throws LocationServiceException;

}
