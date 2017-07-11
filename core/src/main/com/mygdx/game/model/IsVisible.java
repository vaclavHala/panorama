package com.mygdx.game.model;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;

public class IsVisible {

    private final Visibility visibility;
    private final Camera cam;
    private final Vector3 object;

    private final Vector3 from;
    private final Plane camPlane;

    public IsVisible(Visibility visibility, Camera cam, Vector3 object) {
        this.visibility = visibility;
        this.cam = cam;
        this.object = object;
        this.from = new Vector3();
        this.camPlane = new Plane();
    }

    public boolean isVisible() {
        return isInFieldOfView() && isNotObscured();
    }

    private boolean isInFieldOfView() {
        camPlane.set(cam.position, cam.direction);
        return camPlane.distance(object) > 0;
    }

    private boolean isNotObscured() {
        this.from.set(cam.position);
        return this.visibility.isVisibleFrom(from, object);
    }
}
