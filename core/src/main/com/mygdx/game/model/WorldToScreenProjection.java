package com.mygdx.game.model;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class WorldToScreenProjection {

    private final Camera cam;
    private final Vector3 origin;

    public WorldToScreenProjection(Camera cam, Vector3 origin) {
        this.cam = cam;
        this.origin = origin;
    }

    /**
     * Returns coordinates on screen of point given in world banana units (real world unit * elevCfg scaler)
     */
    Vector2 toScreen(Vector3 worldPosition) {
        Vector3 tmp = new Vector3(worldPosition).sub(origin);
        this.cam.project(tmp);
        return new Vector2(tmp.x, tmp.y);
    }

}
