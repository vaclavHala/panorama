package com.mygdx.game.model;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import java.util.Arrays;

public class LandscapeProjection {

    private static final Vector3 UP = new Vector3(0, 1, 0);
    private static final Vector3 DOWN = new Vector3(0, -1, 0);

    private final float[] vertices;
    private final short[] triIndices;
    private final int vertComponnets;
    private final Camera cam;

    public LandscapeProjection(Camera cam, float[] vertices, short[] triIndices, int vertComponnets) {
        this.cam = cam;
        this.vertices = vertices;
        this.triIndices = triIndices;
        this.vertComponnets = vertComponnets;
    }

    /**
     * Returns screen position of the given geo point.
     * Takes into account elevation and current camera position
     */
    public Vector2 projectToLandscape(Vector2 landscapePosition) {
        Vector3 origin = new Vector3(landscapePosition.x, 0, landscapePosition.y);
        Ray rayUp = new Ray(origin, UP);
        Ray rayDown = new Ray(origin, DOWN);
        Vector3 intersection = new Vector3();
        System.out.println(origin);
        System.out.println(Arrays.toString(this.vertices));
        boolean hit = Intersector.intersectRayTriangles(rayDown, this.vertices, this.triIndices, this.vertComponnets, intersection);
        if (!hit) {
            hit = Intersector.intersectRayTriangles(rayUp, this.vertices, this.triIndices, this.vertComponnets, intersection);
        }
        if (!hit) {
            throw new IllegalArgumentException("Position " + landscapePosition + " does not intersect landscape");
        }
        Vector3 screenCoords = this.cam.project(intersection);
        return new Vector2(screenCoords.x, screenCoords.y);
    }
}
