package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

public class PresetLocationService implements LocationService {

    private final Vector2 location;

    public PresetLocationService(Vector2 location) {
        this.location = location;
    }

    @Override
    public Vector2 findCurrentLocation() {
        return this.location;
    }
}
