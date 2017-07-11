package com.mygdx.game.model;

import com.badlogic.gdx.math.Vector3;

public class Feature {

    public final String name;
    /**
     * In real world units (lon degrees, lat degrees, elev meters)
     */
    public final Vector3 position;

    public Feature(String name, Vector3 position) {
        this.name = name;
        this.position = position;
    }

    @Override
    public String toString() {
        return "Feature{" +
               "name=" + name +
               ", position=" + position +
               '}';
    }

}
