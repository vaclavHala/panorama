package com.mygdx.game.model;

import com.badlogic.gdx.math.Vector3;

public class Visibility {

    /**
     * Given some landscape, this determines if two points can see each other.
     * @return true if there is clean line of sight between the two points.
     */
    public boolean isVisibleFrom(Vector3 from, Vector3 to) {
        return true;
    }

}
