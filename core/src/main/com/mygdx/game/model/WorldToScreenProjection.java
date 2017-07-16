package com.mygdx.game.model;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class WorldToScreenProjection {

    private final Camera cam;

    public WorldToScreenProjection(Camera cam) {
        this.cam = cam;
    }

    /**
     * Returns coordinates on screen of point given in internal units
     */
    Vector2 toScreen(Vector3 worldPosition) {
        Vector3 tmp = worldPosition.cpy();
        this.cam.project(tmp);
        return new Vector2(tmp.x, tmp.y);
    }

}
